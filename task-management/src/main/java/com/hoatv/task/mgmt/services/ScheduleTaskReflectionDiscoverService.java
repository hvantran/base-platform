package com.hoatv.task.mgmt.services;

import com.hoatv.task.mgmt.annotations.SchedulePoolSettings;
import com.hoatv.task.mgmt.entities.TaskCollection;
import com.hoatv.task.mgmt.entities.TaskEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.hoatv.fwk.common.constants.Constants.DEFAULT_SCAN_PACKAGE;

public class ScheduleTaskReflectionDiscoverService {

    private static final Logger APP_LOGGER = LoggerFactory.getLogger(ScheduleTaskReflectionDiscoverService.class);

    private TaskReflectionDiscoverService taskDiscover;

    private ScheduleTaskReflectionDiscoverService(TaskReflectionDiscoverService taskDiscover) {
        this.taskDiscover = taskDiscover;
    }

    public void init() {
        APP_LOGGER.info("Collected all schedule task collections");
        Set<Class<?>> schedulePoolSettings = taskDiscover.getSchedulePoolSettings(DEFAULT_SCAN_PACKAGE);
        List<TaskCollection> taskCollection = taskDiscover.getTaskCollection(DEFAULT_SCAN_PACKAGE);
        TaskMgmtService<Void> taskMgmtService = new TaskMgmtService<>(0, 5000);
        Consumer<Class<?>> scheduleApplicationTasks = scheduleApplicationTasks(taskCollection, taskMgmtService);
        schedulePoolSettings.forEach(scheduleApplicationTasks);
    }

    private Consumer<Class<?>> scheduleApplicationTasks(
            List<TaskCollection> taskCollection, TaskMgmtService<Void> taskMgmtService) {

        return schedulePool -> {
            TaskEntry applicationTask = new TaskEntry();
            applicationTask.setApplicationName("Root Application Schedule Task");
            Callable<Object> taskHandler = getTaskHandler(SchedulePoolSettings.class, taskCollection, schedulePool);
            applicationTask.setTaskHandler(taskHandler);
            taskMgmtService.resize(1);
            taskMgmtService.execute(applicationTask);
        };
    }

    private Callable<Object> getTaskHandler(Class<SchedulePoolSettings> schedulePoolSettingsClass,
            List<TaskCollection> taskCollection, Class<?> schedulePool) {
        return () -> {
            SchedulePoolSettings schedulePoolConfig = schedulePool.getAnnotation(schedulePoolSettingsClass);
            String application = schedulePoolConfig.application();
            int threadCount = schedulePoolConfig.threadPoolSettings().numberOfThreads();

            APP_LOGGER.info("Creating schedule tasks service: application - {}, thread count - {}", application, threadCount);
            try (ScheduleTaskMgmtService scheduleTaskMgmtService = new ScheduleTaskMgmtService(schedulePoolConfig)) {
                APP_LOGGER.info("Getting schedule tasks for application {}", application);
                Predicate<TaskCollection> taskEntryPredicate = tc -> application.equals(tc.getApplicationName());
                List<TaskEntry> applicationScheduleTasks = taskCollection.stream()
                        .filter(taskEntryPredicate)
                        .map(TaskCollection::getTaskEntries)
                        .findFirst()
                        .orElseThrow();
                TaskCollection applicationTaskCollection = new TaskCollection(application, applicationScheduleTasks);
                scheduleTaskMgmtService.scheduleTasks(applicationTaskCollection, 5, TimeUnit.SECONDS);
            }
            return null;
        };
    }
}
