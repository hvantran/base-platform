package com.hoatv.task.mgmt.services;

import com.hoatv.task.mgmt.annotations.SchedulePoolSettings;
import com.hoatv.task.mgmt.annotations.ThreadPoolSettings;
import com.hoatv.task.mgmt.entities.TaskEntry;
import lombok.*;

import java.util.concurrent.*;

@Getter
public class ScheduleTaskMgmtService extends CloseableTask {

    public ScheduleTaskMgmtService(SchedulePoolSettings schedulePoolSettings) {
        super(schedulePoolSettings.threadPoolSettings());
        ThreadPoolSettings threadPoolSettings = schedulePoolSettings.threadPoolSettings();
        int corePoolSize = threadPoolSettings.numberOfThreads();
        String application = schedulePoolSettings.application();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(corePoolSize, new AbmThreadFactory(application));
        this.executorService = executorService;
    }

    public Future<?> scheduleTask(TaskEntry taskEntry) {
        concurrentAccountLocks.acquire(taskEntry);
        TaskWorker<?> taskWorker = TaskWorkerFactory.getTaskWorker((Callable<?>) taskEntry.getTaskHandler(), concurrentAccountLocks, taskEntry);
        ScheduledExecutorService scheduledExecutorService = (ScheduledExecutorService) this.executorService;
        return scheduledExecutorService.schedule(taskWorker, taskEntry.getDelayInMillis(), TimeUnit.MILLISECONDS);
    }

    public void scheduleFixedRateTask(TaskEntry taskEntry) {
        concurrentAccountLocks.acquire(taskEntry);
        RunnableWorker runnableWorker = TaskWorkerFactory.getRunnableWorker(taskEntry.getTaskHandler(),
                concurrentAccountLocks, taskEntry.getPeriodInMillis(), taskEntry);
        ScheduledExecutorService scheduledExecutorService = (ScheduledExecutorService) this.executorService;
        scheduledExecutorService.scheduleAtFixedRate(runnableWorker, taskEntry.getDelayInMillis(), taskEntry.getPeriodInMillis(), TimeUnit.MILLISECONDS);
    }
}
