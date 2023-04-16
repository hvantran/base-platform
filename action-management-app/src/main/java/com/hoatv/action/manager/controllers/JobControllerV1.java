package com.hoatv.action.manager.controllers;


import com.hoatv.action.manager.api.JobManagerService;
import com.hoatv.action.manager.dtos.JobOverviewDTO;
import com.hoatv.monitor.mgmt.LoggingMonitor;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/v1/jobs", produces = MediaType.APPLICATION_JSON_VALUE)
public class JobControllerV1 {

    private JobManagerService jobManagerService;

    @Autowired
    public JobControllerV1(JobManagerService jobManagerService) {
        this.jobManagerService = jobManagerService;
    }

    @LoggingMonitor
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getJobsFromAction(@RequestParam("actionId") String actionId,
                                               @RequestParam("pageIndex") @Min(0) int pageIndex,
                                               @RequestParam("pageSize") @Min(0) int pageSize) {
        Page<JobOverviewDTO> actionResults =
                jobManagerService.getJobsFromAction(actionId, PageRequest.of(pageIndex, pageSize));
        return ResponseEntity.ok(actionResults);
    }
}
