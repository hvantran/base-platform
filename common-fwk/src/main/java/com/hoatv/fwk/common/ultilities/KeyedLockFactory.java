package com.hoatv.fwk.common.ultilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class KeyedLockFactory {

    private KeyedLockFactory() {
    }

    public static KeyedLock newKeyLock() {
        return new KeyedLock();
    }

    @Getter
    @NoArgsConstructor
    public static class KeyedLock {

        private static final Logger LOGGER = LoggerFactory.getLogger(KeyedLock.class);

        private final Map<String, Semaphore> locks = new ConcurrentHashMap<>();

        public void acquire(String key) throws InterruptedException {
            LOGGER.info("Acquire lock by key {}", key);
            locks.get(key).acquire();
        }

        public void release(String key) {
            LOGGER.info("Release lock by key {}", key);
            locks.get(key).release();
        }

        public boolean tryAcquire(String key, long timeout, TimeUnit unit) throws InterruptedException {
            LOGGER.info("Try acquire lock by key {}, timeout - {}", key, timeout);
            return locks.get(key).tryAcquire(timeout, unit);
        }

        public void putIfAbsent(String key, Semaphore semaphore) {
            LOGGER.info("Put lock key {}, semaphore - {}", key, semaphore);
            locks.putIfAbsent(key, semaphore);
        }
    }
}
