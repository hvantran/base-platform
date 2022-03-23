package com.hoatv.task.mgmt.services;

import com.hoatv.task.mgmt.annotations.SchedulePoolSettings;
import com.hoatv.task.mgmt.annotations.ThreadPoolSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public enum TaskFactory {

    INSTANCE;

    private static final Logger APP_LOGGER = LoggerFactory.getLogger(TaskFactory.class);

    private final Map<String, LinkedList<CloseableTask>> serviceRegistry = new ConcurrentHashMap<>();

    public TaskMgmtService getTaskMgmtService(int numberOfThreads, int maxAwaitTerminationMillis) {
        serviceRegistry.putIfAbsent("TaskMgmtService", new LinkedList<>());
        LinkedList<? super CloseableTask> services = serviceRegistry.get("TaskMgmtService");
        TaskMgmtService taskMgmtService = new TaskMgmtService(numberOfThreads, maxAwaitTerminationMillis);
        services.add(taskMgmtService);
        return taskMgmtService;
    }

    public TaskMgmtService getTaskMgmtService(int numberOfThreads, int maxAwaitTerminationMillis, String application) {
        serviceRegistry.putIfAbsent(application, new LinkedList<>());
        LinkedList<? super CloseableTask> services = serviceRegistry.get(application);

        TaskMgmtService  taskMgmtService = new TaskMgmtService(numberOfThreads, maxAwaitTerminationMillis, application);
        services.add(taskMgmtService);
        return taskMgmtService;
    }

    public TaskMgmtService getTaskMgmtService(ThreadPoolSettings threadPoolSettings) {
        serviceRegistry.putIfAbsent(threadPoolSettings.name(), new LinkedList<>());
        LinkedList<? super CloseableTask> services = serviceRegistry.get(threadPoolSettings.name());
        TaskMgmtService taskMgmtService = new TaskMgmtService(threadPoolSettings);
        services.add(taskMgmtService);
        return taskMgmtService;
    }

    public ScheduleTaskMgmtService newScheduleTaskMgmtService(SchedulePoolSettings schedulePoolSettings) {
        serviceRegistry.putIfAbsent(schedulePoolSettings.application(), new LinkedList<>());
        LinkedList<? super CloseableTask> services = serviceRegistry.get(schedulePoolSettings.application());
        ScheduleTaskMgmtService scheduleTaskMgmtService = new ScheduleTaskMgmtService(schedulePoolSettings);
        services.add(scheduleTaskMgmtService);
        return scheduleTaskMgmtService;
    }

    public void destroy() {
        APP_LOGGER.info("Destroy all registered executors");
        serviceRegistry.values().stream()
                .flatMap(Collection::stream)
                .filter(p -> !p.isClosed())
                .forEach(CloseableTask::shutdownNow);
    }
}
