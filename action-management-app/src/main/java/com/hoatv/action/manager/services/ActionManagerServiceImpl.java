package com.hoatv.action.manager.services;

import com.hoatv.action.manager.api.ActionManagerService;
import com.hoatv.action.manager.api.JobManagerService;
import com.hoatv.action.manager.collections.ActionDocument;
import com.hoatv.action.manager.collections.ActionStatisticsDocument;
import com.hoatv.action.manager.collections.ActionStatisticsDocument.ActionStatisticsDocumentBuilder;
import com.hoatv.action.manager.dtos.ActionDefinitionDTO;
import com.hoatv.action.manager.dtos.ActionOverviewDTO;
import com.hoatv.action.manager.dtos.JobDefinitionDTO;
import com.hoatv.action.manager.repositories.ActionDocumentRepository;
import com.hoatv.action.manager.repositories.ActionStatisticsDocumentRepository;
import com.hoatv.fwk.common.services.CheckedFunction;
import com.hoatv.fwk.common.ultilities.DateTimeUtils;
import com.hoatv.fwk.common.ultilities.ObjectUtils;
import com.hoatv.monitor.mgmt.LoggingMonitor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
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
    public Page<ActionOverviewDTO> getAllActionsWithPaging(String search, Pageable pageable) {
        Page<ActionDocument> actionDocuments = actionDocumentRepository.findActionByName(search, pageable);
        return getActionOverviewDTOS(actionDocuments);
    }

    @Override
    public Page<ActionOverviewDTO> getAllActionsWithPaging(Pageable pageable) {
        Page<ActionDocument> actionDocuments = actionDocumentRepository.findAll(pageable);

        return getActionOverviewDTOS(actionDocuments);
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

    @Override
    public Optional<ActionDefinitionDTO> getActionById(String hash) {
        return actionDocumentRepository.findById(hash).map(ActionDocument::toActionDefinition);
    }

    @Override
    @LoggingMonitor
    public String executeAction(ActionDefinitionDTO actionDefinition) {
        ActionDocument actionDocument = actionDocumentRepository.save(ActionDocument.fromActionDefinition(actionDefinition));

        ActionStatisticsDocumentBuilder statisticsDocumentBuilder = ActionStatisticsDocument.builder();
        statisticsDocumentBuilder.createdAt(DateTimeUtils.getCurrentEpochTimeInSecond());
        statisticsDocumentBuilder.actionId(actionDocument.getHash());
        statisticsDocumentBuilder.numberOfJobs(actionDefinition.getJobs().size());

        String actionName = actionDefinition.getActionName();
        LOGGER.info("A new action: {} is stored successfully.", actionName);

        LOGGER.info("Processing the nested jobs inside {} action", actionName);
        List<JobDefinitionDTO> definitionJobs = actionDefinition.getJobs();
        CheckedFunction<JobDefinitionDTO, JobResult> checkedJobProcessingFunction =
                jobDefinitionDTO -> jobManagerService.executeJob(jobDefinitionDTO, actionDocument.getHash());

        List<JobResult> jobResults = definitionJobs.stream()
                .map(checkedJobProcessingFunction)
                .toList();

        Predicate<JobResult> failureJobPredicate = jobResult -> StringUtils.isNotEmpty(jobResult.getException());
        long numberOfFailureJobs = jobResults.stream().filter(failureJobPredicate).count();

        statisticsDocumentBuilder.numberOfFailureJobs(numberOfFailureJobs);
        long numberOfCompletedJobs = jobResults.size() - numberOfFailureJobs;
        statisticsDocumentBuilder.numberOfSuccessJobs(numberOfCompletedJobs);
        statisticsDocumentBuilder.percentCompleted((double) jobResults.size() * 100 / jobResults.size());
        LOGGER.info("All nested jobs inside {} action are processed", actionName);

        ActionStatisticsDocument actionStatisticsDocument = statisticsDocumentBuilder.build();
        actionStatisticsDocumentRepository.save(actionStatisticsDocument);
        LOGGER.info("Statistics for action {} are saved successfully", actionName);
        return actionDocument.getHash();
    }
}
