package com.hoatv.action.manager.services;

import com.hoatv.action.manager.dtos.JobDefinitionDTO;

public interface JobManagerService {

    JobResult executeJob(JobDefinitionDTO jobDefinitionDTO);
}
