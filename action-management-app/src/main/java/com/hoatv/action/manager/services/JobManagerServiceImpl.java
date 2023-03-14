package com.hoatv.action.manager.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoatv.action.manager.api.JobManagerService;
import com.hoatv.action.manager.collections.JobDocument;
import com.hoatv.action.manager.collections.JobExecutionResultDocument;
import com.hoatv.action.manager.dtos.JobCategory;
import com.hoatv.action.manager.dtos.JobDefinitionDTO;
import com.hoatv.action.manager.dtos.JobState;
import com.hoatv.action.manager.dtos.JobStatus;
import com.hoatv.action.manager.repositories.JobDocumentRepository;
import com.hoatv.action.manager.repositories.JobExecutionResultDocumentRepository;
import com.hoatv.fwk.common.services.CheckedSupplier;
import com.hoatv.fwk.common.services.TemplateEngineEnum;
import com.hoatv.fwk.common.ultilities.DateTimeUtils;
import com.hoatv.monitor.mgmt.LoggingMonitor;
import com.hoatv.monitor.mgmt.TimingMetricMonitor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class JobManagerServiceImpl implements JobManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobManagerServiceImpl.class);

    private ScriptEngineService scriptEngineService;

    private ObjectMapper objectMapper;

    private JobDocumentRepository jobDocumentRepository;

    private JobExecutionResultDocumentRepository jobExecutionResultDocumentRepository;

    private static Map<String, String> DEFAULT_CONFIGURATIONS = new HashMap<>();

    static {
        DEFAULT_CONFIGURATIONS.put("templateEngineName", "freemarker");
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
    @TimingMetricMonitor
    public JobResult executeJob(JobDefinitionDTO jobDefinitionDTO) {
        return executeJob(jobDefinitionDTO, "");
    }

    @Override
    @TimingMetricMonitor
    public JobResult executeJob(JobDefinitionDTO jobDefinitionDTO, String actionId) {

        long startedAt = DateTimeUtils.getCurrentEpochTimeInSecond();
        String jobName = jobDefinitionDTO.getJobName();
        JobCategory jobCategory = jobDefinitionDTO.getJobCategory();
        String templateName = String.format("%s-%s", jobName, jobCategory);

        String configurations = jobDefinitionDTO.getConfigurations();
        CheckedSupplier<Map> configurationToMapSupplier = () -> objectMapper.readValue(configurations, Map.class);
        Map<String, Object> configurationMap = configurationToMapSupplier.get();

        String defaultTemplateEngine = DEFAULT_CONFIGURATIONS.get("templateEngineName");
        String templateEngineName = (String) configurationMap.getOrDefault("templateEngineName",
                defaultTemplateEngine);

        TemplateEngineEnum templateEngine = TemplateEngineEnum.getTemplateEngineFromName(templateEngineName);
        String jobContent = templateEngine.process(templateName, jobDefinitionDTO.getJobContent(), configurationMap);
        Map<String, Object> jobExecutionContext = new HashMap<>();
        jobExecutionContext.putAll(configurationMap);
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
