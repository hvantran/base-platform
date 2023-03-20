package com.hoatv.action.manager.api;

import com.hoatv.action.manager.dtos.JobDefinitionDTO;
import com.hoatv.action.manager.dtos.JobOverviewDTO;
import com.hoatv.action.manager.services.JobResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

public interface JobManagerService {

    JobResult executeJob(JobDefinitionDTO jobDefinitionDTO, String actionId);
    JobResult executeJob(JobDefinitionDTO jobDefinitionDTO);

    Page<JobOverviewDTO> getJobsFromAction(String actionId, PageRequest pageRequest);
}
