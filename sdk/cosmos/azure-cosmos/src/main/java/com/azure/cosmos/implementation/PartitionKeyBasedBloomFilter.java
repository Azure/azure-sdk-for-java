// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.guava25.hash.BloomFilter;
import com.azure.cosmos.implementation.guava25.hash.Funnel;
import com.azure.cosmos.implementation.guava25.hash.PrimitiveSink;
import com.azure.cosmos.implementation.routing.PartitionKeyInternal;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.models.PartitionKeyDefinition;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class PartitionKeyBasedBloomFilter {

    private static final int EXPECTED_INSERTIONS = 5_000_000;
    private static final double ALLOWED_FALSE_POSITIVE_RATE = 0.001;
    private BloomFilter<PartitionKeyBasedBloomFilterType> pkBasedBloomFilter;
    private final Set<String> recordedRegions;
    private final AtomicBoolean isBloomFilterInitialized;
    private final Funnel<PartitionKeyBasedBloomFilterType> funnel;

    public PartitionKeyBasedBloomFilter() {
        this.recordedRegions = ConcurrentHashMap.newKeySet();
        this.isBloomFilterInitialized = new AtomicBoolean(false);
        this.funnel = new Funnel<PartitionKeyBasedBloomFilterType>() {
            @Override
            public void funnel(PartitionKeyBasedBloomFilterType from, PrimitiveSink into) {
                into
                    .putLong(from.collectionRid)
                    .putString(from.effectivePartitionKeyString, StandardCharsets.UTF_8)
                    .putString(from.region, StandardCharsets.UTF_8);
            }
        };
    }

    public void tryInitializeBloomFilter() {
        if (this.isBloomFilterInitialized.compareAndSet(false, true)) {
            this.pkBasedBloomFilter = BloomFilter.create(this.funnel, EXPECTED_INSERTIONS, ALLOWED_FALSE_POSITIVE_RATE);
        }
    }

    public void tryRecordPartitionKey(
        Long collectionRid,
        String firstPreferredWritableRegion,
        Map<String, String> sessionTokenToRegionMapping,
        PartitionKeyInternal partitionKeyInternal,
        PartitionKeyDefinition partitionKeyDefinition) {

        if (sessionTokenToRegionMapping == null || sessionTokenToRegionMapping.isEmpty()) {
            return;
        }

        if (partitionKeyInternal == null) {
            return;
        }

        if (partitionKeyDefinition == null) {
            return;
        }

        if (Strings.isNullOrEmpty(firstPreferredWritableRegion)) {
            return;
        }

        String effectivePartitionKeyString = PartitionKeyInternalHelper
            .getEffectivePartitionKeyString(partitionKeyInternal, partitionKeyDefinition);

        for (Map.Entry<String, String> sessionTokenToRegionPair : sessionTokenToRegionMapping.entrySet()) {

            String sessionTokenUnparsedInner = sessionTokenToRegionPair.getKey();
            String regionInner = sessionTokenToRegionPair.getValue();

            // a session token should have been received from a region to consider an EPK
            // to also have been requested from the region
            if (!regionInner.equals(firstPreferredWritableRegion) && !Strings.isNullOrEmpty(sessionTokenUnparsedInner)) {

                if (isBloomFilterInitialized.get()) {
                    this.pkBasedBloomFilter.put(new PartitionKeyBasedBloomFilterType(effectivePartitionKeyString,
                        regionInner, collectionRid));
                    this.recordedRegions.add(regionInner);
                }
            }
        }
    }

    public List<String> tryResolvePartitionKeyPossibleRegions(
        Long collectionRid, PartitionKeyInternal partitionKey, PartitionKeyDefinition partitionKeyDefinition) {

        List<String> regionsPartitionKeyHasProbablySeen = new ArrayList<>();
        String effectivePartitionKeyString = PartitionKeyInternalHelper.getEffectivePartitionKeyString(partitionKey, partitionKeyDefinition);

        for (String region : this.recordedRegions) {
            if (this.pkBasedBloomFilter.mightContain(new PartitionKeyBasedBloomFilterType(effectivePartitionKeyString, region, collectionRid))) {
                regionsPartitionKeyHasProbablySeen.add(region);
            }
        }

        return regionsPartitionKeyHasProbablySeen;
    }

    public static class PartitionKeyBasedBloomFilterType {

        private final String effectivePartitionKeyString;
        private final String region;
        private final Long collectionRid;

        public PartitionKeyBasedBloomFilterType(String effectivePartitionKeyString, String region, Long collectionRid) {
            this.effectivePartitionKeyString = effectivePartitionKeyString;
            this.region = region;
            this.collectionRid = collectionRid;
        }

        public String getEffectivePartitionKeyString() {
            return effectivePartitionKeyString;
        }

        public String getRegion() {
            return region;
        }

        public Long getCollectionRid() {
            return collectionRid;
        }
    }

}
