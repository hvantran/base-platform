package com.hoatv.action.manager.services;

import com.hoatv.action.manager.api.ActionManagerService;
import com.hoatv.action.manager.api.JobManagerService;
import com.hoatv.action.manager.collections.ActionDocument;
import com.hoatv.action.manager.collections.ActionStatisticsDocument;
import com.hoatv.action.manager.collections.ActionStatisticsDocument.ActionStatisticsDocumentBuilder;
import com.hoatv.action.manager.collections.JobDocument;
import com.hoatv.action.manager.collections.JobResultDocument;
import com.hoatv.action.manager.dtos.ActionDefinitionDTO;
import com.hoatv.action.manager.dtos.ActionOverviewDTO;
import com.hoatv.action.manager.dtos.JobDefinitionDTO;
import com.hoatv.action.manager.dtos.JobStatus;
import com.hoatv.action.manager.repositories.ActionDocumentRepository;
import com.hoatv.action.manager.repositories.ActionStatisticsDocumentRepository;
import com.hoatv.fwk.common.services.CheckedConsumer;
import com.hoatv.fwk.common.services.CheckedFunction;
import com.hoatv.fwk.common.ultilities.DateTimeUtils;
import com.hoatv.fwk.common.ultilities.Pair;
import com.hoatv.monitor.mgmt.LoggingMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
public class ActionManagerServiceImpl implements ActionManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionManagerServiceImpl.class);

    private final ActionDocumentRepository actionDocumentRepository;

    private final ActionStatisticsDocumentRepository actionStatisticsDocumentRepository;

    private final JobManagerService jobManagerService;

    @Autowired
    public ActionManagerServiceImpl(ActionDocumentRepository actionDocumentRepository,
                                    JobManagerService jobManagerService,
                                    ActionStatisticsDocumentRepository actionStatisticsDocumentRepository)  {
        this.actionDocumentRepository = actionDocumentRepository;
        this.jobManagerService = jobManagerService;
        this.actionStatisticsDocumentRepository = actionStatisticsDocumentRepository;
    }

    @Override
    @LoggingMonitor
    public Page<ActionOverviewDTO> getAllActionsWithPaging(String search, Pageable pageable) {
        Page<ActionDocument> actionDocuments = actionDocumentRepository.findActionByName(search, pageable);
        return getActionOverviewDTOS(actionDocuments);
    }

    @Override
    @LoggingMonitor
    public Page<ActionOverviewDTO> getAllActionsWithPaging(Pageable pageable) {
        Page<ActionDocument> actionDocuments = actionDocumentRepository.findAll(pageable);

        return getActionOverviewDTOS(actionDocuments);
    }
    @Override
    @LoggingMonitor
    public Optional<ActionDefinitionDTO> getActionById(String hash) {
        return actionDocumentRepository.findById(hash).map(ActionDocument::toActionDefinition);
    }

    @Override
    @LoggingMonitor
    public String processAction(ActionDefinitionDTO actionDefinition) {
        ActionExecutionContext actionExecutionContext = initial(actionDefinition);
        jobManagerService.processBulkJobs(actionExecutionContext);
        return actionExecutionContext.getActionDocument().getHash();
    }

    @Override
    @LoggingMonitor
    public void deleteAction(String hash) {
        actionDocumentRepository.deleteById(hash);
        jobManagerService.deleteJobsByActionId(hash);
    }

    private Page<ActionOverviewDTO> getActionOverviewDTOS(Page<ActionDocument> actionDocuments) {
        Set<String> actionIds = actionDocuments.stream().map(ActionDocument::getHash).collect(Collectors.toSet());
        List<ActionStatisticsDocument> actionStatics = actionStatisticsDocumentRepository.findByActionIdIn(actionIds);

        return actionDocuments.map(actionDocument -> {
            String actionId = actionDocument.getHash();
            Optional<ActionStatisticsDocument> actionStatistic =
                    actionStatics.stream().filter(p -> p.getActionId().equals(actionId)).findFirst();

            Supplier<ActionStatisticsDocument> defaultActionStat = () -> ActionStatisticsDocument.builder().build();
            ActionStatisticsDocument actionStat = actionStatistic.orElseGet(defaultActionStat);
            return ActionOverviewDTO.builder()
                    .name(actionDocument.getActionName())
                    .hash(actionId)
                    .numberOfFailureJobs(actionStat.getNumberOfFailureJobs())
                    .numberOfJobs(actionStat.getNumberOfJobs())
                    .createdAt(actionDocument.getCreatedAt())
                    .numberOfSuccessJobs(actionStat.getNumberOfSuccessJobs())
                    .build();
        });
    }

    private ActionExecutionContext initial(ActionDefinitionDTO actionDefinition) {
        ActionDocument actionDocument = actionDocumentRepository.save(ActionDocument.fromActionDefinition(actionDefinition));
        LOGGER.info("ActionExecutionContext: actionDocument - {}", actionDocument);

        ActionStatisticsDocumentBuilder statisticsDocumentBuilder = ActionStatisticsDocument.builder();
        statisticsDocumentBuilder.createdAt(DateTimeUtils.getCurrentEpochTimeInSecond());
        statisticsDocumentBuilder.actionId(actionDocument.getHash());
        statisticsDocumentBuilder.numberOfJobs(actionDefinition.getJobs().size());
        ActionStatisticsDocument actionStatisticsDocument =
                actionStatisticsDocumentRepository.save(statisticsDocumentBuilder.build());
        LOGGER.info("ActionExecutionContext: actionStatisticsDocument - {}", actionStatisticsDocument);

        List<JobDefinitionDTO> definitionJobs = actionDefinition.getJobs();
        CheckedFunction<JobDefinitionDTO, Pair<JobDocument, JobResultDocument>> initialJobFunction =
                jobDefinitionDTO -> jobManagerService.initial(jobDefinitionDTO, actionDocument.getHash());
        List<Pair<JobDocument, JobResultDocument>> jobDocumentPairs = definitionJobs.stream()
                .map(initialJobFunction)
                .toList();
        LOGGER.info("ActionExecutionContext: jobDocumentPairs - {}", jobDocumentPairs);

        CheckedConsumer<JobStatus> onCompletedJobCallback = onCompletedJobCallback(actionStatisticsDocument);

        return ActionExecutionContext.builder()
                .actionDocument(actionDocument)
                .actionStatisticsDocument(actionStatisticsDocument)
                .jobDocumentPairs(jobDocumentPairs)
                .onCompletedJobCallback(onCompletedJobCallback)
                .build();
    }

    private CheckedConsumer<JobStatus> onCompletedJobCallback(ActionStatisticsDocument actionStatisticsDocument) {
        return jobStatus -> {

            switch (jobStatus) {
                case SUCCESS:
                    long numberOfSuccessJobs = actionStatisticsDocument.getNumberOfSuccessJobs();
                    actionStatisticsDocument.setNumberOfSuccessJobs(numberOfSuccessJobs + 1);
                    break;
                case FAILURE:
                default:
                    long numberOfFailureJobs = actionStatisticsDocument.getNumberOfFailureJobs();
                    actionStatisticsDocument.setNumberOfFailureJobs(numberOfFailureJobs + 1);
                    break;
            }
            long currentProcessedJobs =
                    actionStatisticsDocument.getNumberOfFailureJobs() + actionStatisticsDocument.getNumberOfSuccessJobs();
            actionStatisticsDocument.setPercentCompleted((double) currentProcessedJobs * 100 / actionStatisticsDocument.getNumberOfJobs());
            actionStatisticsDocumentRepository.save(actionStatisticsDocument);
        };
    }
}
