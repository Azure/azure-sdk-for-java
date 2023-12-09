package com.azure.cosmos.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RegionBasedSessionTokenRegistry {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, ISessionToken>> regionToPkRangeBasedSessionTokens;

    public RegionBasedSessionTokenRegistry() {
        this.regionToPkRangeBasedSessionTokens = new ConcurrentHashMap<>();
    }

    public void tryRecordSessionToken(String region, String pkRangeId, ISessionToken sessionToken) {
        this.regionToPkRangeBasedSessionTokens.compute(region, (regionAsKey, pkRangeIdToSessionTokensAsVal) -> {

            if (pkRangeIdToSessionTokensAsVal == null) {
                pkRangeIdToSessionTokensAsVal = new ConcurrentHashMap<>();
            }

            pkRangeIdToSessionTokensAsVal.merge(pkRangeId, sessionToken, ISessionToken::merge);

            return pkRangeIdToSessionTokensAsVal;
        });
    }

    public ISessionToken tryResolveSessionToken(List<String> lesserPreferredRegionsPkProbablyRequestedFrom, String firstPreferredRegion, String pkRangeId) {
        List<ISessionToken> regionSpecificSessionTokens = new ArrayList<>();

        regionSpecificSessionTokens.add(resolveRegionSpecificSessionToken(firstPreferredRegion, pkRangeId));

        for (String region : lesserPreferredRegionsPkProbablyRequestedFrom) {
            regionSpecificSessionTokens.add(resolveRegionSpecificSessionToken(region, pkRangeId));
        }

        return mergeSessionToken(regionSpecificSessionTokens);
    }

    private ISessionToken resolveRegionSpecificSessionToken(String region, String pkRangeId) {
        ConcurrentHashMap<String, ISessionToken> regionSpecificSessionTokenRegistry = this.regionToPkRangeBasedSessionTokens.get(region);

        if (regionSpecificSessionTokenRegistry != null) {
            return regionSpecificSessionTokenRegistry.get(pkRangeId);
        }

        return null;
    }

    private ISessionToken mergeSessionToken(List<ISessionToken> sessionTokens) {

        if (sessionTokens.isEmpty() || sessionTokens == null) {
            return null;
        }

        ISessionToken effectiveSessionToken = sessionTokens.get(0);

        for (int i = 1; i < sessionTokens.size(); i++) {
            effectiveSessionToken = effectiveSessionToken.merge(sessionTokens.get(i));
        }

        return effectiveSessionToken;
    }
}
