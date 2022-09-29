// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.CosmosSchedulers;
import com.azure.cosmos.implementation.Exceptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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
 */
public class AsyncCacheNonBlocking<TKey, TValue> {
    private final static Logger logger = LoggerFactory.getLogger(AsyncCacheNonBlocking.class);
    private final ConcurrentHashMap<TKey, AsyncLazyWithRefresh<TValue>> values;

    public AsyncCacheNonBlocking() {
        this.values = new ConcurrentHashMap<>();
    }

    private Boolean removeNotFoundFromCacheException(CosmosException e) {
        if (Exceptions.isNotFound(e)) {
            return true;
        }
        return false;
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
            logger.debug("cache[{}] exists", key);

            return initialLazyValue.getValueAsync().flatMap(value -> {
                if(!forceRefresh.apply(value)) {
                    return Mono.just(value);
                }

                Mono<TValue> refreshMono = initialLazyValue.getOrCreateBackgroundRefreshTaskAsync(singleValueInitFunc);

                return refreshMono.onErrorResume(
                    (exception) -> {
                        // In some scenarios when a background failure occurs like a 404 the initial cache value should be removed.
                        if (exception instanceof CosmosException && removeNotFoundFromCacheException((CosmosException) exception)) {
                            if (initialLazyValue.shouldRemoveFromCache()) {
                                this.remove(key);
                            }
                        }

                        logger.debug("refresh cache [{}] resulted in error", key, exception);
                        return Mono.error(exception);
                    }
                );
            }).onErrorResume((exception) -> {
                if (initialLazyValue.shouldRemoveFromCache()) {
                    this.remove(key);
                }
                logger.debug("cache[{}] resulted in error", key, exception);
                return Mono.error(exception);
            });
        }

        logger.debug("cache[{}] doesn't exist, computing new value", key);
        AsyncLazyWithRefresh<TValue> asyncLazyWithRefresh = new AsyncLazyWithRefresh<TValue>(singleValueInitFunc);
        AsyncLazyWithRefresh<TValue> preResult = this.values.putIfAbsent(key, asyncLazyWithRefresh);
        if (preResult == null) {
            preResult = asyncLazyWithRefresh;
        }
        AsyncLazyWithRefresh<TValue> result = preResult;

        return result.getValueAsync().onErrorResume(
            (exception) -> {
                // Remove the failed task from the dictionary so future requests can send other calls.
                if (result.shouldRemoveFromCache()) {
                    this.remove(key);
                }
                logger.debug("cache[{}] resulted in error", key, exception);
                return Mono.error(exception);
            }
        );
    }

    public void refresh(
            TKey key,
            Function<TValue, Mono<TValue>> singleValueInitFunc) {

        logger.debug("refreshing cache[{}]", key);
        AsyncLazyWithRefresh<TValue> initialLazyValue = values.get(key);
        if (initialLazyValue != null) {
            Mono<TValue> backgroundRefreshTask = initialLazyValue.refresh(singleValueInitFunc);
            if (backgroundRefreshTask != null) {
                backgroundRefreshTask
                        .subscribeOn(CosmosSchedulers.ASYNC_CACHE_BACKGROUND_REFRESH_BOUNDED_ELASTIC)
                        .subscribe();
            }
        }
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
    private static class AsyncLazyWithRefresh<TValue> {
        private final AtomicBoolean removeFromCache = new AtomicBoolean(false);
        private final AtomicReference<Mono<TValue>> value;
        private final AtomicReference<Mono<TValue>> refreshInProgress;

        public AsyncLazyWithRefresh(TValue value) {
            this.value = new AtomicReference<>();
            this.value.set(Mono.just(value));
            this.refreshInProgress = new AtomicReference<>(null);
        }

        public AsyncLazyWithRefresh(Function<TValue, Mono<TValue>> taskFactory) {
            this.value = new AtomicReference<>();
            this.value.set(taskFactory.apply(null).cache());
            this.refreshInProgress = new AtomicReference<>(null);
        }

        public Mono<TValue> getValueAsync() {

            return this.value.get();
        }

        public Mono<TValue> value() {
            return value.get();
        }

        public Mono<TValue> getOrCreateBackgroundRefreshTaskAsync(Function<TValue, Mono<TValue>> createRefreshFunction) {
            Mono<TValue> refreshInProgressSnapshot = this.refreshInProgress.updateAndGet(existingMono -> {
                if (existingMono == null) {
                    logger.debug("Started a new background task");
                    return this.createBackgroundRefreshTask(createRefreshFunction);
                } else {
                    logger.debug("Background refresh task is already in progress");
                }

                return existingMono;
            });
            return refreshInProgressSnapshot == null ? this.value.get() : refreshInProgressSnapshot;
        }

        private Mono<TValue> createBackgroundRefreshTask(Function<TValue, Mono<TValue>> createRefreshFunction) {
            return this.value
                        .get()
                        .flatMap(createRefreshFunction)
                        .flatMap(response -> {
                            this.refreshInProgress.set(null);
                            return this.value.updateAndGet(existingValue -> Mono.just(response));
                        })
                        .doOnError(throwable -> {
                            this.refreshInProgress.set(null);
                            logger.debug("Background refresh task failed", throwable);
                        })
                        .cache();
        }

        /***
         * If there is no refresh in progress background task, then create a new one, else skip
         *
         * @param createRefreshFunction the createRefreshFunction
         * @return if there is already a refreshInProgress task ongoing, then return Mono.empty, else return the newly created background refresh task
         */
        public Mono<TValue> refresh(Function<TValue, Mono<TValue>> createRefreshFunction) {
            if (this.refreshInProgress.compareAndSet(null, this.createBackgroundRefreshTask(createRefreshFunction))) {
                logger.debug("Started a new background task");
                return this.refreshInProgress.get();
            }

            logger.debug("Background refresh task is already in progress, skip creating a new one");
            return null;
        }

        public boolean shouldRemoveFromCache() {
            // Multiple threads could subscribe to the Mono, only one of them will be allowed to remove the Mono from the cache
            // For example for the following scenario:
            // Request1 -> getAsync -> Mono1
            // Request2 -> getAsync -> Mono1
            // Mono1 failed, and we decided to remove this entry from the cache. Request1 has removed the entry from the cache
            // Request3 -> getAsync -> Mono2
            // without this check, request2 will end up removing the cache entry created by request3
            return this.removeFromCache.compareAndSet(false, true);
        }
    }
}
