/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.rest.v2.http;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A thread-safe multi map where the values for a certain key are FIFO organized.
 * @param <K> the key type
 * @param <V> the value type
 */
public class ConcurrentMultiHashMap<K, V> {
    private final Map<K, ConcurrentLinkedQueue<V>> data;
    private final AtomicInteger size;

    /**
     * Create a concurrent multi hash map.
     */
    public ConcurrentMultiHashMap() {
        this.data = Collections.synchronizedMap(new LinkedHashMap<K, ConcurrentLinkedQueue<V>>(16, 0.75f, true));
        this.size = new AtomicInteger(0);
    }

    /**
     * Add a new key value pair to the multimap.
     *
     * @param key the key to put
     * @param value the value to put
     * @return the added value
     */
    public V put(K key, V value) {
        synchronized (size) {
            if (!data.containsKey(key)) {
                data.put(key, new ConcurrentLinkedQueue<V>());
            }
            data.get(key).add(value);
            size.incrementAndGet();
            return value;
        }
    }

    /**
     * Returns the queue associated with the given key.
     *
     * @param key the key to query
     * @return the queue associated with the key
     */
    public ConcurrentLinkedQueue<V> get(K key) {
        return data.get(key);
    }

    /**
     * Retrieves and removes one item from the multi map. The item is from
     * the least recently used key set.
     * @return the item removed from the map
     */
    public V poll() {
        synchronized (size) {
            if (size.get() == 0) {
                return null;
            } else {
                K key;
                synchronized (data) {
                    Iterator<K> keys = data.keySet().iterator();
                    key = keys.next();
                }
                return poll(key);
            }
        }
    }

    /**
     * Retrieves the least recently used item in the queue for the given key.
     *
     * @param key the key to poll an item
     * @return the least recently used item for the key
     */
    public V poll(K key) {
        if (!data.containsKey(key)) {
            return null;
        } else {
            ConcurrentLinkedQueue<V> queue = data.get(key);
            V ret;
            synchronized (size) {
                size.decrementAndGet();
                ret = queue.poll();
            }
            if (queue.isEmpty()) {
                data.remove(key);
            }
            return ret;
        }
    }

    /**
     * @return the size of the multimap.
     */
    public int size() {
        return size.get();
    }

    /**
     * Checks if there are values associated with a key in the multimap.
     *
     * @param key the key to check
     * @return true if there are values associated
     */
    public boolean containsKey(K key) {
        return data.containsKey(key) && data.get(key).size() > 0;
    }

    /**
     * @return the set of keys with which there are values associated
     */
    public Set<K> keys() {
        return Sets.filter(data.keySet(), new Predicate<K>() {
            @Override
            public boolean apply(K input) {
                return data.get(input).size() > 0;
            }
        });
    }

    /**
     * @return the set of all values for all keys in the multimap.
     */
    public Set<V> values() {
        Set<V> values = new HashSet<>();
        for (K k : keys()) {
            values.addAll(data.get(k));
        }
        return values;
    }

    /**
     * Removes a key value pair in the multimap. If there's no such key value
     * pair then this returns false. Otherwise this method removes it and
     * returns true.
     *
     * @param key the key to remove
     * @param value the value to remove
     * @return true if an item is removed
     */
    public boolean remove(K key, V value) {
        if (!data.containsKey(key)) {
            return false;
        }
        ConcurrentLinkedQueue<V> queue = data.get(key);
        boolean removed;
        synchronized (size) {
            size.decrementAndGet();
            removed = queue.remove(value);
        }
        if (queue.isEmpty()) {
            data.remove(key);
        }
        return removed;
    }
}
