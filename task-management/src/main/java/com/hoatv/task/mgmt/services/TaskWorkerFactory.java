package com.hoatv.task.mgmt.services;

import com.hoatv.fwk.common.exceptions.AppException;
import com.hoatv.fwk.common.services.CheckedSupplier;
import com.hoatv.task.mgmt.entities.TaskEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

public class TaskWorkerFactory {

    private static final Logger APP_LOGGER = LoggerFactory.getLogger(TaskWorkerFactory.class);

    private TaskWorkerFactory() {
    }

    public static <T> RunnableWorker getRunnableWorker(Callable<T> callable, TaskMgmtSemaphore semaphore, Long period,
            TaskEntry taskEntry) {

        return new RunnableWorker() {

            @Override
            public TaskMgmtSemaphore getLockObject() {
                return semaphore;
            }

            @Override
            public void run() {
                try {
                    callable.call();
                } catch (Throwable exception) {
                    APP_LOGGER.error("An exception occurred while executing schedule task", exception);
                } finally {
                    if (period <= 0) {
                        getLockObject().release(taskEntry);
                    }
                }
            }
        };
    }

    public static <T> TaskWorker<T> getTaskWorker(Callable<T> callable, TaskMgmtSemaphore semaphore, TaskEntry taskEntry) {

        return new TaskWorker<>() {
            @Override
            public TaskMgmtSemaphore getLockObject() {
                return semaphore;
            }

            @Override
            public T callWithReturn() {
                try {
                    return callable.call();
                } catch (Throwable exception){
                    APP_LOGGER.error("An exception occurred while executing schedule task", exception);
                    return null;
                } finally {
                    getLockObject().release(taskEntry);
                }
            }
        };
    }
}
