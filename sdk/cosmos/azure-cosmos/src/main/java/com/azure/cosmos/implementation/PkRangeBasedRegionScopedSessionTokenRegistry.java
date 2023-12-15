// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PkRangeBasedRegionScopedSessionTokenRegistry {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, ISessionToken>> pkRangeIdToRegionScopedSessionTokens;

    public PkRangeBasedRegionScopedSessionTokenRegistry() {
        this.pkRangeIdToRegionScopedSessionTokens = new ConcurrentHashMap<>();
    }

    public ConcurrentHashMap<String, ConcurrentHashMap<String, ISessionToken>> getPkRangeIdToRegionScopedSessionTokens() {
        return this.pkRangeIdToRegionScopedSessionTokens;
    }

    public void tryRecordSessionToken(Map<String, String> sessionTokenToRegionMapping, String pkRangeId, ISessionToken sessionToken) {

        if (sessionTokenToRegionMapping == null || sessionTokenToRegionMapping.isEmpty()) {
            return;
        }

        for (Map.Entry<String, String> sessionTokenToRegion : sessionTokenToRegionMapping.entrySet()) {

            String sessionTokenUnparsedInner = sessionTokenToRegion.getKey();
            String regionInner = sessionTokenToRegion.getValue();

            if (!Strings.isNullOrEmpty(sessionTokenUnparsedInner) && !Strings.isNullOrEmpty(regionInner)) {

                String[] sessionTokenSegments = sessionTokenUnparsedInner.split(":");

                assert sessionTokenSegments.length > 1;

                ISessionToken parsedRegionSpecificSessionToken = SessionTokenHelper.parse(sessionTokenSegments[1]);

                this.pkRangeIdToRegionScopedSessionTokens.compute(pkRangeId, (pkRangeIdAsKey, regionToSessionTokensAsVal) -> {

                    if (regionToSessionTokensAsVal == null) {
                        regionToSessionTokensAsVal = new ConcurrentHashMap<>();
                    }

                    regionToSessionTokensAsVal.merge(regionInner, parsedRegionSpecificSessionToken, ISessionToken::merge);

                    return regionToSessionTokensAsVal;
                });
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
        // case 2 : where the session token for the first preferred writable region
        // has not been recorded - then merge session token for all recorded regions - increases
        // 404/1002 retries but ensures read your own write guarantee
        if (!canUseRegionScopedSessionTokens || sessionTokenForFirstPreferredWritableRegion == null) {
            return resolveSessionTokenRepresentingAllRegions(partitionKeyRangeId);
        }

        regionSpecificSessionTokens.add(sessionTokenForFirstPreferredWritableRegion);

        for (String region : lesserPreferredRegionsPkProbablyRequestedFrom) {
            ISessionToken regionSpecificSessionToken = resolveRegionSpecificSessionToken(region, partitionKeyRangeId);

            if (regionSpecificSessionToken != null) {
                regionSpecificSessionTokens.add(regionSpecificSessionToken);
            }
        }

        return mergeSessionToken(regionSpecificSessionTokens);
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

    private ISessionToken resolveSessionTokenRepresentingAllRegions(String partitionKeyRangeId) {
        ConcurrentHashMap<String, ISessionToken> pkRangeIdSpecificSessionTokenRegistryInner = this.pkRangeIdToRegionScopedSessionTokens.get(partitionKeyRangeId);
        List<ISessionToken> sessionTokensAcrossAllRegions;

        if (pkRangeIdSpecificSessionTokenRegistryInner != null) {
            sessionTokensAcrossAllRegions = pkRangeIdSpecificSessionTokenRegistryInner.values().stream().toList();
            return mergeSessionToken(sessionTokensAcrossAllRegions);
        }

        return null;
    }

    private ISessionToken mergeSessionToken(List<ISessionToken> sessionTokens) {

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
