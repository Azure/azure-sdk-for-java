// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.LRUCache;
import org.testng.annotations.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class LRUCacheTests {

    @Test
    public void cacheSize() {
        int cacheSize = 10;
        LRUCache<String, String> lruCache = new LRUCache<>(cacheSize);
        // Trying to insert docs twice the size of cache
        for (int i = 0; i < 2 * cacheSize; i++) {
            lruCache.put(UUID.randomUUID().toString(), "some value");
        }

        assertThat(lruCache.size()).isLessThanOrEqualTo(cacheSize);
    }

    @Test
    public void cacheBehavior() {
        int cacheSize = 10;
        // insert docs with keys 0 -9
        // get key 9 multiple times to increase usage.
        // When we try to insert the next key 10, 9 should remain in the cache and 0 should go away
        LRUCache<Integer, Integer> lruCache = new LRUCache<>(cacheSize);
        for (int i = 0; i < 10; i++) {
            lruCache.put(i, 1000); // inserting random value as key is what is imp for us
        }
        lruCache.get(9);
        lruCache.get(9);

        lruCache.put(10, 1000);

        assertThat(lruCache.get(9)).isEqualTo(1000);
        assertThat(lruCache.get(10)).isEqualTo(1000);
        assertThat(lruCache.get(0)).isNull();
    }

}
