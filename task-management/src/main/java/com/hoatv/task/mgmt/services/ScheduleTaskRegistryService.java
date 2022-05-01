package com.hoatv.task.mgmt.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ScheduleTaskRegistryService {

    private static final Logger APP_LOGGER = LoggerFactory.getLogger(ScheduleTaskRegistryService.class);

    private final Map<String, ScheduleTaskMgmtService> scheduleTaskMgmtExecutors = new ConcurrentHashMap<>();

    public void register(String application, ScheduleTaskMgmtService scheduleTaskMgmtService) {
        APP_LOGGER.info("Registering a executor for application: {}", application);
        scheduleTaskMgmtExecutors.put(application, scheduleTaskMgmtService);
    }

    public ScheduleTaskMgmtService getScheduleTaskMgmtService(String application) {
        return scheduleTaskMgmtExecutors.get(application);
    }

    public void destroy() {
        APP_LOGGER.info("Destroy all registered executors");
        scheduleTaskMgmtExecutors.values().forEach(CloseableTask::shutdownNow);
    }
}
