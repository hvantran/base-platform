package com.hoatv.action.manager.services;

import com.hoatv.action.manager.collections.ActionDocument;
import com.hoatv.action.manager.collections.ActionStatisticsDocument;
import com.hoatv.action.manager.collections.ActionStatisticsDocument.ActionStatisticsDocumentBuilder;
import com.hoatv.action.manager.dtos.ActionDefinitionDTO;
import com.hoatv.action.manager.dtos.JobDefinitionDTO;
import com.hoatv.action.manager.repositories.ActionDocumentRepository;
import com.hoatv.action.manager.repositories.ActionStatisticsDocumentRepository;
import com.hoatv.fwk.common.services.CheckedConsumer;
import com.hoatv.fwk.common.services.CheckedFunction;
import com.hoatv.fwk.common.ultilities.DateTimeUtils;
import com.hoatv.monitor.mgmt.LoggingMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class ActionManagerServiceImpl implements ActionManagerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActionManagerServiceImpl.class);

    private ActionDocumentRepository actionDocumentRepository;

    private ActionStatisticsDocumentRepository actionStatisticsDocumentRepository;

    private JobManagerService jobManagerService;

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
        CheckedFunction<JobDefinitionDTO, JobResult> checkedJobProcessingFunction = jobManagerService::executeJob;

        List<JobResult> jobResults = definitionJobs.stream()
                .map(checkedJobProcessingFunction)
                .collect(Collectors.toList());

        Predicate<JobResult> failureJobPredicate = jobResult -> Objects.nonNull(jobResult.getException());
        long numberOfFailureJobs = jobResults.stream().filter(failureJobPredicate).count();

        statisticsDocumentBuilder.numberOfFailureJobs(numberOfFailureJobs);
        long numberOfCompletedJobs = jobResults.size() - numberOfFailureJobs;
        statisticsDocumentBuilder.percentCompleted((double) numberOfCompletedJobs / jobResults.size());
        LOGGER.info("All nested jobs inside {} action are processed", actionName);

        ActionStatisticsDocument actionStatisticsDocument = statisticsDocumentBuilder.build();
        actionStatisticsDocumentRepository.save(actionStatisticsDocument);
        LOGGER.info("Statistics for action {} are saved successfully", actionName);
        return "TODO";
    }
}
