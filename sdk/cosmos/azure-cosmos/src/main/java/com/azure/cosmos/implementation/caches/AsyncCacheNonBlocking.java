// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.CosmosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

public class AsyncCacheNonBlocking<TKey, TValue> {
    private final Logger logger = LoggerFactory.getLogger(AsyncCacheNonBlocking.class);
    private final ConcurrentHashMap<TKey, AsyncLazyWithRefresh<TValue>> values;
    private final IEqualityComparer<TKey> keyEqualityComparer;

    public AsyncCacheNonBlocking(ConcurrentHashMap<TKey, AsyncLazyWithRefresh<TValue>> values, IEqualityComparer<TKey> keyEqualityComparer) {
        this.values = values;
        this.keyEqualityComparer = keyEqualityComparer;
    }

    public AsyncCacheNonBlocking(IEqualityComparer<TKey> keyEqualityComparer) {
        this(new ConcurrentHashMap<>(), keyEqualityComparer);
    }

    public AsyncCacheNonBlocking() {
        this((value1, value2) -> {
            if (value1 == value2)
                return true;
            if (value1 == null || value2 == null)
                return false;
            return value1.equals(value2);
        });
    }

    /**
     * <summary>
     * <para>
     * Gets value corresponding to <paramref name="key"/>.
     * </para>
     * <para>
     * If another initialization function is already running, new initialization function will not be started.
     * The result will be result of currently running initialization function.
     * </para>
     * <para>
     * If previous initialization function is successfully completed it will return the value. It is possible this
     * value is stale and will only be updated after the force refresh task is complete.
     * </para>
     * <para>
     * Force refresh is true:
     * If the key does not exist: It will create and await the new task
     * If the key exists and the current task is still running: It will return the existing task
     * If the key exists and the current task is already done: It will start a new task to get the updated values.
     * Once the refresh task is complete it will be returned to caller.
     * If it is a success the value in the cache will be updated. If the refresh task throws an exception the key will be removed from the cache.
     * </para>
     * <para>
     * If previous initialization function failed - new one will be launched.
     * </para>
     * </summary>
     */
    public Mono<TValue> getAsync(
        TKey key,
        Function<TValue, Mono<TValue>> singleValueInitFunc,
        boolean forceRefresh) {

        AsyncLazyWithRefresh<TValue> initialLazyValue = values.get(key);
        if (initialLazyValue != null) {
            logger.debug("cache[{}] exists", key);
            return initialLazyValue.getValueAsync().flatMap(value -> {
                try {
                    if (!forceRefresh) {
                        return Mono.just(value);
                    }
                } catch (Exception e) {
                    // This is needed for scenarios where the initial GetAsync was
                    // called but never awaited.
                    if (initialLazyValue.shouldRemoveFromCache()) {
                        this.remove(key);
                        logger.debug("cache[{}] resulted in error", key, e);
                    }
                }

                try {
                    return initialLazyValue.createAndWaitForBackgroundRefreshTaskAsync(singleValueInitFunc);
                } catch (Exception e) {
                    if (initialLazyValue.shouldRemoveFromCache()) {
                        logger.debug("cache[{}] resulted in error, refresh failed", key, e);
                        if (e.equals(CosmosException.class)) {
                            this.remove(key);
                        }
                    }
                }
                return Mono.empty();
            });
        }

        AsyncLazyWithRefresh<TValue> asyncLazyWithRefresh = new AsyncLazyWithRefresh<TValue>(singleValueInitFunc);
        this.values.putIfAbsent(key, asyncLazyWithRefresh);
        AsyncLazyWithRefresh<TValue> result = this.values.get(key);

        try {
            return result.getValueAsync();
        } catch (Exception e) {
            logger.debug("cache[{}] resulted in error", key, e);
            // Remove the failed task from the dictionary so future requests can send other calls..
            this.remove(key);

        }
        return Mono.empty();
    }

    public void set(TKey key, TValue value) {
        logger.debug("set cache[{}]={}", key, value);
        AsyncLazyWithRefresh<TValue> updatedValue = new AsyncLazyWithRefresh<TValue>(value);
        this.values.put(key, updatedValue);
    }

    public void remove(TKey key) {
        values.remove(key);
    }

    /**
     * This is AsyncLazy that has an additional Task that can
     * be used to update the value. This allows concurrent requests
     * to use the stale value while the refresh is occurring.
     */
    private class AsyncLazyWithRefresh<TValue> {
        private final Function<TValue, Mono<TValue>> createValueFunc;
        private final ReentrantLock valueLock = new ReentrantLock();
        private final ReentrantLock removeFromCacheLock = new ReentrantLock();

        private boolean removeFromCache = false;
        private Mono<TValue> value;
        private Mono<TValue> refreshInProgress;

        public AsyncLazyWithRefresh(TValue value) {
            this.createValueFunc = null;
            this.value = Mono.just(value);
            this.refreshInProgress = null;
        }

        public AsyncLazyWithRefresh(Function<TValue, Mono<TValue>> taskFactory) {
            this.createValueFunc = taskFactory;
            this.value = null;
            this.refreshInProgress = null;
        }

        boolean isValueCreated = this.value != null;

        public Mono<TValue> getValueAsync() {
            Mono<TValue> valueMono = this.value;
            if (valueMono != null) {
                return valueMono;
            }

            valueLock.lock();
            try {
                if (this.value != null) {
                    return this.value;
                }

                this.value = this.createValueFunc.apply(null);
                return this.value;
            } finally {
                valueLock.unlock();
            }
        }

        public synchronized Mono<TValue> createAndWaitForBackgroundRefreshTaskAsync(Function<TValue, Mono<TValue>> createRefreshFunction) {
            Mono<TValue> valueMono = this.value;
            AtomicReference<TValue> originalValue = new AtomicReference<>();

            valueMono.flatMap(value -> {
                originalValue.set(value);
                return valueMono;
            });

            AtomicReference<Mono<TValue>> refreshMono = new AtomicReference<>(this.refreshInProgress);
            refreshMono.get().flatMap(value -> {
                Mono<TValue> result = refreshMono.get();
                return result;
            });

            AtomicBoolean createdRefresh = new AtomicBoolean(false);
            valueLock.lock();
            try {
                refreshMono.get().doOnNext(a -> {
                    createdRefresh.set(true);
                    this.refreshInProgress = createRefreshFunction.apply(originalValue.get());
                    refreshMono.set(this.refreshInProgress);
                });
            } finally {
                valueLock.unlock();
            }

            if (!createdRefresh.get()) {
                Mono<TValue> result = refreshMono.get();
                return result;
            }

            Mono<TValue> itemResult = refreshMono.get();
            // TODO: Didn't understand the .net implementation
            return itemResult;
        }

        public boolean shouldRemoveFromCache() {
            if (this.removeFromCache) {
                return false;
            }

            removeFromCacheLock.lock();
            try {
                if (this.removeFromCache) {
                    return false;
                }
                this.removeFromCache = true;
                return true;
            } finally {
                removeFromCacheLock.unlock();
            }
        }
    }
}
