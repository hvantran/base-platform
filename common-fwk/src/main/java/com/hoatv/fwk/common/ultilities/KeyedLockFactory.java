package com.hoatv.fwk.common.ultilities;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

        private final Map<String, Semaphore> locks = new ConcurrentHashMap<>();

        public void acquire(String key) throws InterruptedException {
            locks.get(key).acquire();
        }

        public void release(String key) throws InterruptedException {
            locks.get(key).release();
        }

        public boolean tryAcquire(String key, long timeout, TimeUnit unit) throws InterruptedException {
            return locks.get(key).tryAcquire(timeout, unit);
        }

        public void putIfAbsent(String key, Semaphore semaphore) {
            locks.putIfAbsent(key, semaphore);
        }
    }
}
