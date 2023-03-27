package com.hoatv.action.manager.api;

import com.hoatv.action.manager.collections.JobDocument;
import com.hoatv.action.manager.collections.JobResultDocument;
import com.hoatv.action.manager.dtos.JobDefinitionDTO;
import com.hoatv.action.manager.dtos.JobOverviewDTO;
import com.hoatv.action.manager.dtos.JobStatus;
import com.hoatv.action.manager.services.ActionExecutionContext;
import com.hoatv.fwk.common.ultilities.Pair;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.function.Consumer;

public interface JobManagerService {

    void deleteJobsByActionId(String actionId);

    void processBulkJobs(ActionExecutionContext actionExecutionContext);
    Page<JobOverviewDTO> getJobsFromAction(String actionId, PageRequest pageRequest);

    Pair<JobDocument, JobResultDocument> initialJobs(JobDefinitionDTO jobDefinitionDTO, String actionId);
    List<Pair<JobDocument, JobResultDocument>> getJobsFromAction(String actionId);
    void processJob(JobDocument jobDocument, JobResultDocument jobResultDocument, Consumer<JobStatus> callback);
}
