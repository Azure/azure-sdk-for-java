// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.apachecommons.collections.map.UnmodifiableMap;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.routing.RegionNameToRegionIdMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PartitionScopedRegionLevelProgress {

    private static final Logger logger = LoggerFactory.getLogger(PartitionScopedRegionLevelProgress.class);

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, RegionLevelProgress>> partitionKeyRangeIdToRegionLevelProgress;

    public final static String GlobalProgressKey = "global";

    public PartitionScopedRegionLevelProgress() {
        this.partitionKeyRangeIdToRegionLevelProgress = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<String, RegionLevelProgress>> getPartitionKeyRangeIdToRegionLevelProgress() {
        return this.partitionKeyRangeIdToRegionLevelProgress;
    }

    public void tryRecordSessionToken(ISessionToken parsedSessionToken, String partitionKeyRangeId, String firstEffectivePreferredReadableRegion, String regionRoutedTo) {

        this.partitionKeyRangeIdToRegionLevelProgress.compute(partitionKeyRangeId, (partitionKeyRangeIdAsKey, regionLevelProgressAsVal) -> {

            try {
                if (regionLevelProgressAsVal == null) {
                    regionLevelProgressAsVal = new ConcurrentHashMap<>();
                }

                VectorSessionToken vectorSessionToken = (VectorSessionToken) parsedSessionToken;

                // store the global merged progress of the session token for a given physical partition
                regionLevelProgressAsVal.merge(GlobalProgressKey, new RegionLevelProgress(Long.MIN_VALUE, Long.MIN_VALUE, vectorSessionToken), (regionLevelProgressExisting, regionLevelProgressNew) -> {

                    VectorSessionToken existingVectorSessionToken = regionLevelProgressExisting.vectorSessionToken;
                    VectorSessionToken newVectorSessionToken = regionLevelProgressNew.vectorSessionToken;

                    return new RegionLevelProgress(Long.MIN_VALUE, Long.MIN_VALUE, (VectorSessionToken) existingVectorSessionToken.merge(newVectorSessionToken));
                });

                // identify whether regionRoutedTo has a regionId mapping in session token
                UnmodifiableMap<Integer, Long> localLsnByRegion = vectorSessionToken.getLocalLsnByRegion();
                String normalizedRegionRoutedTo = regionRoutedTo.toLowerCase(Locale.ROOT).trim().replace(" ", "");

                int regionId = RegionNameToRegionIdMap.getRegionId(normalizedRegionRoutedTo);

                if (regionId != -1) {
                    long localLsn = localLsnByRegion.getOrDefault(regionId, Long.MIN_VALUE);

                    // regionId maps to a satellite region
                    if (localLsn != Long.MIN_VALUE) {
                        regionLevelProgressAsVal.put(normalizedRegionRoutedTo, new RegionLevelProgress(parsedSessionToken.getLSN(), localLsn, null));
                    }
                    // regionId maps to a hub region
                    else {
                        regionLevelProgressAsVal.put(normalizedRegionRoutedTo, new RegionLevelProgress(parsedSessionToken.getLSN(), Long.MIN_VALUE, null));
                    }

                    // store the session token in parsed form if obtained from the firstEffectivePreferredReadableRegion (an overwrite)
                    if (regionRoutedTo.equals(firstEffectivePreferredReadableRegion)) {
                        regionLevelProgressAsVal.put(normalizedRegionRoutedTo, new RegionLevelProgress(parsedSessionToken.getLSN(), localLsn, vectorSessionToken));
                    }
                }

                return regionLevelProgressAsVal;
            } catch (CosmosException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    public ISessionToken tryResolveSessionToken(
        Set<String> lesserPreferredRegionsPkProbablyRequestedFrom,
        String partitionKeyRangeId,
        String firstEffectivePreferredReadableRegion,
        boolean canUseRegionScopedSessionTokens) {

        RegionLevelProgress globalLevelProgress = resolvePartitionKeyRangeIdBasedProgress(partitionKeyRangeId, GlobalProgressKey);
        VectorSessionToken globalSessionToken = globalLevelProgress.vectorSessionToken;

        // if region level scoping is not allowed, then resolve to the global session token
        // region level scoping is allowed in the following cases:
        //      1. when the request is targeted to a specific logical partition
        //      2. when multiple write locations are configured
        if (!canUseRegionScopedSessionTokens) {
            return globalSessionToken;
        }

        RegionLevelProgress baseLevelProgress = resolvePartitionKeyRangeIdBasedProgress(partitionKeyRangeId, firstEffectivePreferredReadableRegion);

        if (baseLevelProgress == null || baseLevelProgress.vectorSessionToken == null) {
            return globalSessionToken;
        }

        VectorSessionToken baseSessionToken = baseLevelProgress.vectorSessionToken;

        long globalLsn = -1;
        UnmodifiableMap<Integer, Long> localLsnByRegion = globalSessionToken.getLocalLsnByRegion();
        long version = globalSessionToken.getVersion();

        StringBuilder sbPartOne = new StringBuilder();
        StringBuilder sbPartTwo = new StringBuilder();

        for (Map.Entry<Integer, Long> localLsnByRegionEntry : localLsnByRegion.entrySet()) {

            int regionId = localLsnByRegionEntry.getKey();
            String normalizedRegionName = RegionNameToRegionIdMap.getRegionName(regionId);

            // the regionId to normalizedRegionName does not exist
            if (normalizedRegionName.equals(StringUtils.EMPTY)) {
                return globalSessionToken;
            }

            if (lesserPreferredRegionsPkProbablyRequestedFrom.contains(normalizedRegionName)) {
                RegionLevelProgress satelliteRegionProgress = this.resolvePartitionKeyRangeIdBasedProgress(partitionKeyRangeId, normalizedRegionName);
                globalLsn = Math.max(globalLsn, satelliteRegionProgress.maxGlobalLsnSeen);
                sbPartTwo.append(VectorSessionToken.SegmentSeparator);
                sbPartTwo.append(regionId);
                sbPartTwo.append(VectorSessionToken.RegionProgressSeparator);
                sbPartTwo.append(satelliteRegionProgress.maxLocalLsnSeen);
            } else {
                sbPartTwo.append(VectorSessionToken.SegmentSeparator);
                sbPartTwo.append(regionId);
                sbPartTwo.append(VectorSessionToken.RegionProgressSeparator);
                sbPartTwo.append(-1);
            }
        }

        sbPartOne.append(version);
        sbPartOne.append(VectorSessionToken.SegmentSeparator);
        sbPartOne.append(globalLsn);

        Utils.ValueHolder<ISessionToken> resolvedSessionToken = new Utils.ValueHolder<>(null);

        // TODO: one additional step of merging base session token / first preferred read region
        if (VectorSessionToken.tryCreate(sbPartOne.append(sbPartTwo).toString(), resolvedSessionToken)) {
            return baseSessionToken.merge(resolvedSessionToken.v);
        }

        return globalSessionToken;
    }

    public boolean isPartitionKeyRangeIdPresent(String partitionKeyRangeId) {
        return this.partitionKeyRangeIdToRegionLevelProgress.containsKey(partitionKeyRangeId);
    }

    private RegionLevelProgress resolvePartitionKeyRangeIdBasedProgress(String partitionKeyRangeId, String progressScope) {
        return this.partitionKeyRangeIdToRegionLevelProgress.get(partitionKeyRangeId).get(progressScope);
    }

    static class RegionLevelProgress {
        private final long maxGlobalLsnSeen;
        private final long maxLocalLsnSeen;
        private final VectorSessionToken vectorSessionToken;

        public RegionLevelProgress(long maxGlobalLsnSeen, long maxLocalLsnSeen, VectorSessionToken vectorSessionToken) {
            this.maxGlobalLsnSeen = maxGlobalLsnSeen;
            this.maxLocalLsnSeen = maxLocalLsnSeen;
            this.vectorSessionToken = vectorSessionToken;
        }

        public long getMaxGlobalLsnSeen() {
            return maxGlobalLsnSeen;
        }

        public long getMaxLocalLsnSeen() {
            return maxLocalLsnSeen;
        }

        public VectorSessionToken getVectorSessionToken() {
            return vectorSessionToken;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RegionLevelProgress that = (RegionLevelProgress) o;
            return maxGlobalLsnSeen == that.maxGlobalLsnSeen && maxLocalLsnSeen == that.maxLocalLsnSeen && Objects.equals(vectorSessionToken, that.vectorSessionToken);
        }

        @Override
        public int hashCode() {
            return Objects.hash(maxGlobalLsnSeen, maxLocalLsnSeen, vectorSessionToken);
        }
    }
}
