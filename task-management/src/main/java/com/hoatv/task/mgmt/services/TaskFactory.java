package com.hoatv.task.mgmt.services;

import com.hoatv.task.mgmt.annotations.SchedulePoolSettings;
import com.hoatv.task.mgmt.annotations.ThreadPoolSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public enum TaskFactory {

    INSTANCE;

    private static final Logger APP_LOGGER = LoggerFactory.getLogger(TaskFactory.class);

    final Map<String, LinkedList<CloseableTask>> serviceRegistry = new ConcurrentHashMap<>();
    final Map<String, LinkedList<CloseableTaskV1>> serviceRegistryV1 = new ConcurrentHashMap<>();

    public TaskMgmtServiceV1 getTaskMgmtServiceV1(int numberOfThreads, int maxAwaitTerminationMillis, String application) {
        serviceRegistryV1.putIfAbsent(application, new LinkedList<>());
        LinkedList<CloseableTaskV1> services = serviceRegistryV1.get(application);
        TaskMgmtServiceV1 taskMgmtServiceV1 = new TaskMgmtServiceV1(numberOfThreads, maxAwaitTerminationMillis, application);
        services.add(taskMgmtServiceV1);
        return taskMgmtServiceV1;
    }

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

        TaskMgmtService taskMgmtService = new TaskMgmtService(numberOfThreads, maxAwaitTerminationMillis, application);
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
        ThreadPoolSettings threadPoolSettings = schedulePoolSettings.threadPoolSettings();
        return newScheduleTaskMgmtService(schedulePoolSettings.application(),
                threadPoolSettings.numberOfThreads(),
                threadPoolSettings.maxAwaitTerminationMillis());
    }

    public ScheduleTaskMgmtService newScheduleTaskMgmtService(String application, int numberOfThreads, int maxAwaitTerminationMillis) {
        serviceRegistry.putIfAbsent(application, new LinkedList<>());
        LinkedList<? super CloseableTask> services = serviceRegistry.get(application);
        ScheduleTaskMgmtService scheduleTaskMgmtService = new ScheduleTaskMgmtService(application, numberOfThreads, maxAwaitTerminationMillis);
        services.add(scheduleTaskMgmtService);
        return scheduleTaskMgmtService;
    }

    public void destroy(String application, TaskMgmtService taskMgmtService) {
        APP_LOGGER.info("Destroy registered executor under application: {}", application);
        Optional<LinkedList<CloseableTask>> linkedListStreamOp = serviceRegistry.entrySet().stream()
                .filter(p -> p.getKey().equals(application))
                .findFirst()
                .map(Map.Entry::getValue);
        if (linkedListStreamOp.isPresent()) {
            LinkedList<CloseableTask> linkedListStream = linkedListStreamOp.get();
            linkedListStream
                    .stream()
                    .filter(taskMgmtService::equals)
                    .filter(p -> !p.isClosed())
                    .forEach(CloseableTask::shutdownNow);
            linkedListStream.remove(taskMgmtService);
        }
    }

    public void destroy(String application, TaskMgmtServiceV1 taskMgmtService) {
        Optional<LinkedList<CloseableTaskV1>> linkedListStreamOp = serviceRegistryV1.entrySet().stream()
                .filter(p -> p.getKey().equals(application))
                .findFirst()
                .map(Map.Entry::getValue);

        if (linkedListStreamOp.isPresent()) {
            LinkedList<CloseableTaskV1> linkedListStream = linkedListStreamOp.get();
            linkedListStream
                    .stream()
                    .filter(taskMgmtService::equals)
                    .filter(p -> !p.isClosed())
                    .forEach(CloseableTaskV1::shutdownNow);
            linkedListStream.remove(taskMgmtService);
        }
    }

    public void destroy(String application) {
        APP_LOGGER.info("Destroy all registered executors in both v0 and v1 under application: {}", application);
        serviceRegistry.entrySet().stream()
                .filter(p -> p.getKey().equals(application))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .filter(p -> !p.isClosed())
                .forEach(CloseableTask::shutdownNow);
        serviceRegistry.remove(application);

        serviceRegistryV1.entrySet().stream()
                .filter(p -> p.getKey().equals(application))
                .map(Map.Entry::getValue)
                .flatMap(Collection::stream)
                .filter(p -> !p.isClosed())
                .forEach(CloseableTaskV1::shutdownNow);
        serviceRegistryV1.remove(application);
    }

    public void destroy() {
        APP_LOGGER.info("Destroy all registered executors");
        serviceRegistry.values().stream()
                .flatMap(Collection::stream)
                .filter(p -> !p.isClosed())
                .forEach(CloseableTask::shutdownNow);
        serviceRegistry.clear();
        serviceRegistryV1.values().stream()
                .flatMap(Collection::stream)
                .filter(p -> !p.isClosed())
                .forEach(CloseableTaskV1::shutdownNow);
        serviceRegistryV1.clear();
    }
}
