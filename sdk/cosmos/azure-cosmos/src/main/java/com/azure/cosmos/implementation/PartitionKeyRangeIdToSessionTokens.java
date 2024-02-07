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

    private final ConcurrentHashMap<String, ISessionToken> partitionKeyRangeIdToSessionTokens;

    public PartitionKeyRangeIdToSessionTokens() {
        this.partitionKeyRangeIdToSessionTokens = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, ISessionToken> getPartitionKeyRangeIdToSessionTokens() {
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

    public void tryRecordSessionToken(ISessionToken parsedSessionToken, String partitionKeyRangeId, String regionRoutedTo) {

        this.partitionKeyRangeIdToSessionTokens.merge(partitionKeyRangeId, parsedSessionToken, (existingSessionToken, newSessionToken) -> {

            try {
                if (existingSessionToken == null) {
                    return newSessionToken;
                }

                return existingSessionToken.merge(newSessionToken);
            } catch (CosmosException e) {
                throw new IllegalStateException(e);
            }
        });
    }

    public ISessionToken tryResolveSessionToken(
        Set<String> lesserPreferredRegionsPkProbablyRequestedFrom,
        String partitionKeyRangeId,
        boolean canUseRegionScopedSessionTokens) {

        ISessionToken pkRangeIdBasedSessionToken = resolveSessionToken(partitionKeyRangeId);

        if (!canUseRegionScopedSessionTokens) {
            return pkRangeIdBasedSessionToken;
        }

        if (pkRangeIdBasedSessionToken instanceof VectorSessionToken) {
            long globalLsn = pkRangeIdBasedSessionToken.getLSN();
            UnmodifiableMap<Integer, Long> localLsnByRegion = ((VectorSessionToken) pkRangeIdBasedSessionToken).getLocalLsnByRegion();
            long version = ((VectorSessionToken) pkRangeIdBasedSessionToken).getVersion();

            StringBuilder sb = new StringBuilder();
            sb.append(version);
            sb.append("#");
            sb.append(globalLsn);

            for (Map.Entry<Integer, Long> localLsnByRegionEntry : localLsnByRegion.entrySet()) {

                int regionId = localLsnByRegionEntry.getKey();
                long localLsnForRegionId = localLsnByRegionEntry.getValue();
                String normalizedRegionName = RegionNameToRegionIdMap.getRegionName(regionId);

                // the regionId to normalizedRegionName does not exist
                if (normalizedRegionName.equals(StringUtils.EMPTY)) {
                    return pkRangeIdBasedSessionToken;
                }

                if (lesserPreferredRegionsPkProbablyRequestedFrom.contains(normalizedRegionName)) {
                    sb.append("#");
                    sb.append(regionId);
                    sb.append("=");
                    sb.append(localLsnForRegionId);
                } else {
                    sb.append("#");
                    sb.append(regionId);
                    sb.append("=");
                    sb.append(-1);
                }
            }

            Utils.ValueHolder<ISessionToken> resolvedSessionToken = new Utils.ValueHolder<>(null);

            if (VectorSessionToken.tryCreate(sb.toString(), resolvedSessionToken)) {
                return resolvedSessionToken.v;
            }

            return null;
        }

        logger.warn("");
        return null;
    }

    public boolean isPartitionKeyRangeIdPresent(String partitionKeyRangeId) {
        return this.partitionKeyRangeIdToSessionTokens.containsKey(partitionKeyRangeId);
    }

    private ISessionToken resolveSessionToken(String partitionKeyRangeId) {
        return this.partitionKeyRangeIdToSessionTokens.get(partitionKeyRangeId);
    }
}
