package com.hoatv.task.mgmt.services;

import java.util.concurrent.Callable;

public interface TaskWorker<T> extends Callable<T> {

    TaskMgmtSemaphore getLockObject();

    T callWithReturn();

    @Override
    default T call() {
        T returnValue = callWithReturn();
        return returnValue;
    }
}
