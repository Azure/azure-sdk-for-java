// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity.implementation.client;

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
    private final Supplier<T> cacheSupplier;
    private final ReentrantLock lock;
    private final Duration cacheTimeout;
    private final long startTimeInMillis;

    /**
     * Creates an instance of the synchronous accessor.
     *
     * @param supplier the supplier that supplies value to be cached
     */
    public SynchronousAccessor(Supplier<T> supplier) {
        cacheSupplier = supplier;
        startTimeInMillis = System.currentTimeMillis();
        lock = new ReentrantLock();
        this.cacheTimeout = null;
    }

    /**
     * Creates an instance of the synchronous accessor.
     *
     * @param supplier the supplier that supplies value to be cached
     * @param cacheTimeout the timeout for the cache
     */
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

    /**
     * Checks whether cache should be refreshed or not.
     *
     * @return the boolean indicating whether cache should be refreshed or not.
     */
    private boolean shouldRefreshCache() {
        return cacheTimeout != null
            ? (System.currentTimeMillis() - startTimeInMillis) >= cacheTimeout.toMillis()
            : false;
    }
}
