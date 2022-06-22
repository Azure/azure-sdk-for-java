// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 *
 * @param <TKey>
 * @param <TValue>
 *
 * This is a thread safe AsyncCache that allows refreshing values in the background.
 * The benefits of AsyncCacheNonBlocking over AsyncCache is it keeps stale values until the refresh is completed.
 * AsyncCache removes values causing it to block all requests until the refresh is complete.
 * 1. For example 1 replica moved out of the 4 replicas available. 3 replicas could still be processing requests.
 *    The request going to the 1 stale replica would be retried.
 * 2. AsyncCacheNonBlocking updates the value in the cache rather than recreating it on each refresh. This will help reduce object creation.
 *
 */
public class AsyncCacheNonBlocking<TKey, TValue> {
    private final static Logger logger = LoggerFactory.getLogger(AsyncCacheNonBlocking.class);
    private final ConcurrentHashMap<TKey, AsyncLazyWithRefresh<TValue>> values;
    private final IEqualityComparer<TKey> keyEqualityComparer;

    private AsyncCacheNonBlocking(ConcurrentHashMap<TKey, AsyncLazyWithRefresh<TValue>> values, IEqualityComparer<TKey> keyEqualityComparer) {
        this.values = values;
        this.keyEqualityComparer = keyEqualityComparer;
    }

    private AsyncCacheNonBlocking(IEqualityComparer<TKey> keyEqualityComparer) {
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
     *
     * <p>
     * If another initialization function is already running, new initialization function will not be started.
     * The result will be result of currently running initialization function.
     * </p>
     *
     * <p>
     * If previous initialization function is successfully completed it will return the value. It is possible this
     * value is stale and will only be updated after the force refresh task is complete.
     * Force refresh is true:
     *  If the key does not exist: It will create and await the new task
     *  If the key exists and the current task is still running: It will return the existing task
     *  If the key exists and the current task is already done: It will start a new task to get the updated values.
     *     Once the refresh task is complete it will be returned to caller.
     *     If it is a success the value in the cache will be updated. If the refresh task throws an exception the key will be removed from the cache.
     * </p>
     *
     * <p>
     * If previous initialization function failed - new one will be launched.
     * </p>
     *
     * @param key Key for which to get a value.
     * @param singleValueInitFunc Initialization function.
     * @param forceRefresh Force refresh for refreshing the cache
     * @return Cached value or value returned by initialization function.
     */
    public Mono<TValue> getAsync(
        TKey key,
        Function<TValue, Mono<TValue>> singleValueInitFunc,
        Function<TValue, Boolean> forceRefresh) {

        AsyncLazyWithRefresh<TValue> initialLazyValue = values.get(key);
        if (initialLazyValue != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("cache[{}] exists", key);
            }

            return initialLazyValue.getValueAsync().flatMap(value -> {
                if(!forceRefresh.apply(value)) {
                    return Mono.just(value);
                }

                Mono<TValue> refreshMono = initialLazyValue.createAndWaitForBackgroundRefreshTaskAsync(singleValueInitFunc);

                return refreshMono.onErrorResume(
                    (exception) -> {
                        if (logger.isDebugEnabled()) {
                            logger.debug("refresh cache [{}] resulted in error", key, exception);
                        }
                        if (initialLazyValue.shouldRemoveFromCache()) {
                            this.remove(key);
                        }
                        return Mono.error(exception);
                    }
                );
            }).onErrorResume((exception) -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("cache[{}] resulted in error", key, exception);
                }
                if (initialLazyValue.shouldRemoveFromCache()) {
                    this.remove(key);
                }
                return Mono.error(exception);
            });
        }

        if (logger.isDebugEnabled()) {
            logger.debug("cache[{}] doesn't exist, computing new value", key);
        }
        AsyncLazyWithRefresh<TValue> asyncLazyWithRefresh = new AsyncLazyWithRefresh<TValue>(singleValueInitFunc);
        this.values.putIfAbsent(key, asyncLazyWithRefresh);
        AsyncLazyWithRefresh<TValue> result = this.values.get(key);

        return result.getValueAsync().onErrorResume(
            (exception) -> {
                if (logger.isDebugEnabled()) {
                    logger.debug("cache[{}] resulted in error", key, exception);
                }
                // Remove the failed task from the dictionary so future requests can send other calls..
                this.remove(key);
                return Mono.error(exception);
            }
        );
    }

    public void set(TKey key, TValue value) {
        if (logger.isDebugEnabled()) {
            logger.debug("set cache[{}]={}", key, value);
        }
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
        private final AtomicBoolean removeFromCache = new AtomicBoolean(false);
        private AtomicReference<Mono<TValue>> value;
        private final AtomicReference<Mono<TValue>> refreshInProgress;

        public AsyncLazyWithRefresh(TValue value) {
            this.createValueFunc = null;
            this.value.set(Mono.just(value));
            this.refreshInProgress = new AtomicReference<>();
        }

        public AsyncLazyWithRefresh(Function<TValue, Mono<TValue>> taskFactory) {
            this.createValueFunc = taskFactory;
            this.value = new AtomicReference<>();
            this.refreshInProgress = new AtomicReference<>();
        }

        public Mono<TValue> getValueAsync() {
            this.value.compareAndSet(null, this.createValueFunc.apply(null));

            return this.value.get().cache();
        }

        public Mono<TValue> value() {
            return value.get();
        }

        public Mono<TValue> createAndWaitForBackgroundRefreshTaskAsync(Function<TValue, Mono<TValue>> createRefreshFunction) {
            Mono<TValue> valueMono = this.value.get();
            AtomicReference<TValue> originalValue = new AtomicReference<>();

            return valueMono.flatMap(value -> {
                originalValue.set(value);
                return valueMono;
            }).flatMap(value -> {
                if(this.refreshInProgress.compareAndSet(null, createRefreshFunction.apply(originalValue.get()))) {
                    return this.refreshInProgress.get().cache()
                        .flatMap(response -> {
                            this.value.set(Mono.just(response));

                            this.refreshInProgress.set(null);
                            return this.value.get();
                        });
                }
                return this.refreshInProgress.get();
            });
        }

        public boolean shouldRemoveFromCache() {
            return this.removeFromCache.compareAndSet(false, true);
        }
    }
}
