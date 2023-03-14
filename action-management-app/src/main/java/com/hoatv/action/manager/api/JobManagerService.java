package com.hoatv.action.manager.api;

import com.hoatv.action.manager.dtos.JobDefinitionDTO;
import com.hoatv.action.manager.services.JobResult;

public interface JobManagerService {

    JobResult executeJob(JobDefinitionDTO jobDefinitionDTO, String actionId);
    JobResult executeJob(JobDefinitionDTO jobDefinitionDTO);
}
