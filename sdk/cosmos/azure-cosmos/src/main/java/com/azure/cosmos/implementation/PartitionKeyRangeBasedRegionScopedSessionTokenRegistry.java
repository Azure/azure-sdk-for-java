// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class PartitionKeyRangeBasedRegionScopedSessionTokenRegistry {

    private static final Logger logger = LoggerFactory.getLogger(PartitionKeyRangeBasedRegionScopedSessionTokenRegistry.class);

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, ISessionToken>> pkRangeIdToRegionScopedSessionTokens;

    public PartitionKeyRangeBasedRegionScopedSessionTokenRegistry() {
        this.pkRangeIdToRegionScopedSessionTokens = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<String, ISessionToken>> getPkRangeIdToRegionScopedSessionTokens() {
        return this.pkRangeIdToRegionScopedSessionTokens;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PartitionKeyRangeBasedRegionScopedSessionTokenRegistry that = (PartitionKeyRangeBasedRegionScopedSessionTokenRegistry) o;
        return Objects.equals(this.pkRangeIdToRegionScopedSessionTokens, that.pkRangeIdToRegionScopedSessionTokens);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pkRangeIdToRegionScopedSessionTokens);
    }

    public void tryRecordSessionToken(Map<String, String> sessionTokenToRegionMapping) {

        if (sessionTokenToRegionMapping == null || sessionTokenToRegionMapping.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> sessionTokenToRegion : sessionTokenToRegionMapping.entrySet()) {

            String sessionTokenUnparsedInner = sessionTokenToRegion.getKey();
            String regionInner = sessionTokenToRegion.getValue();

            if (!Strings.isNullOrEmpty(sessionTokenUnparsedInner) && !Strings.isNullOrEmpty(regionInner)) {

                String[] sessionTokenSegments = StringUtils.split(sessionTokenUnparsedInner, ":");

                if (sessionTokenSegments.length > 1) {

                    String partitionKeyRangeIdInner = sessionTokenSegments[0];
                    ISessionToken parsedRegionSpecificSessionToken = SessionTokenHelper.parse(sessionTokenUnparsedInner);

                    this.pkRangeIdToRegionScopedSessionTokens.compute(partitionKeyRangeIdInner, (pkRangeIdAsKey, regionToSessionTokensAsVal) -> {

                        if (regionToSessionTokensAsVal == null) {
                            regionToSessionTokensAsVal = new ConcurrentHashMap<>();
                        }

                        regionToSessionTokensAsVal.merge(regionInner, parsedRegionSpecificSessionToken, ISessionToken::merge);

                        return regionToSessionTokensAsVal;
                    });
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Unparsed session token - {} cannot be parsed.", sessionTokenUnparsedInner);
                    }
                }
            }

        }
    }

    public ISessionToken tryResolveSessionToken(
        List<String> lesserPreferredRegionsPkProbablyRequestedFrom,
        String firstPreferredWritableRegion,
        String partitionKeyRangeId,
        boolean canUseRegionScopedSessionTokens) {

        List<ISessionToken> regionSpecificSessionTokens = new ArrayList<>();

        ISessionToken sessionTokenForFirstPreferredWritableRegion
            = resolveRegionSpecificSessionToken(firstPreferredWritableRegion, partitionKeyRangeId);

        // case 1 : where the request is not targeted to a logical partition
        // therefore resolve session token representing all regions
        // case 2 : where the request targets a logical partition not seen by
        // the bloom filter / lesser preferred regions
        if (!canUseRegionScopedSessionTokens || lesserPreferredRegionsPkProbablyRequestedFrom.isEmpty()) {
            return mergeSessionTokenRepresentingAllRegions(partitionKeyRangeId);
        }

        if (sessionTokenForFirstPreferredWritableRegion != null) {
            regionSpecificSessionTokens.add(sessionTokenForFirstPreferredWritableRegion);
        }

        for (String region : lesserPreferredRegionsPkProbablyRequestedFrom) {
            ISessionToken regionSpecificSessionToken = resolveRegionSpecificSessionToken(region, partitionKeyRangeId);

            if (regionSpecificSessionToken != null) {
                regionSpecificSessionTokens.add(regionSpecificSessionToken);
            }
        }

        return mergeSessionTokenRepresentingSpecificRegions(regionSpecificSessionTokens);
    }

    public boolean isPartitionKeyRangeIdPresent(String partitionKeyRangeId) {
        return this.pkRangeIdToRegionScopedSessionTokens.containsKey(partitionKeyRangeId);
    }

    private ISessionToken resolveRegionSpecificSessionToken(String region, String partitionKeyRangeId) {
        ConcurrentHashMap<String, ISessionToken> pkRangeIdSpecificSessionTokenRegistryInner = this.pkRangeIdToRegionScopedSessionTokens.get(partitionKeyRangeId);

        if (pkRangeIdSpecificSessionTokenRegistryInner != null) {
            return pkRangeIdSpecificSessionTokenRegistryInner.get(region);
        }

        return null;
    }

    private ISessionToken mergeSessionTokenRepresentingAllRegions(String partitionKeyRangeId) {
        ConcurrentHashMap<String, ISessionToken> pkRangeIdSpecificSessionTokenRegistryInner = this.pkRangeIdToRegionScopedSessionTokens.get(partitionKeyRangeId);
        List<ISessionToken> sessionTokensAcrossAllRegions = new ArrayList<>();

        if (pkRangeIdSpecificSessionTokenRegistryInner != null) {
            sessionTokensAcrossAllRegions = new ArrayList<>(pkRangeIdSpecificSessionTokenRegistryInner.values());
            return mergeSessionTokenRepresentingSpecificRegions(sessionTokensAcrossAllRegions);
        }

        return null;
    }

    private ISessionToken mergeSessionTokenRepresentingSpecificRegions(List<ISessionToken> sessionTokens) {

        if (sessionTokens == null || sessionTokens.isEmpty()) {
            return null;
        }

        ISessionToken effectiveSessionToken = sessionTokens.get(0);

        for (int i = 1; i < sessionTokens.size(); i++) {
            effectiveSessionToken = effectiveSessionToken.merge(sessionTokens.get(i));
        }

        return effectiveSessionToken;
    }
}
