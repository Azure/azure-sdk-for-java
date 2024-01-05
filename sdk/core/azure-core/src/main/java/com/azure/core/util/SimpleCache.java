// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A simple cache that stores key-value pairs in memory.
 * @param <K> The type of the key.
 * @param <V> The type of the value.
 */
public final class SimpleCache<K, V> {
    /**
     * The default maximum number of entries to store in the cache.
     */
    public static final int DEFAULT_LIMIT = 10_000;

    private final int limit;
    private final Map<K, V> cache = new ConcurrentHashMap<>();

    /**
     * Creates a new instance of {@link SimpleCache} with a default limit of {@link #DEFAULT_LIMIT}.
     */
    public SimpleCache() {
        this(DEFAULT_LIMIT);
    }

    /**
     * Creates a new instance of {@link SimpleCache}.
     *
     * @param limit The maximum number of entries to store in the cache.
     */
    public SimpleCache(int limit) {
        this.limit = limit;
    }

    /**
     * Gets the value associated with the given key.
     *
     * @param key The key to get the value for.
     * @return The value associated with the given key, or null if no value is associated with the key.
     */
    public V get(K key) {
        return cache.get(key);
    }

    /**
     * Puts the given key-value pair into the cache.
     *
     * @param key The key to put into the cache.
     * @param value The value to put into the cache.
     */
    public void put(K key, V value) {
        if (cache.size() >= limit) {
            cache.clear();
        }

        cache.put(key, value);
    }

    /**
     * Puts the given key-value pair into the cache if the key is not already present.
     *
     * @param key The key to compute.
     * @param mappingFunction The function to compute the value if the key is not already present.
     * @return Either the existing value associated with the key, or the new value if no value was associated with the
     * key.
     */
    public V computeIfAbsent(K key, java.util.function.Function<? super K, ? extends V> mappingFunction) {
        V value = cache.get(key);
        if (value != null) {
            return value;
        }

        // Will push the cache over the limit, so clear it.
        if (cache.size() >= limit) {
            cache.clear();
        }

        return cache.computeIfAbsent(key, mappingFunction);
    }

    /**
     * Removes the given key from the cache.
     *
     * @param key The key to remove from the cache.
     * @return The value associated with the given key, or null if no value was associated with the key.
     */
    public V remove(K key) {
        return cache.remove(key);
    }
}
