package com.hoatv.task.mgmt.services;

import com.hoatv.task.mgmt.annotations.SchedulePoolSettings;
import com.hoatv.task.mgmt.annotations.ThreadPoolSettings;
import com.hoatv.task.mgmt.entities.TaskCollection;
import com.hoatv.task.mgmt.entities.TaskEntry;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

@Getter
public class ScheduleTaskMgmtService extends CloseableTask {

    private static final Logger APP_LOGGER = LoggerFactory.getLogger(ScheduleTaskMgmtService.class);

    ScheduleTaskMgmtService(SchedulePoolSettings schedulePoolSettings) {
        super(schedulePoolSettings.threadPoolSettings());
        ThreadPoolSettings threadPoolSettings = schedulePoolSettings.threadPoolSettings();
        int corePoolSize = threadPoolSettings.numberOfThreads();
        String application = schedulePoolSettings.application();
        this.executorService = Executors.newScheduledThreadPool(corePoolSize, new AbmThreadFactory(application));
    }

    public Future<?> scheduleTask(TaskEntry taskEntry, int waitingTime, TimeUnit timeUnit) {
        if (concurrentAccountLocks.tryAcquire(taskEntry, waitingTime, timeUnit)) {
            APP_LOGGER.info("Schedule one time execution task - {} under application - {}", taskEntry.getName(),
                    taskEntry.getApplicationName());
            TaskWorker<?> taskWorker = TaskWorkerFactory.getTaskWorker((Callable<?>) taskEntry.getTaskHandler(),
                    concurrentAccountLocks, taskEntry);
            ScheduledExecutorService scheduledExecutorService = (ScheduledExecutorService) this.executorService;
            return scheduledExecutorService.schedule(taskWorker, taskEntry.getDelayInMillis(), TimeUnit.MILLISECONDS);
        }
        return null;
    }

    public void scheduleFixedRateTask(TaskEntry taskEntry, int waitingTime, TimeUnit timeUnit) {
        if (concurrentAccountLocks.tryAcquire(taskEntry, waitingTime, timeUnit)) {
            APP_LOGGER.info(
                    "Schedule period task - {} under application - {}, delay - {} milliseconds, period - {} milliseconds",
                    taskEntry.getName(), taskEntry.getApplicationName(), taskEntry.getDelayInMillis(), taskEntry.getPeriodInMillis());
            RunnableWorker runnableWorker = TaskWorkerFactory.getRunnableWorker(taskEntry.getTaskHandler(),
                    concurrentAccountLocks, taskEntry.getPeriodInMillis(), taskEntry);
            ScheduledExecutorService scheduledExecutorService = (ScheduledExecutorService) this.executorService;
            scheduledExecutorService.scheduleAtFixedRate(runnableWorker, taskEntry.getDelayInMillis(), taskEntry.getPeriodInMillis(), TimeUnit.MILLISECONDS);
        }
    }

    public void scheduleTasks(TaskCollection taskCollection, int waitingTime, TimeUnit timeUnit) {
        TaskMgmtService taskMgmtService = new TaskMgmtService(1, 5000);
        TaskEntry applicationTask = new TaskEntry();
        applicationTask.setApplicationName(taskCollection.getApplicationName());
        Callable<Object> taskHandler = getTaskHandler(taskCollection, waitingTime, timeUnit);
        applicationTask.setTaskHandler(taskHandler);
        taskMgmtService.execute(applicationTask);
    }

    private Callable<Object> getTaskHandler(TaskCollection taskCollection, int waitingTime, TimeUnit timeUnit) {
        return () -> {
            List<TaskEntry> taskEntries = taskCollection.getTaskEntries();
            for (TaskEntry taskEntry : taskEntries) {
                if (taskEntry.getPeriodInMillis() > 0) {
                    scheduleFixedRateTask(taskEntry, waitingTime, timeUnit);
                    continue;
                }
                scheduleTask(taskEntry, waitingTime, timeUnit);
            }
            return null;
        };
    }
}
