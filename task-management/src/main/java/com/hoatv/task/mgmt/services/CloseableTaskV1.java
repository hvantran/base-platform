package com.hoatv.task.mgmt.services;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Getter
public abstract class CloseableTaskV1 extends ThreadPoolExecutor implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(CloseableTaskV1.class);

    private boolean isClosed;
    protected final int awaitTerminationTimes;
    protected final TaskMgmtSemaphore concurrentAccountLocks;

    protected CloseableTaskV1(int numberOfThreads, int maxAwaitTerminationMillis, String application) {
        super(numberOfThreads, numberOfThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), new AbmThreadFactory(application));
        this.awaitTerminationTimes = maxAwaitTerminationMillis;
        this.concurrentAccountLocks = new TaskMgmtSemaphore(numberOfThreads);
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
        }
        shutdownNow();
    }

    @Override
    public void close() {
        awaitShutdown();
    }

    @Override
    public List<Runnable> shutdownNow() {
        try {
            super.shutdown();
            if( super.awaitTermination(awaitTerminationTimes, TimeUnit.MILLISECONDS)) {
                return super.shutdownNow();
            }
            return super.shutdownNow();
        } catch (InterruptedException exception) {
            LOGGER.error("An exception occurred while shutdown executor", exception);
            return super.shutdownNow();
        }
    }
}
