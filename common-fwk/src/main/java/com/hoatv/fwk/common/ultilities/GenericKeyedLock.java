package com.hoatv.fwk.common.ultilities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Getter
@NoArgsConstructor
public class GenericKeyedLock<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GenericKeyedLock.class);

    private final Map<T, Semaphore> locks = new ConcurrentHashMap<>();

    public void acquire (T key) throws InterruptedException {
        LOGGER.info("Acquire lock by key {}", key);
        locks.get(key).acquire();
    }

    public void release (T key) {
        LOGGER.info("Release lock by key {}", key);
        locks.get(key).release();
    }

    public boolean tryAcquire (T key, long timeout, TimeUnit unit) throws InterruptedException {
        LOGGER.info("Try acquire lock by key {}, timeout - {}", key, timeout);
        return locks.get(key).tryAcquire(timeout, unit);
    }

    public void putIfAbsent (T key, Semaphore semaphore) {
        LOGGER.info("Put lock key {}, semaphore - {}", key, semaphore);
        locks.putIfAbsent(key, semaphore);
    }
}
