package com.hoatv.action.manager.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoatv.action.manager.api.JobManagerService;
import com.hoatv.action.manager.collections.JobDocument;
import com.hoatv.action.manager.collections.JobResultDocument;
import com.hoatv.action.manager.dtos.*;
import com.hoatv.action.manager.repositories.JobDocumentRepository;
import com.hoatv.action.manager.repositories.JobExecutionResultDocumentRepository;
import com.hoatv.fwk.common.constants.MetricProviders;
import com.hoatv.fwk.common.exceptions.AppException;
import com.hoatv.fwk.common.services.CheckedSupplier;
import com.hoatv.fwk.common.services.TemplateEngineEnum;
import com.hoatv.fwk.common.ultilities.DateTimeUtils;
import com.hoatv.fwk.common.ultilities.Pair;
import com.hoatv.metric.mgmt.annotations.Metric;
import com.hoatv.metric.mgmt.annotations.MetricProvider;
import com.hoatv.metric.mgmt.entities.ComplexValue;
import com.hoatv.metric.mgmt.entities.MetricTag;
import com.hoatv.metric.mgmt.services.MetricService;
import com.hoatv.monitor.mgmt.LoggingMonitor;
import com.hoatv.task.mgmt.services.TaskFactory;
import com.hoatv.task.mgmt.services.TaskMgmtServiceV1;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.AbstractMap.SimpleEntry;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@MetricProvider(application = MetricProviders.OTHER_APPLICATION, category = MetricProviders.MetricCategories.STATS_DATA_CATEGORY)
public class JobManagerServiceImpl implements JobManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobManagerServiceImpl.class);
    public static final int NUMBER_OF_JOB_THREADS = 20;
    public static final String IO_JOB_MANAGER_APPLICATION = "io-job-manager";
    public static final String CPU_JOB_MANAGER_APPLICATION = "cpu-job-manager";
    public static final int MAX_AWAIT_TERMINATION_MILLIS = 5000;

    private final ScriptEngineService scriptEngineService;

    private final ObjectMapper objectMapper;

    private final JobDocumentRepository jobDocumentRepository;

    private final JobExecutionResultDocumentRepository jobExecutionResultDocumentRepository;

    private final TaskMgmtServiceV1 ioTaskMgmtService;
    private final TaskMgmtServiceV1 cpuTaskMgmtService;
    private final MetricService metricService;
    private final JobManagementStatistics jobManagementStatistics;

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
        this.metricService = new MetricService();
        this.jobManagementStatistics = new JobManagementStatistics();
        this.ioTaskMgmtService = TaskFactory.INSTANCE
                .getTaskMgmtServiceV1(NUMBER_OF_JOB_THREADS, MAX_AWAIT_TERMINATION_MILLIS, IO_JOB_MANAGER_APPLICATION);
        int cores = Runtime.getRuntime().availableProcessors();
        this.cpuTaskMgmtService = TaskFactory.INSTANCE
                .getTaskMgmtServiceV1(cores, MAX_AWAIT_TERMINATION_MILLIS, CPU_JOB_MANAGER_APPLICATION);
        this.objectMapper = new ObjectMapper();
    }

    @Metric(name="job-manager")
    public Collection<ComplexValue> getMetricValues() {
        return metricService.getMetrics().values();
    }
    @Metric(name="job-manager-number-of-jobs")
    public long getTotalNumberOfJobs() {
        return jobManagementStatistics.totalNumberOfJobs.get();
    }
    @Metric(name="job-manager-number-of-failure-jobs")
    public long getTotalNumberOfFailureJobs() {
        return jobManagementStatistics.numberOfFailureJobs.get();
    }
    @Metric(name="job-manager-total-number-of-activate-jobs")
    public long getTotalNumberOfActiveJobs() {
        return jobManagementStatistics.numberOfActiveJobs.get();
    }

    @Override
    @LoggingMonitor
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
                    .elapsedTime(DurationFormatUtils.formatDuration(jobStat.getElapsedTime(), "HH:mm:ss.S"))
                    .failureNotes(jobStat.getFailureNotes())
                    .build();
        });
    }

    @Override
    public List<Pair<JobDocument, JobResultDocument>> getJobsFromAction(String actionId) {
        List<JobDocument> jobDocuments = jobDocumentRepository.findJobByActionId(actionId);
        List<String> jobIds = jobDocuments.stream().map(JobDocument::getHash).toList();
        List<JobResultDocument> jobResultDocuments = jobExecutionResultDocumentRepository.findByJobIdIn(jobIds);
        Map<String, JobResultDocument> jobResultMapping = jobResultDocuments.stream()
                .map(p -> new SimpleEntry<>(p.getJobId(), p))
                .collect(Collectors.toMap(p -> p.getKey(), p -> p.getValue()));

        return jobDocuments.stream().map(jobDocument -> Pair.of(jobDocument,
                jobResultMapping.get(jobDocument.getHash()))).toList();
    }

    @Override
    @LoggingMonitor
    public void deleteJobsByActionId(String actionId) {
        jobDocumentRepository.deleteByActionId(actionId);
        jobExecutionResultDocumentRepository.deleteByActionId(actionId);
    }

    @Override
    @LoggingMonitor
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
    @LoggingMonitor
    public void processJob(JobDocument jobDocument, JobResultDocument jobResultDocument, Consumer<JobStatus> callback) {
        if (jobDocument.isAsync()) {
            processAsync(jobDocument, jobResultDocument, callback);
            return;
        }
        processSync(jobDocument, jobResultDocument, callback);
    }

    private void processSync(JobDocument jobDocument, JobResultDocument jobResultDocument, Consumer<JobStatus> callback) {
        jobManagementStatistics.totalNumberOfJobs.incrementAndGet();
        jobManagementStatistics.numberOfActiveJobs.incrementAndGet();
        long startedAt = DateTimeUtils.getCurrentEpochTimeInMillisecond();
        try {
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
            processOutputTargets(jobDocument, jobName, jobResult);

            JobStatus jobStatus = StringUtils.isNotEmpty(jobResult.getException()) ? JobStatus.FAILURE : JobStatus.SUCCESS;
            long endedAt = DateTimeUtils.getCurrentEpochTimeInMillisecond();
            jobResultDocument.setJobState(JobState.COMPLETED);
            jobResultDocument.setJobStatus(jobStatus);
            jobResultDocument.setEndedAt(endedAt);
            jobResultDocument.setElapsedTime(endedAt - startedAt);
            jobResultDocument.setFailureNotes(jobResult.getException());
            jobExecutionResultDocumentRepository.save(jobResultDocument);
            callback.accept(jobStatus);
        } catch (Exception exception) {
            jobManagementStatistics.numberOfFailureJobs.incrementAndGet();
            LOGGER.error("An exception occurred while processing job", exception);
            long endedAt = DateTimeUtils.getCurrentEpochTimeInMillisecond();
            jobResultDocument.setJobState(JobState.COMPLETED);
            jobResultDocument.setJobStatus(JobStatus.FAILURE);
            jobResultDocument.setEndedAt(endedAt);
            jobResultDocument.setElapsedTime(endedAt - startedAt);
            jobResultDocument.setFailureNotes(exception.getMessage());
            jobExecutionResultDocumentRepository.save(jobResultDocument);
        } finally {
            jobManagementStatistics.numberOfActiveJobs.decrementAndGet();
        }
    }

    private void processOutputTargets(JobDocument jobDocument, String jobName, JobResult jobResult) {
        List<String> jobOutputTargets = jobDocument.getOutputTargets();
        jobOutputTargets.forEach(target -> {
           JobOutputTarget jobOutputTarget = JobOutputTarget.valueOf(target);
            switch (jobOutputTarget) {
                case CONSOLE -> LOGGER.info("Async job: {} result: {}", jobName, jobResult);
                case METRIC -> {
                    ArrayList<MetricTag> metricTags = new ArrayList<>();
                    MetricTag metricTag = new MetricTag(jobResult.getData());
                    metricTag.setAttributes(Map.of("name", String.format("job-management-%s", jobName)));
                    metricTags.add(metricTag);
                    metricService.setMetric(jobName, metricTags);
                }
                default -> throw new AppException("Unsupported output target " + target);
            }
        });
    }

    private void processAsync(JobDocument jobDocument, JobResultDocument jobResultDocument,
                              Consumer<JobStatus> callback) {
        Runnable jobProcessRunnable = () -> processSync(jobDocument, jobResultDocument, callback);
        if (jobDocument.getJobCategory() == JobCategory.CPU) {
            LOGGER.info("Using CPU threads to execute job: {}", jobDocument.getJobName());
            cpuTaskMgmtService.execute(jobProcessRunnable);
            return;
        }
        LOGGER.info("Using IO threads to execute job: {}", jobDocument.getJobName());
        ioTaskMgmtService.execute(jobProcessRunnable);
    }

    @Override
    @LoggingMonitor
    public Pair<JobDocument, JobResultDocument> initialJobs(JobDefinitionDTO jobDefinitionDTO, String actionId) {
        JobDocument jobDocument = jobDocumentRepository.save(JobDocument.fromJobDefinition(jobDefinitionDTO, actionId));
        JobResultDocument.JobResultDocumentBuilder jobResultDocumentBuilder = JobResultDocument.builder()
                .jobState(JobState.INITIAL)
                .jobStatus(JobStatus.PENDING)
                .actionId(actionId)
                .createdAt(DateTimeUtils.getCurrentEpochTimeInSecond())
                .jobId(jobDocument.getHash());
        JobResultDocument jobResultDocument = jobExecutionResultDocumentRepository.save(jobResultDocumentBuilder.build());
        return Pair.of(jobDocument, jobResultDocument);
    }

    private static class JobManagementStatistics {

        private final AtomicLong totalNumberOfJobs = new AtomicLong(0);
        private final AtomicLong numberOfActiveJobs = new AtomicLong(0);
        private final AtomicLong numberOfFailureJobs = new AtomicLong(0);
    }
}
