package com.hoatv.action.manager.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoatv.action.manager.api.JobManagerService;
import com.hoatv.action.manager.collections.JobDocument;
import com.hoatv.action.manager.collections.JobResultDocument;
import com.hoatv.action.manager.dtos.JobDefinitionDTO;
import com.hoatv.action.manager.dtos.JobOverviewDTO;
import com.hoatv.action.manager.dtos.JobState;
import com.hoatv.action.manager.dtos.JobStatus;
import com.hoatv.action.manager.repositories.JobDocumentRepository;
import com.hoatv.action.manager.repositories.JobExecutionResultDocumentRepository;
import com.hoatv.fwk.common.services.CheckedSupplier;
import com.hoatv.fwk.common.services.TemplateEngineEnum;
import com.hoatv.fwk.common.ultilities.DateTimeUtils;
import com.hoatv.fwk.common.ultilities.Pair;
import com.hoatv.task.mgmt.services.TaskFactory;
import com.hoatv.task.mgmt.services.TaskMgmtServiceV1;
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
import java.util.function.Consumer;
import java.util.function.Supplier;

@Service
public class JobManagerServiceImpl implements JobManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobManagerServiceImpl.class);
    public static final int NUMBER_OF_JOB_THREADS = 20;
    public static final String JOB_MANAGER_APPLICATION = "job-manager";
    public static final int MAX_AWAIT_TERMINATION_MILLIS = 5000;

    private final ScriptEngineService scriptEngineService;

    private final ObjectMapper objectMapper;

    private final JobDocumentRepository jobDocumentRepository;

    private final JobExecutionResultDocumentRepository jobExecutionResultDocumentRepository;

    private final TaskMgmtServiceV1 taskMgmtServiceV1;

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
        this.taskMgmtServiceV1 = TaskFactory.INSTANCE
                .getTaskMgmtServiceV1(NUMBER_OF_JOB_THREADS, MAX_AWAIT_TERMINATION_MILLIS, JOB_MANAGER_APPLICATION);
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Page<JobOverviewDTO> getJobsFromAction(String actionId, PageRequest pageRequest) {
        Page<JobDocument> jobDocuments = jobDocumentRepository.findJobByActionId(actionId, pageRequest);
        List<String> jobIds = jobDocuments.stream().map(JobDocument::getHash).toList();
        List<JobResultDocument> jobResultDocuments
                = jobExecutionResultDocumentRepository.findByJobIdIn(jobIds);

        return jobDocuments.map(jobDocument -> {
            String jobId = jobDocument.getHash();
            Optional<JobResultDocument> jobExecutionResultDocument =
                    jobResultDocuments.stream().filter(p -> p.getJobId().equals(jobId)).findFirst();

            Supplier<JobResultDocument> defaultJobResult = () -> JobResultDocument.builder().build();
            JobResultDocument jobStat = jobExecutionResultDocument.orElseGet(defaultJobResult);
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
/*
    @Override
    @TimingMetricMonitor
    public JobResult processJob(JobDefinitionDTO jobDefinitionDTO) {
        return processJob(jobDefinitionDTO, "");
    }

    @Override
    @TimingMetricMonitor
    public JobResult processJob(JobDefinitionDTO jobDefinitionDTO, String actionId) {

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
        JobResultDocument jobResultDocument = JobResultDocument.builder()
                .jobState(JobState.COMPLETED)
                .jobStatus(jobStatus)
                .startedAt(startedAt)
                .jobId(jobDocument.getHash())
                .failureNotes(jobResult.getException())
                .endedAt(endedAt)
                .elapsedTime(endedAt - startedAt)
                .createdAt(startedAt)
                .build();
        jobExecutionResultDocumentRepository.save(jobResultDocument);
        LOGGER.info("{} is created successfully.", jobResultDocument);

        return jobResult;
    }*/

    @Override
    public void processBulkJobs(ActionExecutionContext actionExecutionContext) {
        List<Pair<JobDocument, JobResultDocument>> jobDocumentTriplets =
                actionExecutionContext.getJobDocumentPairs();
        jobDocumentTriplets.forEach(pair -> {
            JobDocument jobDocument = pair.getKey();
            JobResultDocument jobResultDocument = pair.getValue();
            processJob(jobDocument, jobResultDocument, actionExecutionContext.getOnCompletedJobCallback());
        });
    }

    @Override
    public void processJob(JobDocument jobDocument, JobResultDocument jobResultDocument, Consumer<JobStatus> callback) {
        if (jobDocument.isAsync()) {
            processAsync(jobDocument, jobResultDocument, callback);
            return;
        }
        processSync(jobDocument, jobResultDocument, callback);
    }

    private void processSync(JobDocument jobDocument, JobResultDocument jobResultDocument, Consumer<JobStatus> callback) {

        long startedAt = DateTimeUtils.getCurrentEpochTimeInSecond();
        jobResultDocument.setStartedAt(startedAt);
        jobResultDocument.setJobStatus(JobStatus.PROCESSING);
        jobExecutionResultDocumentRepository.save(jobResultDocument);

        String jobName = jobDocument.getJobName();
        String templateName = String.format("%s-%s", jobName, jobDocument.getJobCategory());
        String configurations = jobDocument.getConfigurations();
        CheckedSupplier<Map<String, Object>> configurationToMapSupplier =
                () -> objectMapper.readValue(configurations, Map.class);
        Map<String, Object> configurationMap = configurationToMapSupplier.get();

        String defaultTemplateEngine = DEFAULT_CONFIGURATIONS.get(TEMPLATE_ENGINE_NAME);
        String templateEngineName = (String) configurationMap.getOrDefault(TEMPLATE_ENGINE_NAME, defaultTemplateEngine);
        TemplateEngineEnum templateEngine = TemplateEngineEnum.getTemplateEngineFromName(templateEngineName);
        String jobContent = templateEngine.process(templateName, jobDocument.getJobContent(), configurationMap);

        Map<String, Object> jobExecutionContext = new HashMap<>(configurationMap);
        jobExecutionContext.put("templateEngine", templateEngine);
        JobResult jobResult = scriptEngineService.execute(jobContent, jobExecutionContext);
        LOGGER.info("Async job: {} result: {}", jobName, jobResult);

        JobStatus jobStatus = StringUtils.isNotEmpty(jobResult.getException()) ? JobStatus.FAILURE : JobStatus.SUCCESS;
        long endedAt = DateTimeUtils.getCurrentEpochTimeInSecond();
        jobResultDocument.setJobState(JobState.COMPLETED);
        jobResultDocument.setJobStatus(jobStatus);
        jobResultDocument.setEndedAt(endedAt);
        jobResultDocument.setElapsedTime(endedAt - startedAt);
        jobResultDocument.setFailureNotes(jobResult.getException());
        jobExecutionResultDocumentRepository.save(jobResultDocument);
        callback.accept(jobStatus);
    }

    private void processAsync(JobDocument jobDocument, JobResultDocument jobResultDocument,
                              Consumer<JobStatus> callback) {
        taskMgmtServiceV1.execute(() -> {
            processSync(jobDocument, jobResultDocument, callback);

        });
    }

    @Override
    public Pair<JobDocument, JobResultDocument> initial(JobDefinitionDTO jobDefinitionDTO, String actionId) {
        JobDocument jobDocument = jobDocumentRepository.save(JobDocument.fromJobDefinition(jobDefinitionDTO, actionId));
        JobResultDocument.JobResultDocumentBuilder jobResultDocumentBuilder = JobResultDocument.builder()
                .jobState(JobState.INITIAL)
                .jobStatus(JobStatus.PENDING)
                .createdAt(DateTimeUtils.getCurrentEpochTimeInSecond())
                .jobId(jobDocument.getHash());
        JobResultDocument jobResultDocument = jobExecutionResultDocumentRepository.save(jobResultDocumentBuilder.build());
        return Pair.of(jobDocument, jobResultDocument);
    }
}
