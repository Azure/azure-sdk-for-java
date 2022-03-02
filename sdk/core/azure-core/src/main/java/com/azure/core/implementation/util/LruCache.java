// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.implementation.util;

import com.azure.core.util.logging.ClientLogger;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.function.Function;

/**
 * A least recently used cache that will retain the most recently used key-values until a cache size limit is reached.
 * <p>
 * The least recently used cache will eject values that have been added or accessed least recently, offering the ability
 * to limit the size of the cache while still receiving the benefit of retaining the most recently used values. The
 * cache does have limitations where it is possible for a most commonly used value to be ejected due to a streak of less
 * commonly but more recently used values being added, which won't be covered in this implementation.
 */
public final class LruCache<Key, Value> {
    private static final ClientLogger LOGGER = new ClientLogger(LruCache.class);

    private final int cacheSizeLimit;

    // Maintains the key-value pairs in the cache.
    private final Map<Key, Value> cache;

    // Maintains the least recently used ordering of keys.
    private final Queue<Key> lruKeys;

    /**
     * Creates an {@link LruCache} instance that caches the {@code cacheSize} most recently used values.
     *
     * @param cacheSizeLimit The number of values that can be cached at any time.
     * @throws IllegalArgumentException If {@code cacheSize} is less than or equal to 0.
     */
    public LruCache(int cacheSizeLimit) {
        if (cacheSizeLimit <= 0) {
            throw LOGGER.logExceptionAsError(
                new IllegalArgumentException("'cacheSizeLimit' cannot be less than or equal to 0."));
        }

        this.cacheSizeLimit = cacheSizeLimit;

        // Given that all operations on the LRU cache are synchronized the implementation types can be non-thread safe.
        this.cache = new HashMap<>();
        this.lruKeys = new LinkedList<>();
    }

    /**
     * Puts the key-value pair into the LRU cache.
     * <p>
     * If the key already exists in the cache the value associated with it will be overwritten and the key will become
     * the most recently used key.
     * <p>
     * If the key-value pair will increase the cache size above the allowed limit the least recently used key-value pair
     * will be ejected from the cache and the passed key-value pair will be added.
     *
     * @param key The key of the value being added to the cache.
     * @param value The value being added to the cache.
     * @return The value that was added into the cache.
     */
    public synchronized Value put(Key key, Value value) {
        if (cache.containsKey(key)) {
            lruKeys.remove(key);
            lruKeys.add(key);
            cache.put(key, value);
        } else {
            if (cache.size() >= cacheSizeLimit) {
                cache.remove(lruKeys.poll());
            }

            cache.put(key, value);
            lruKeys.add(key);
        }

        return value;
    }

    /**
     * Gets the value associated to the {@code key} from the cache.
     * <p>
     * If the cache doesn't contain a value associated to the {@code key} null will be returned.
     *
     * @param key The key to get the associated value in the cache.
     * @return The value associated to the key or null if there is no value associated to the key.
     */
    public synchronized Value get(Key key) {
        if (cache.containsKey(key)) {
            lruKeys.remove(key);
            lruKeys.add(key);
            return cache.get(key);
        }

        return null;
    }

    /**
     * Gets or computes the value for the {@code key}.
     * <p>
     * If the {@code key} exists in the cache the value associated with it is return and the key becomes the most
     * recently used key.
     * <p>
     * If the {@code key} doesn't exist, {@code compute} is called with the passed key and the key-value pair is added
     * into the cache. If the new key-value causes the cache to exceed its allowed size the least recently used
     * key-value pair is ejected from the cache.
     *
     * @param key The key to get the associated value in the cache.
     * @param compute The function that will supply the value for the key if the key doesn't exist in the cache.
     * @return Either the found value associated with the key in the cache or the value returned by {@code compute}
     * when supplied with the {@code key}.
     */
    public synchronized Value computeIfAbsent(Key key, Function<Key, Value> compute) {
        if (cache.containsKey(key)) {
            lruKeys.remove(key);
            lruKeys.add(key);

            return cache.get(key);
        }

        if (cache.size() >= cacheSizeLimit) {
            cache.remove(lruKeys.poll());
        }

        Value value = compute.apply(key);
        cache.put(key, value);
        lruKeys.add(key);

        return value;
    }

    /**
     * Gets the current number of key-value pairs in the cache.
     *
     * @return The number of key-value pairs in the cache.
     */
    public synchronized int size() {
        return cache.size();
    }
}
