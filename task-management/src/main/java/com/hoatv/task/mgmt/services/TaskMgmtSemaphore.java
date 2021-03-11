package com.hoatv.task.mgmt.services;

import com.hoatv.fwk.common.services.CheckedSupplier;
import com.hoatv.task.mgmt.entities.TaskEntry;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Semaphore;

@Getter
public class TaskMgmtSemaphore extends Semaphore {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskMgmtSemaphore.class);

    private final int initialPermits;

    public TaskMgmtSemaphore(int permits) {
        super(permits, true);
        initialPermits = permits;
    }

    public void acquire(TaskEntry taskEntry) {
        CheckedSupplier<Void> acquire = () -> {
            super.acquire();
            return null;
        };
        acquire.get();
        LOGGER.info("Acquired a connection for application: {}", taskEntry.getApplicationName());
    }

    public void release(TaskEntry taskEntry) {
        super.release();
        LOGGER.info("Released a connection for application: {}, available permits: {}", taskEntry.getApplicationName(), availablePermits());
    }
}
