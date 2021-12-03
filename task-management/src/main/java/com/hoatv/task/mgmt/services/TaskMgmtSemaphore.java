package com.hoatv.task.mgmt.services;

import com.hoatv.fwk.common.services.CheckedSupplier;
import com.hoatv.task.mgmt.entities.TaskEntry;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Getter
public class TaskMgmtSemaphore extends Semaphore {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskMgmtSemaphore.class);

    private final int initialPermits;

    public TaskMgmtSemaphore(int permits) {
        super(permits, true);
        initialPermits = permits;
    }

    public void acquire(TaskEntry taskEntry) {
        CheckedSupplier<Void> checkedSupplier = () -> {
            LOGGER.debug("Acquired a connection for task: {}, available connections: {}", taskEntry.getName(), availablePermits());
            super.acquire();
            return null;
        };
        checkedSupplier.get();
    }

    public boolean tryAcquire(TaskEntry taskEntry) {
        CheckedSupplier<Boolean> acquire = () -> {
            if (super.tryAcquire(5, TimeUnit.SECONDS)) {
                LOGGER.debug("Acquired a connection for task: {}, available connections: {}", taskEntry.getName(), availablePermits());
                return true;
            }
            LOGGER.warn("Connection is not available to running the task: {}, available connections: {}", taskEntry.getName(), availablePermits());
            return false;
        };
        return acquire.get();
    }

    public void release(TaskEntry taskEntry) {
        super.release();
        LOGGER.debug("Released a connection for task: {}, available permits: {}", taskEntry.getName(), availablePermits());
    }
}
