package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.guava25.hash.BloomFilter;
import com.azure.cosmos.implementation.guava25.hash.Funnels;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PartitionKeyBasedBloomFilter {

    private static final int EXPECTED_INSERTIONS = 5_000_000;
    private final BloomFilter<String> stringBasedBloomFilter;
    private final Set<String> recordedRegions;

    public PartitionKeyBasedBloomFilter() {
        this.stringBasedBloomFilter = BloomFilter.create(Funnels.stringFunnel(StandardCharsets.UTF_8), EXPECTED_INSERTIONS);
        this.recordedRegions = ConcurrentHashMap.newKeySet();
    }

    public void tryRecordPartitionKey(Long collectionRid, String firstPreferredWritableRegion, String region, String partitionKey) {

        if (!region.equals(firstPreferredWritableRegion)) {
            final String effectiveKey = constructEffectiveKeyForBloomFilter(collectionRid, region, partitionKey);
            this.stringBasedBloomFilter.put(effectiveKey);
            this.recordedRegions.add(region);
        }
    }

    public ISessionToken tryResolveSessionToken(
        Long collectionRid,
        String partitionKey,
        String pkRangeId,
        String firstPreferredWritableRegion,
        RegionBasedSessionTokenRegistry regionBasedSessionTokenRegistry) {

        List<String> regionsPkIsProbablyRequestedFrom = new ArrayList<>();

        for (String region : this.recordedRegions) {

            String effectiveKey = constructEffectiveKeyForBloomFilter(collectionRid, region, partitionKey);

            if (this.stringBasedBloomFilter.mightContain(effectiveKey)) {
                regionsPkIsProbablyRequestedFrom.add(region);
            }
        }

        return regionBasedSessionTokenRegistry.tryResolveSessionToken(regionsPkIsProbablyRequestedFrom, firstPreferredWritableRegion, pkRangeId);
    }

    private static String constructEffectiveKeyForBloomFilter(Long collectionRid, String region, String partitionKey) {
        if (collectionRid == null) {
            throw new IllegalArgumentException("collectionRid cannot be null.");
        }

        if (region == null || region.isEmpty()) {
            throw new IllegalArgumentException("region cannot be empty or null.");
        }

        if (partitionKey == null || partitionKey.isEmpty()) {
            throw new IllegalArgumentException("partitionKey cannot be empty or null.");
        }

        return region + ":" + collectionRid + ":" + partitionKey;
    }

}
