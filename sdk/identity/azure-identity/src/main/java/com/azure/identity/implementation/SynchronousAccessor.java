// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import java.time.Duration;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Synchronizes reactor threads accessing/instantiating a common value {@code T}.
 *
 * @param <T> The value being instantiated / accessed.
 */
public class SynchronousAccessor<T> {
    private volatile T cache;
    private Supplier<T> cacheSupplier;
    private ReentrantLock lock;
    private Duration cacheTimeout;

    private long startTimeInMillis;

    public SynchronousAccessor(Supplier<T> supplier) {
        cacheSupplier = supplier;
        startTimeInMillis = System.currentTimeMillis();
        lock = new ReentrantLock();
    }

    public SynchronousAccessor(Supplier<T> supplier, Duration cacheTimeout) {
        cacheSupplier = supplier;
        startTimeInMillis = System.currentTimeMillis();
        this.cacheTimeout = cacheTimeout;
        lock = new ReentrantLock();
    }

    /**
     * Get the value from the configured supplier.
     *
     * @return the output {@code T}
     */
    public T getValue() {
        if (cache == null || shouldRefreshCache()) {
            lock.lock();
            try {
                cache = cacheSupplier.get();
            } finally {
                lock.unlock();
            }
        }
        return cache;
    }

    private boolean shouldRefreshCache() {
        return cacheTimeout != null
            ? (System.currentTimeMillis() - startTimeInMillis) >= cacheTimeout.toMillis()
            : false;
    }
}
