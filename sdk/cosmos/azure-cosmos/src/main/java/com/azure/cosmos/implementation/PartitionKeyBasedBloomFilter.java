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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * PartitionKeyBasedBloomFilter encapsulates a bloom filter provided by the Guava library. A bloom filter
 * is a probabilistic data structure which with full guarantee can say if an element does not exist but can
 * only say if an element exists with some error rate.
 * <p>
 * The below class has the following purposes:
 * <ul>
 *     <li>
 *         To store and retrieve regions in which a particular logical partition (scoped to a collection) saw requests in.
 *     </li>
 *     <li>
 *         This allows the RegionScopedSessionContainer to decide which region specific progress
 *         to include when resolving the session token for a request configured to use session consistency.
 *     </li>
 * </ul>
 */
public class PartitionKeyBasedBloomFilter {

    private static final long EXPECTED_INSERTIONS = Configs.getPkBasedBloomFilterExpectedInsertionCount();
    private static final double ALLOWED_FALSE_POSITIVE_RATE = Configs.getPkBasedBloomFilterExpectedFfpRate();
    private BloomFilter<PartitionKeyBasedBloomFilterType> pkBasedBloomFilter;
    private final Set<String> recordedRegions;
    private final AtomicLong bloomFilterApproximateInsertionCount;
    private final Funnel<PartitionKeyBasedBloomFilterType> funnel;

    public PartitionKeyBasedBloomFilter() {
        this.recordedRegions = ConcurrentHashMap.newKeySet();
        this.funnel = new Funnel<PartitionKeyBasedBloomFilterType>() {
            @Override
            public void funnel(PartitionKeyBasedBloomFilterType from, PrimitiveSink into) {
                into
                    .putLong(from.collectionRid)
                    .putString(from.effectivePartitionKeyString, StandardCharsets.UTF_8)
                    .putString(from.region, StandardCharsets.UTF_8);
            }
        };
        this.pkBasedBloomFilter = BloomFilter.create(this.funnel, EXPECTED_INSERTIONS, ALLOWED_FALSE_POSITIVE_RATE);
        this.bloomFilterApproximateInsertionCount = new AtomicLong(0);
    }

    public void tryRecordPartitionKey(
        RxDocumentServiceRequest request,
        Long collectionRid,
        String firstPreferredReadableRegion,
        String regionRoutedTo,
        PartitionKeyInternal partitionKeyInternal,
        PartitionKeyDefinition partitionKeyDefinition) {

        try {
            if (partitionKeyInternal == null) {
                return;
            }

            if (partitionKeyDefinition == null) {
                return;
            }

            if (Strings.isNullOrEmpty(firstPreferredReadableRegion)) {
                return;
            }

            String effectivePartitionKeyString = request.getEffectivePartitionKey() != null ? request.getEffectivePartitionKey() : PartitionKeyInternalHelper
                .getEffectivePartitionKeyString(partitionKeyInternal, partitionKeyDefinition);

            String normalizedRegionRoutedTo = regionRoutedTo.toLowerCase(Locale.ROOT).replace(" ", "");;

            // 1. record region information for EPK hash only if this EPK was resolved
            // to a different preferred region than the first preferred region
            // 2. session token originating from the first preferred region is always
            // merged when resolving the session token for a request, so it is not
            // needed to also record which EPK got resolved in the first preferred region
            // 3. this avoids the bloom filter from getting filled up in steady state
            if (!normalizedRegionRoutedTo.equals(firstPreferredReadableRegion)) {
                this.pkBasedBloomFilter.put(
                    new PartitionKeyBasedBloomFilterType(
                        effectivePartitionKeyString,
                        normalizedRegionRoutedTo,
                        collectionRid));
                this.recordedRegions.add(normalizedRegionRoutedTo);
                this.bloomFilterApproximateInsertionCount.incrementAndGet();
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    // resolve which regions was a given EPK possibly saw requests resolved in (processed by the replica in that region)
    public Set<String> tryGetPossibleRegionsLogicalPartitionResolvedTo(
        RxDocumentServiceRequest request,
        Long collectionRid,
        PartitionKeyInternal partitionKey,
        PartitionKeyDefinition partitionKeyDefinition) {

        try {
            Set<String> regionsPartitionKeyHasProbablySeen = new HashSet<>();

            String effectivePartitionKeyString = request.getEffectivePartitionKey() != null ? request.getEffectivePartitionKey() : PartitionKeyInternalHelper
                .getEffectivePartitionKeyString(partitionKey, partitionKeyDefinition);

            for (String region : this.recordedRegions) {
                if (this.pkBasedBloomFilter.mightContain(new PartitionKeyBasedBloomFilterType(effectivePartitionKeyString, region, collectionRid))) {
                    regionsPartitionKeyHasProbablySeen.add(region);
                }
            }

            if (request.requestContext != null) {
                request.requestContext.setApproximateBloomFilterInsertionCount(bloomFilterApproximateInsertionCount.get());
            }

            return regionsPartitionKeyHasProbablySeen;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean isPartitionKeyResolvedToARegion(
        String effectivePartitionKeyString,
        String normalizedRegion,
        Long collectionRid) {

        return this.pkBasedBloomFilter.mightContain(
            new PartitionKeyBasedBloomFilterType(effectivePartitionKeyString, normalizedRegion, collectionRid));
    }

    // below type represents a bloom filter entry
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
