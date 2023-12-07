package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.guava25.hash.BloomFilter;
import com.azure.cosmos.implementation.guava25.hash.Funnels;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

public class PartitionKeyBasedBloomFilter {

    private static final int EXPECTED_INSERTIONS = 5_000_000;
    private final ConcurrentHashMap<String, BloomFilter<String>> regionScopedBloomFilter;

    public PartitionKeyBasedBloomFilter() {
        this.regionScopedBloomFilter = new ConcurrentHashMap<>();
    }

    public void tryRecordPartitionKey(String regionId, String partitionKey) {
        this.regionScopedBloomFilter.compute(partitionKey, (key, val) -> {

            if (val == null) {
                val = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), EXPECTED_INSERTIONS);
            }

            val.put(partitionKey);
            return val;
        });
    }
}
