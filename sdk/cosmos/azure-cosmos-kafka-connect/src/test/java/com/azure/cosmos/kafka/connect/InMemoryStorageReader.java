// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect;

import org.apache.kafka.connect.storage.OffsetStorageReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/***
 * Only used for test.
 */
public class InMemoryStorageReader implements OffsetStorageReader {
    private static final Logger logger = LoggerFactory.getLogger(InMemoryStorageReader.class);
    private Map<Map<String, Object>, Map<String, Object>> offsetStore;
    public InMemoryStorageReader() {
        offsetStore = new HashMap<>();
    }

    public void populateOffset(Map<Map<String, Object>, Map<String, Object>> offsets) {
        this.offsetStore.putAll(offsets);
    }

    @Override
    public <T> Map<String, Object> offset(Map<String, T> partition) {
        return offsetStore.get(partition);
    }

    @Override
    public <T> Map<Map<String, T>, Map<String, Object>> offsets(Collection<Map<String, T>> partitions) {
        Map<Map<String, T>, Map<String, Object>> results = new HashMap<>();
        for (Map<String, T> partition : partitions) {
            results.put(partition, offsetStore.get(partition));
        }

        return results;
    }
}
