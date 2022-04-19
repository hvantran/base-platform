package com.hoatv.task.mgmt.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class AbmThreadFactory implements ThreadFactory {

    private static final Logger APP_LOGGER = LoggerFactory.getLogger(AbmThreadFactory.class);

    private AtomicInteger threadNumber = new AtomicInteger(1);

    @NonNull
    private String name;

    @Override
    public Thread newThread(@NonNull Runnable runnable) {
        String threadName = this.name + "-thread-" + threadNumber.getAndIncrement();
        APP_LOGGER.debug("Created a new thread name {}", threadName);
        return new Thread(runnable, threadName);
    }
}
