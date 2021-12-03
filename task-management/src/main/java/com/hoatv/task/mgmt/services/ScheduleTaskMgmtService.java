package com.hoatv.task.mgmt.services;

import com.hoatv.task.mgmt.annotations.SchedulePoolSettings;
import com.hoatv.task.mgmt.annotations.ThreadPoolSettings;
import com.hoatv.task.mgmt.entities.TaskEntry;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

@Getter
public class ScheduleTaskMgmtService extends CloseableTask {

    private static final Logger APP_LOGGER = LoggerFactory.getLogger(ScheduleTaskMgmtService.class);

    public ScheduleTaskMgmtService(SchedulePoolSettings schedulePoolSettings) {
        super(schedulePoolSettings.threadPoolSettings());
        ThreadPoolSettings threadPoolSettings = schedulePoolSettings.threadPoolSettings();
        int corePoolSize = threadPoolSettings.numberOfThreads();
        String application = schedulePoolSettings.application();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(corePoolSize, new AbmThreadFactory(application));
        this.executorService = executorService;
    }

    public Future<?> scheduleTask(TaskEntry taskEntry) {
        if (concurrentAccountLocks.tryAcquire(taskEntry)) {
            APP_LOGGER.info("Schedule one time execution task - {} under application - {}", taskEntry.getName(),
                    taskEntry.getApplicationName());
            TaskWorker<?> taskWorker = TaskWorkerFactory.getTaskWorker((Callable<?>) taskEntry.getTaskHandler(),
                    concurrentAccountLocks, taskEntry);
            ScheduledExecutorService scheduledExecutorService = (ScheduledExecutorService) this.executorService;
            return scheduledExecutorService.schedule(taskWorker, taskEntry.getDelayInMillis(), TimeUnit.MILLISECONDS);
        }
        return null;
    }

    public void scheduleFixedRateTask(TaskEntry taskEntry) {
        if (concurrentAccountLocks.tryAcquire(taskEntry)) {
            APP_LOGGER.info(
                    "Schedule period task - {} under application - {}, delay - {} milliseconds, period - {} milliseconds",
                    taskEntry.getName(), taskEntry.getApplicationName(), taskEntry.getDelayInMillis(), taskEntry.getPeriodInMillis());
            RunnableWorker runnableWorker = TaskWorkerFactory.getRunnableWorker(taskEntry.getTaskHandler(),
                    concurrentAccountLocks, taskEntry.getPeriodInMillis(), taskEntry);
            ScheduledExecutorService scheduledExecutorService = (ScheduledExecutorService) this.executorService;
            scheduledExecutorService.scheduleAtFixedRate(runnableWorker, taskEntry.getDelayInMillis(), taskEntry.getPeriodInMillis(), TimeUnit.MILLISECONDS);
        }
    }
}
