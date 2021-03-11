package com.hoatv.task.mgmt.services;

public interface RunnableWorker extends Runnable {

    TaskMgmtSemaphore getLockObject();
}
