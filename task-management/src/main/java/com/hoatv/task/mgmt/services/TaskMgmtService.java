package com.hoatv.task.mgmt.services;

import com.hoatv.task.mgmt.annotations.ThreadPoolSettings;
import com.hoatv.task.mgmt.entities.TaskEntry;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

@Getter
public class TaskMgmtService<T> extends CloseableTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskMgmtService.class);

    public TaskMgmtService(int numberOfThreads, int maxAwaitTerminationMillis) {
        super(numberOfThreads, maxAwaitTerminationMillis);
        String application = "TaskMgmtService";
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(this.numberOfThreads, new AbmThreadFactory(application));
        this.executorService = executorService;
    }

    public TaskMgmtService(ThreadPoolSettings threadPoolSettings) {
        super(threadPoolSettings);
        String application = threadPoolSettings.name();
        ScheduledExecutorService executorService = Executors.newScheduledThreadPool(this.numberOfThreads, new AbmThreadFactory(application));
        this.executorService = executorService;
    }

    public Future<T> execute(TaskEntry taskEntry) {
        concurrentAccountLocks.acquire(taskEntry);
        Callable<T> taskHandler = (Callable<T>) taskEntry.getTaskHandler();
        TaskWorker<T> taskWorker = TaskWorkerFactory.getTaskWorker(taskHandler, concurrentAccountLocks, taskEntry);
        return executorService.submit(taskWorker);
    }

    public void resize(int increasePoolSize) {
        if (increasePoolSize > 0) {
            ThreadPoolExecutor executorService = (ThreadPoolExecutor) this.executorService;
            int newSize = executorService.getCorePoolSize() + increasePoolSize;
            executorService.setCorePoolSize(newSize);
            executorService.setMaximumPoolSize(newSize);
        }
    }
}
