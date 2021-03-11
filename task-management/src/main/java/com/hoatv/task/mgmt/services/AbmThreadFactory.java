package com.hoatv.task.mgmt.services;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
public class AbmThreadFactory implements ThreadFactory {

    private AtomicInteger threadNumber = new AtomicInteger(1);

    @NonNull
    private String name;

    @Override
    public Thread newThread(Runnable runnable) {
        return new Thread(runnable, name + "-thread-" + threadNumber.getAndIncrement());
    }
}
