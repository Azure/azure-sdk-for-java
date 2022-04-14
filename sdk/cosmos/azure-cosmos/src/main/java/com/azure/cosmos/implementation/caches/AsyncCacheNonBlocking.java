// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.HttpConstants;
import com.azure.cosmos.implementation.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
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
                if(!forceRefresh) {
                    return Mono.just(value);
                }

                Mono<TValue> refreshMono = initialLazyValue.createAndWaitForBackgroundRefreshTaskAsync(singleValueInitFunc);
                AsyncLazyWithRefresh<TValue> asyncLazyWithRefresh = new AsyncLazyWithRefresh<TValue>(refreshMono);
                this.values.put(key, asyncLazyWithRefresh);
                AsyncLazyWithRefresh<TValue> result = this.values.get(key);

                return result.getValueAsync().onErrorResume(
                    (error) -> {
                        logger.debug("refresh cache [{}] resulted in error", key, error);
                        if (initialLazyValue.shouldRemoveFromCache()) {
                            this.remove(key);
                        }
                        return Mono.empty();
                    }
                );
            }).onErrorResume((error) -> {
                logger.debug("cache[{}] resulted in error", key, error);
                if (initialLazyValue.shouldRemoveFromCache()) {
                    this.remove(key);
                }
                return Mono.empty();
            });
        }

        logger.debug("cache[{}] doesn't exist, computing new value", key);
        AsyncLazyWithRefresh<TValue> asyncLazyWithRefresh = new AsyncLazyWithRefresh<TValue>(singleValueInitFunc);
        this.values.putIfAbsent(key, asyncLazyWithRefresh);
        AsyncLazyWithRefresh<TValue> result = this.values.get(key);

        return result.getValueAsync().onErrorResume(
            (error) -> {
                logger.debug("cache[{}] resulted in error", key, error);
                // Remove the failed task from the dictionary so future requests can send other calls..
                this.remove(key);
                return Mono.empty();
            }
        );
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

        public AsyncLazyWithRefresh(Mono<TValue> value) {
            this.createValueFunc = null;
            this.value = value;
            this.refreshInProgress = null;
        }

        public AsyncLazyWithRefresh(Function<TValue, Mono<TValue>> taskFactory) {
            this.createValueFunc = taskFactory;
            this.value = null;
            this.refreshInProgress = null;
        }

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

            AtomicReference<Mono<TValue>> refreshMono = new AtomicReference<>();
            valueLock.lock();
            try {
                this.refreshInProgress = createRefreshFunction.apply(originalValue.get());
                refreshMono.set(this.refreshInProgress);
                return refreshMono.get();
            } finally {
                valueLock.unlock();
            }
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
