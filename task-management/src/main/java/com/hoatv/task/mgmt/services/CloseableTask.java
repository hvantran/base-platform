package com.hoatv.task.mgmt.services;

import com.hoatv.task.mgmt.annotations.ThreadPoolSettings;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
public abstract class CloseableTask implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloseableTask.class);

    private boolean isClosed;
    protected final int numberOfThreads;
    protected final int awaitTerminationTimes;
    protected final TaskMgmtSemaphore concurrentAccountLocks;
    protected ExecutorService executorService;

    protected CloseableTask(int numberOfThreads, int maxAwaitTerminationMillis) {
        this.numberOfThreads = numberOfThreads;
        this.awaitTerminationTimes = maxAwaitTerminationMillis;
        this.concurrentAccountLocks = new TaskMgmtSemaphore(this.numberOfThreads);
    }

    public long getActiveTasks() {
        return (long)concurrentAccountLocks.getInitialPermits() - concurrentAccountLocks.availablePermits();
    }

    protected CloseableTask(ThreadPoolSettings threadPoolSettings) {
        this(threadPoolSettings.numberOfThreads(), threadPoolSettings.maxAwaitTerminationMillis());
    }

    protected CloseableTask(int numberOfThreads, int maxAwaitTerminationMillis, ExecutorService executorService) {
        this(numberOfThreads, maxAwaitTerminationMillis);
        this.executorService = executorService;
    }

    protected CloseableTask(ThreadPoolSettings threadPoolSettings, ExecutorService executorService) {
        this(threadPoolSettings.numberOfThreads(), threadPoolSettings.maxAwaitTerminationMillis(), executorService);
    }

    private void awaitShutdown() {
        try {
            while (concurrentAccountLocks.availablePermits() != concurrentAccountLocks.getInitialPermits()) {
                LOGGER.debug("Still have some tasks in processing. Need to wait for all task completed.");
                Thread.sleep(300);
            }
            isClosed = true;
        } catch (InterruptedException exception){
            LOGGER.error("An exception occurred while shutdown executor", exception);
            Thread.currentThread().interrupt();
        }
        shutdownNow();
    }

    @Override
    public void close() {
        awaitShutdown();
    }

    public void shutdownNow() {
        try {
            executorService.shutdown();
            if( executorService.awaitTermination(awaitTerminationTimes, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException exception) {
            LOGGER.error("An exception occurred while shutdown executor", exception);
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
