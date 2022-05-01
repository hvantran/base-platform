package com.hoatv.ext.endpoint.api;

import com.hoatv.ext.endpoint.services.ExecutionContext;

import java.util.concurrent.Callable;

public interface TaskExecutionImplementation {

    Callable<Object> getExecutionTasks(ExecutionContext executionContext);
}
