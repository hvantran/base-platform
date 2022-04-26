package com.hoatv.task.mgmt.services;

import java.util.concurrent.Callable;

public interface TaskWorker<T> extends Callable<T> {

    default TaskMgmtSemaphore getLockObject() {
        return null;
    }

    T callWithReturn();

    @Override
    default T call() {
        return callWithReturn();
    }
}
