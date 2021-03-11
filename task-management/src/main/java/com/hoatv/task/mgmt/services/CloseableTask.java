package com.hoatv.task.mgmt.services;

import com.hoatv.task.mgmt.annotations.ThreadPoolSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public abstract class CloseableTask implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloseableTask.class);

    protected final int numberOfThreads;
    protected final int awaitTerminationTimes;
    protected final TaskMgmtSemaphore concurrentAccountLocks;
    protected ExecutorService executorService;

    public CloseableTask(int numberOfThreads, int maxAwaitTerminationMillis) {
        this.numberOfThreads = numberOfThreads;
        this.awaitTerminationTimes = maxAwaitTerminationMillis;
        this.concurrentAccountLocks = new TaskMgmtSemaphore(this.numberOfThreads);
    }

    public CloseableTask(ThreadPoolSettings threadPoolSettings) {
        this(threadPoolSettings.numberOfThreads(), threadPoolSettings.maxAwaitTerminationMillis());
    }

    public CloseableTask(int numberOfThreads, int maxAwaitTerminationMillis, ExecutorService executorService) {
        this(numberOfThreads, maxAwaitTerminationMillis);
        this.executorService = executorService;
    }

    public CloseableTask(ThreadPoolSettings threadPoolSettings, ExecutorService executorService) {
        this(threadPoolSettings.numberOfThreads(), threadPoolSettings.maxAwaitTerminationMillis(), executorService);
    }

    private void awaitShutdown() {
        try {
            while (concurrentAccountLocks.availablePermits() != concurrentAccountLocks.getInitialPermits()) {
                LOGGER.debug("Still have some tasks in processing. Need to wait for all task completed.");
                Thread.sleep(300);
            }

            LOGGER.info("Reached to end of process. Shutdown Executor Service...");
            executorService.shutdown();
            if( executorService.awaitTermination(awaitTerminationTimes, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException exception) {
            LOGGER.error("An exception occurred while shutdown executor", exception);
            executorService.shutdownNow();
        }
    }

    @Override
    public void close() {
        awaitShutdown();
    }
}
