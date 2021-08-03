// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.implementation.DocumentCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class AsyncCache<TKey, TValue> {
    private final Logger logger = LoggerFactory.getLogger(AsyncCache.class);
    private final ConcurrentHashMap<TKey, AsyncLazy<TValue>> values;
    private final IEqualityComparer<TValue> equalityComparer;

    public AsyncCache(IEqualityComparer<TValue> equalityComparer, ConcurrentHashMap<TKey, AsyncLazy<TValue>> values) {
        this.equalityComparer = equalityComparer;
        this.values = values;
    }

    public AsyncCache(IEqualityComparer<TValue> equalityComparer) {
        this(equalityComparer, new ConcurrentHashMap<>());
    }

    public AsyncCache() {
        this((value1, value2) -> {
        if (value1 == value2)
            return true;
        if (value1 == null || value2 == null)
            return false;
        return value1.equals(value2);
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
    public Mono<TValue> getAsync(
            TKey key,
            TValue obsoleteValue,
            Callable<Mono<TValue>> singleValueInitFunc) {

        AsyncLazy<TValue> initialLazyValue = values.get(key);
        if (initialLazyValue != null) {

            logger.debug("cache[{}] exists", key);
            return initialLazyValue.single().flux().flatMap(value -> {

                if (!equalityComparer.areEqual(value, obsoleteValue)) {
                    logger.debug("Returning cache[{}] as it is different from obsoleteValue", key);
                    return Flux.just(value);
                }

                logger.debug("cache[{}] result value is obsolete ({}), computing new value", key, obsoleteValue);
                AsyncLazy<TValue> asyncLazy = new AsyncLazy<>(singleValueInitFunc);
                AsyncLazy<TValue> actualValue = values.merge(key, asyncLazy,
                        (lazyValue1, lazyValue2) -> lazyValue1 == initialLazyValue ? lazyValue2 : lazyValue1);
                return actualValue.single().flux();

            }, err -> {

                logger.debug("cache[{}] resulted in error, computing new value", key, err);
                AsyncLazy<TValue> asyncLazy = new AsyncLazy<>(singleValueInitFunc);
                AsyncLazy<TValue> resultAsyncLazy = values.merge(key, asyncLazy,
                        (lazyValue1, lazyValu2) -> lazyValue1 == initialLazyValue ? lazyValu2 : lazyValue1);
                return resultAsyncLazy.single().flux();

            }, Flux::empty).single();
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
    public Mono<TValue> removeAsync(TKey key) {
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
            Callable<Mono<TValue>> singleValueInitFunc) {
        logger.debug("refreshing cache[{}]", key);
        AsyncLazy<TValue> initialLazyValue = values.get(key);
        if (initialLazyValue != null && (initialLazyValue.isSucceeded() || initialLazyValue.isFaulted())) {
            AsyncLazy<TValue> newLazyValue = new AsyncLazy<>(singleValueInitFunc);

            // UPDATE the new task in the cache,
            values.merge(key, newLazyValue,
                    (lazyValue1, lazyValu2) -> lazyValue1 == initialLazyValue ? lazyValu2 : lazyValue1);
        }
    }

    public abstract static class SerializableAsyncCache<TKey, TValue> implements Serializable {
        private static final long serialVersionUID = 2l;
        private static transient Logger logger = LoggerFactory.getLogger(SerializableAsyncCache.class);
        protected transient AsyncCache<TKey, TValue> cache;

        protected SerializableAsyncCache() {}

        public static class SerializableAsyncCollectionCache extends SerializableAsyncCache<String, DocumentCollection> {
            private static final long serialVersionUID = 2l;
            private SerializableAsyncCollectionCache() {}

            @Override
            protected void serializeKey(ObjectOutputStream oos, String s) throws IOException {
                oos.writeUTF(s);
            }

            @Override
            protected void serializeValue(ObjectOutputStream oos, DocumentCollection documentCollection) throws IOException {
                oos.writeObject(DocumentCollection.SerializableDocumentCollection.from(documentCollection));
            }

            @Override
            protected String deserializeKey(ObjectInputStream ois) throws IOException {
                return ois.readUTF();
            }

            @Override
            protected DocumentCollection deserializeValue(ObjectInputStream ois) throws IOException,
                ClassNotFoundException {
                return ((DocumentCollection.SerializableDocumentCollection) ois.readObject()).getWrappedItem();
            }
        }

        @SuppressWarnings("unchecked")
        public static <TKey, TValue> SerializableAsyncCache<TKey, TValue> from(AsyncCache<TKey,
            TValue> cache, Class<TKey> keyClass, Class<TValue> valueClass) {
            if (keyClass == String.class && valueClass == DocumentCollection.class) {
                SerializableAsyncCollectionCache sacc = new SerializableAsyncCollectionCache();
                sacc.cache = (AsyncCache<String, DocumentCollection>) cache;
                return (SerializableAsyncCache<TKey, TValue>) sacc;
            } else {
                throw new RuntimeException("not supported");
            }
        }

        protected abstract void serializeKey(ObjectOutputStream oos, TKey key) throws IOException;

        protected abstract void serializeValue(ObjectOutputStream oos, TValue value) throws IOException;

        protected abstract TKey deserializeKey(ObjectInputStream oos) throws IOException;

        protected abstract TValue deserializeValue(ObjectInputStream oos) throws IOException, ClassNotFoundException;

        public AsyncCache<TKey, TValue> toAsyncCache() {
            return this.cache;
        }

        private void writeObject(ObjectOutputStream oos)
            throws IOException {
            logger.info("Serializing {}", this.getClass());

            Map<TKey, TValue> paris = new HashMap<>();
            for (Map.Entry<TKey, AsyncLazy<TValue>> entry : cache.values.entrySet()) {
                TKey key = entry.getKey();
                Optional<TValue> value = entry.getValue().tryGet();
                if (value.isPresent()) {
                    paris.put(key, value.get());
                }
            }

            oos.writeInt(paris.size());

            for (Map.Entry<TKey, TValue> entry : paris.entrySet()) {
                serializeKey(oos, entry.getKey());
                serializeValue(oos, entry.getValue());
            }

            oos.writeObject(cache.equalityComparer);
        }

        private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
            logger.info("Deserializing {}", this.getClass());

            int size = ois.readInt();
            ConcurrentHashMap<TKey, AsyncLazy<TValue>> pairs = new ConcurrentHashMap<>();
            for (int i = 0; i < size; i++) {
                TKey key = deserializeKey(ois);
                TValue value = deserializeValue(ois);
                pairs.put(key, new AsyncLazy<>(value));
            }

            @SuppressWarnings("unchecked")
            IEqualityComparer<TValue> equalityComparer = (IEqualityComparer<TValue>) ois.readObject();
            this.cache = new AsyncCache<>(equalityComparer, pairs);
        }
    }
}
