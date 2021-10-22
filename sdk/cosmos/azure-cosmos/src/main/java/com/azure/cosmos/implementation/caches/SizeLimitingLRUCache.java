// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.caches;

import com.azure.cosmos.implementation.query.PartitionedQueryExecutionInfo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * LRU Cache using LinkedHashMap that limits the number of entries
 */
public class SizeLimitingLRUCache extends LinkedHashMap<String, PartitionedQueryExecutionInfo> {

    private static final long serialVersionUID = 1L;
    private final int maxEntries;

    public SizeLimitingLRUCache(int maxEntries) {
        this.maxEntries = maxEntries;
    }

    public SizeLimitingLRUCache(int initialCapacity, float loadFactor, int maxEntries) {
        super(initialCapacity, loadFactor);
        this.maxEntries = maxEntries;
    }

    public SizeLimitingLRUCache(
        Map<? extends String, ? extends PartitionedQueryExecutionInfo> m, int maxEntries) {
        super(m);
        this.maxEntries = maxEntries;
    }

    public SizeLimitingLRUCache(int initialCapacity, float loadFactor, boolean accessOrder, int maxEntries) {
        super(initialCapacity, loadFactor, accessOrder);
        this.maxEntries = maxEntries;
    }

    public SizeLimitingLRUCache(int initialCapacity, int maxEntries) {
        super(initialCapacity);
        this.maxEntries = maxEntries;
    }

    @Override
    protected boolean removeEldestEntry(
        Map.Entry<String, PartitionedQueryExecutionInfo> eldest) {
        return size() > maxEntries;
    }
}
