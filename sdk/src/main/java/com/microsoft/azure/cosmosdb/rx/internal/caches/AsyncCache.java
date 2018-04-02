/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmosdb.rx.internal.caches;

import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;
import rx.Single;
import rx.functions.Func0;

class AsyncCache<TKey, TValue> {

    private final Logger logger = LoggerFactory.getLogger(AsyncCache.class);
    private final ConcurrentHashMap<TKey, AsyncLazy<TValue>> values = new ConcurrentHashMap<>();

    private final IEqualityComparer<TValue> equalityComparer;

    public AsyncCache(IEqualityComparer<TValue> equalityComparer) {
        this.equalityComparer = equalityComparer;
    }

    public AsyncCache() {
        this(new IEqualityComparer<TValue>() {
            @Override
            public boolean areEqual(TValue value1, TValue value2) {
                if (value1 == value2)
                    return true;
                if (value1 == null || value2 == null)
                    return false;
                return value1.equals(value2);
            }
        });
    }

    public void set(TKey key, TValue value) {
        logger.debug("set cache[{}]={}", key, value);
        values.put(key, new AsyncLazy<>(value));
    }

    /**
     * Gets value corresponding to <code>key</code>
     *
     * <p>
     * If another initialization function is already running, new initialization function will not be started.
     * The result will be result of currently running initialization function.
     * </p>
     *
     * <p>
     * If previous initialization function is successfully completed - value returned by it will be returned unless
     * it is equal to <code>obsoleteValue</code>, in which case new initialization function will be started.
     * </p>
     * <p>
     * If previous initialization function failed - new one will be launched.
     * </p>
     *
     * @param key Key for which to get a value.
     * @param obsoleteValue Value which is obsolete and needs to be refreshed.
     * @param singleValueInitFunc Initialization function.
     * @return Cached value or value returned by initialization function.
     */
    public Single<TValue> getAsync(
            TKey key,
            TValue obsoleteValue,
            Func0<Single<TValue>> singleValueInitFunc) {

        AsyncLazy<TValue> initialLazyValue = values.get(key);
        if (initialLazyValue != null) {

            logger.debug("cache[{}] exists", key);
            return initialLazyValue.single().toObservable().flatMap(vaule -> {

                if (!equalityComparer.areEqual(vaule, obsoleteValue)) {
                    logger.debug("Returning cache[{}] as it is different from obsoleteValue", key);
                    return Observable.just(vaule);
                }

                logger.debug("cache[{}] result value is obsolete ({}), computing new value", key, obsoleteValue);
                AsyncLazy<TValue> asyncLazy = new AsyncLazy<>(singleValueInitFunc);
                AsyncLazy<TValue> actualValue = values.merge(key, asyncLazy,
                        (lazyValue1, lazyValu2) -> lazyValue1 == initialLazyValue ? lazyValu2 : lazyValue1);
                return actualValue.single().toObservable();

            }, err -> {

                logger.debug("cache[{}] resulted in error {}, computing new value", key, err);
                AsyncLazy<TValue> asyncLazy = new AsyncLazy<>(singleValueInitFunc);
                AsyncLazy<TValue> resultAsyncLazy = values.merge(key, asyncLazy,
                        (lazyValue1, lazyValu2) -> lazyValue1 == initialLazyValue ? lazyValu2 : lazyValue1);
                return resultAsyncLazy.single().toObservable();

            }, () -> Observable.empty()).toSingle();
        }

        logger.debug("cache[{}] doesn't exist, computing new value", key);
        AsyncLazy<TValue> asyncLazy = new AsyncLazy<>(singleValueInitFunc);
        AsyncLazy<TValue> resultAsyncLazy = values.merge(key, asyncLazy,
                (lazyValue1, lazyValu2) -> lazyValue1 == initialLazyValue ? lazyValu2 : lazyValue1);
        return resultAsyncLazy.single();
    }

    public void remove(TKey key) {
        values.remove(key);
    }

    /**
     * Remove value from cache and return it if present
     * @param key
     * @return Value if present, default value if not present
     */
    public Single<TValue> removeAsync(TKey key) {
        AsyncLazy<TValue> lazy = values.remove(key);
        return lazy.single();
        // TODO: .Net returns default value on failure of single why?
    }

    public void clear() {
        this.values.clear();
    }

    /**
     * Forces refresh of the cached item if it is not being refreshed at the moment.
     * @param key
     * @param singleValueInitFunc
     */
    public void refresh(
            TKey key,
            Func0<Single<TValue>> singleValueInitFunc) {
        logger.debug("refreshing cache[{}]", key);
        AsyncLazy<TValue> initialLazyValue = values.get(key);
        if (initialLazyValue != null && (initialLazyValue.isSucceeded() || initialLazyValue.isFaulted())) {
            AsyncLazy<TValue> newLazyValue = new AsyncLazy<>(singleValueInitFunc);

            // Update the new task in the cache,
            values.merge(key, newLazyValue,
                    (lazyValue1, lazyValu2) -> lazyValue1 == initialLazyValue ? lazyValu2 : lazyValue1);
        }
    }
}
