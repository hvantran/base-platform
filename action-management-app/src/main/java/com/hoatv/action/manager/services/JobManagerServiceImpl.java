package com.hoatv.action.manager.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoatv.action.manager.api.JobManagerService;
import com.hoatv.action.manager.collections.JobDocument;
import com.hoatv.action.manager.collections.JobExecutionResultDocument;
import com.hoatv.action.manager.dtos.*;
import com.hoatv.action.manager.repositories.JobDocumentRepository;
import com.hoatv.action.manager.repositories.JobExecutionResultDocumentRepository;
import com.hoatv.fwk.common.services.CheckedSupplier;
import com.hoatv.fwk.common.services.TemplateEngineEnum;
import com.hoatv.fwk.common.ultilities.DateTimeUtils;
import com.hoatv.monitor.mgmt.TimingMetricMonitor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class JobManagerServiceImpl implements JobManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobManagerServiceImpl.class);

    private final ScriptEngineService scriptEngineService;

    private final ObjectMapper objectMapper;

    private final JobDocumentRepository jobDocumentRepository;

    private final JobExecutionResultDocumentRepository jobExecutionResultDocumentRepository;

    private static final Map<String, String> DEFAULT_CONFIGURATIONS = new HashMap<>();

    public static final String TEMPLATE_ENGINE_NAME = "templateEngineName";

    static {
        DEFAULT_CONFIGURATIONS.put(TEMPLATE_ENGINE_NAME, "freemarker");
    }

    @Autowired
    public JobManagerServiceImpl(ScriptEngineService scriptEngineService, JobDocumentRepository jobDocumentRepository,
                                 JobExecutionResultDocumentRepository jobExecutionResultDocumentRepository) {
        this.scriptEngineService = scriptEngineService;
        this.jobDocumentRepository = jobDocumentRepository;
        this.jobExecutionResultDocumentRepository = jobExecutionResultDocumentRepository;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Page<JobOverviewDTO> getJobsFromAction(String actionId, PageRequest pageRequest) {
        Page<JobDocument> jobDocuments = jobDocumentRepository.findJobByActionId(actionId, pageRequest);
        List<String> jobIds = jobDocuments.stream().map(JobDocument::getHash).toList();
        List<JobExecutionResultDocument> jobExecutionResultDocuments
                = jobExecutionResultDocumentRepository.findByJobIdIn(jobIds);

        return jobDocuments.map(jobDocument -> {
            String jobId = jobDocument.getHash();
            Optional<JobExecutionResultDocument> jobExecutionResultDocument =
                    jobExecutionResultDocuments.stream().filter(p -> p.getJobId().equals(jobId)).findFirst();

            Supplier<JobExecutionResultDocument> defaultJobResult = () -> JobExecutionResultDocument.builder().build();
            JobExecutionResultDocument jobStat = jobExecutionResultDocument.orElseGet(defaultJobResult);
            return JobOverviewDTO.builder()
                    .name(jobDocument.getJobName())
                    .hash(jobId)
                    .jobState(jobStat.getJobState().name())
                    .jobStatus(jobStat.getJobStatus().name())
                    .startedAt(jobStat.getStartedAt())
                    .elapsedTime(jobStat.getElapsedTime())
                    .failureNotes(jobStat.getFailureNotes())
                    .build();
        });
    }

    @Override
    @TimingMetricMonitor
    public JobResult executeJob(JobDefinitionDTO jobDefinitionDTO) {
        return executeJob(jobDefinitionDTO, "");
    }

    @Override
    @TimingMetricMonitor
    public JobResult executeJob(JobDefinitionDTO jobDefinitionDTO, String actionId) {

        long startedAt = DateTimeUtils.getCurrentEpochTimeInSecond();
        String jobName = jobDefinitionDTO.getJobName();
        JobCategory jobCategory = JobCategory.valueOf(jobDefinitionDTO.getJobCategory());
        String templateName = String.format("%s-%s", jobName, jobCategory);

        String configurations = jobDefinitionDTO.getConfigurations();
        CheckedSupplier<Map<String, Object>> configurationToMapSupplier = () -> objectMapper.readValue(configurations
                , Map.class);
        Map<String, Object> configurationMap = configurationToMapSupplier.get();

        String defaultTemplateEngine = DEFAULT_CONFIGURATIONS.get(TEMPLATE_ENGINE_NAME);
        String templateEngineName = (String) configurationMap.getOrDefault(TEMPLATE_ENGINE_NAME,
                defaultTemplateEngine);

        TemplateEngineEnum templateEngine = TemplateEngineEnum.getTemplateEngineFromName(templateEngineName);
        String jobContent = templateEngine.process(templateName, jobDefinitionDTO.getJobContent(), configurationMap);
        Map<String, Object> jobExecutionContext = new HashMap<>(configurationMap);
        jobExecutionContext.put("templateEngine", templateEngine);
        JobResult jobResult = scriptEngineService.execute(jobContent, jobExecutionContext);
        LOGGER.info("Job result: {}", jobResult);

        JobDocument jobDocument = JobDocument.fromJobDefinition(jobDefinitionDTO, actionId);
        jobDocumentRepository.save(jobDocument);
        LOGGER.info("{} is created successfully.", jobDocument);

        JobStatus jobStatus = StringUtils.isNotEmpty(jobResult.getException()) ? JobStatus.FAILURE : JobStatus.SUCCESS;
        long endedAt = DateTimeUtils.getCurrentEpochTimeInSecond();
        JobExecutionResultDocument jobExecutionResultDocument = JobExecutionResultDocument.builder()
                .jobState(JobState.COMPLETED)
                .jobStatus(jobStatus)
                .startedAt(startedAt)
                .jobId(jobDocument.getHash())
                .failureNotes(jobResult.getException())
                .endedAt(endedAt)
                .elapsedTime(endedAt - startedAt)
                .createdAt(startedAt)
                .build();
        jobExecutionResultDocumentRepository.save(jobExecutionResultDocument);
        LOGGER.info("{} is created successfully.", jobExecutionResultDocument);

        return jobResult;
    }
}
