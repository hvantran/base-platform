package com.hoatv.task.mgmt.services;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;

@Getter
public class TaskMgmtServiceV1 extends CloseableTaskV1 {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskMgmtServiceV1.class);
    private final String appName;

    public TaskMgmtServiceV1(int numberOfThreads, int maxAwaitTerminationMillis, String application) {
        super(numberOfThreads, maxAwaitTerminationMillis, application);
        this.appName = application;
    }

    @Override
    public Future<?> submit(Runnable task) {
        this.concurrentAccountLocks.acquireUninterruptibly();
        return super.submit(task);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        this.concurrentAccountLocks.acquireUninterruptibly();
        return super.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        this.concurrentAccountLocks.acquireUninterruptibly();
        return super.submit(task, result);
    }

    @Override
    public void execute(Runnable task) {
        this.concurrentAccountLocks.acquireUninterruptibly();
        super.execute(task);
        this.concurrentAccountLocks.release();
    }
}
