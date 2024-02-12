// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.apachecommons.collections.map.UnmodifiableMap;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.azure.cosmos.implementation.routing.RegionNameToRegionIdMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class PartitionKeyRangeIdToSessionTokens {

    private static final Logger logger = LoggerFactory.getLogger(PartitionKeyRangeIdToSessionTokens.class);

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, RegionLevelProgress>> partitionKeyRangeIdToSessionTokens;

    public PartitionKeyRangeIdToSessionTokens() {
        this.partitionKeyRangeIdToSessionTokens = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<String, RegionLevelProgress>> getPartitionKeyRangeIdToSessionTokens() {
        return this.partitionKeyRangeIdToSessionTokens;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PartitionKeyRangeIdToSessionTokens that = (PartitionKeyRangeIdToSessionTokens) o;
        return Objects.equals(this.partitionKeyRangeIdToSessionTokens, that.partitionKeyRangeIdToSessionTokens);
    }

    @Override
    public int hashCode() {
        return Objects.hash(partitionKeyRangeIdToSessionTokens);
    }

    public void tryRecordSessionToken(ISessionToken parsedSessionToken, String partitionKeyRangeId, String firstEffectivePreferredReadableRegion, String regionRoutedTo) {

        this.partitionKeyRangeIdToSessionTokens.compute(partitionKeyRangeId, (partitionKeyRangeIdAsKey, regionToSessionTokenAsVal) -> {

            // identify whether regionRoutedTo has a regionId mapping in session token
            // if regionRoutedTo doesn't exist in mappings add global session token
            VectorSessionToken vectorSessionToken = (VectorSessionToken) parsedSessionToken;

            UnmodifiableMap<Integer, Long> localLsnByRegion = vectorSessionToken.getLocalLsnByRegion();
            int regionId = RegionNameToRegionIdMap.getRegionId(regionRoutedTo);

            try {
                if (regionToSessionTokenAsVal == null) {
                    regionToSessionTokenAsVal = new ConcurrentHashMap<>();
                }

                // store the global representation of the session token
                regionToSessionTokenAsVal.merge("global", new RegionLevelProgress(Long.MIN_VALUE, Long.MIN_VALUE, vectorSessionToken), (regionLevelProgressExisting, regionLevelPrgressNew) -> {

                    VectorSessionToken existingVectorSessionToken = regionLevelProgressExisting.vectorSessionToken;
                    VectorSessionToken newVectorSessionToken = regionLevelPrgressNew.vectorSessionToken;

                    return new RegionLevelProgress(Long.MIN_VALUE, Long.MIN_VALUE, (VectorSessionToken) existingVectorSessionToken.merge(newVectorSessionToken));
                });

                 if (regionId != -1) {
                    long localLsn = localLsnByRegion.getOrDefault(regionId, Long.MIN_VALUE);

                    // regionId maps to a satellite region
                    if (localLsn != Long.MIN_VALUE) {
                        regionToSessionTokenAsVal.put(regionRoutedTo, new RegionLevelProgress(parsedSessionToken.getLSN(), localLsn, null));
                    }
                    // regionId maps to a hub region
                    else {
                        regionToSessionTokenAsVal.put(regionRoutedTo, new RegionLevelProgress(parsedSessionToken.getLSN(), Long.MIN_VALUE, null));
                    }

                    // store the session token in parsed form if obtained from the firstEffectivePreferredReadableRegion
                    if (regionRoutedTo.equals(firstEffectivePreferredReadableRegion)) {
                        regionToSessionTokenAsVal.put(regionRoutedTo, new RegionLevelProgress(parsedSessionToken.getLSN(), localLsn, vectorSessionToken));
                    }
                }

                return regionToSessionTokenAsVal;
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

        RegionLevelProgress globalLevelProgress = resolvePartitionKeyRangeIdBasedProgress(partitionKeyRangeId, "global");
        VectorSessionToken globalSessionToken = globalLevelProgress.vectorSessionToken;

        if (!canUseRegionScopedSessionTokens) {
            return globalSessionToken;
        }

        RegionLevelProgress baseLevelProgress = resolvePartitionKeyRangeIdBasedProgress(partitionKeyRangeId, firstEffectivePreferredReadableRegion);
        VectorSessionToken baseSessionToken = baseLevelProgress.vectorSessionToken;

        if (baseSessionToken == null) {
            return globalSessionToken;
        }

        long globalLsn = -1;
        UnmodifiableMap<Integer, Long> localLsnByRegion = globalSessionToken.getLocalLsnByRegion();
        long version = globalSessionToken.getVersion();

        StringBuilder sbPartOne = new StringBuilder();
        StringBuilder sbPartTwo = new StringBuilder();


        for (Map.Entry<Integer, Long> localLsnByRegionEntry : localLsnByRegion.entrySet()) {

            int regionId = localLsnByRegionEntry.getKey();
            long localLsnForRegionId = localLsnByRegionEntry.getValue();
            String normalizedRegionName = RegionNameToRegionIdMap.getRegionName(regionId);

            // the regionId to normalizedRegionName does not exist
            if (normalizedRegionName.equals(StringUtils.EMPTY)) {
                return globalSessionToken;
            }

            if (lesserPreferredRegionsPkProbablyRequestedFrom.contains(normalizedRegionName)) {
                RegionLevelProgress satelliteRegionProgress = this.resolvePartitionKeyRangeIdBasedProgress(partitionKeyRangeId, normalizedRegionName);
                globalLsn = Math.max(globalLsn, satelliteRegionProgress.maxGlobalLsnSeen);
                sbPartTwo.append("#");
                sbPartTwo.append(regionId);
                sbPartTwo.append("=");
                sbPartTwo.append(satelliteRegionProgress.maxLocalLsnSeen);
            } else {
                sbPartTwo.append("#");
                sbPartTwo.append(regionId);
                sbPartTwo.append("=");
                sbPartTwo.append(-1);
            }
        }

        sbPartOne.append(version);
        sbPartOne.append("#");
        sbPartOne.append(globalLsn);

        Utils.ValueHolder<ISessionToken> resolvedSessionToken = new Utils.ValueHolder<>(null);

        // TODO: one additional step of merging base session token / first preferred read region
        if (VectorSessionToken.tryCreate(sbPartOne.append(sbPartTwo).toString(), resolvedSessionToken)) {
            return baseSessionToken.merge(resolvedSessionToken.v);
        }

        return globalSessionToken;
    }

    public boolean isPartitionKeyRangeIdPresent(String partitionKeyRangeId) {
        return this.partitionKeyRangeIdToSessionTokens.containsKey(partitionKeyRangeId);
    }

    private RegionLevelProgress resolvePartitionKeyRangeIdBasedProgress(String partitionKeyRangeId, String progressScope) {
        return this.partitionKeyRangeIdToSessionTokens.get(partitionKeyRangeId).get(progressScope);
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
    }
}
