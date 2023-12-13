package com.azure.cosmos.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class PkRangeBasedRegionScopedSessionTokenRegistry {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, ISessionToken>> pkRangeIdToRegionScopedSessionTokens;

    public PkRangeBasedRegionScopedSessionTokenRegistry() {
        this.pkRangeIdToRegionScopedSessionTokens = new ConcurrentHashMap<>();
    }

    public void tryRecordSessionToken(String region, String pkRangeId, ISessionToken sessionToken) {
        this.pkRangeIdToRegionScopedSessionTokens.compute(pkRangeId, (pkRangeIdAsKey, regionToSessionTokensAsVal) -> {

            if (regionToSessionTokensAsVal == null) {
                regionToSessionTokensAsVal = new ConcurrentHashMap<>();
            }

            regionToSessionTokensAsVal.merge(region, sessionToken, ISessionToken::merge);

            return regionToSessionTokensAsVal;
        });
    }

    public ISessionToken tryResolveSessionToken(List<String> lesserPreferredRegionsPkProbablyRequestedFrom, String firstPreferredRegion, String pkRangeId) {
        List<ISessionToken> regionSpecificSessionTokens = new ArrayList<>();

        regionSpecificSessionTokens.add(resolveRegionSpecificSessionToken(firstPreferredRegion, pkRangeId));

        for (String region : lesserPreferredRegionsPkProbablyRequestedFrom) {
            ISessionToken regionSpecificSessionToken = resolveRegionSpecificSessionToken(region, pkRangeId);

            if (regionSpecificSessionToken != null) {
                regionSpecificSessionTokens.add(resolveRegionSpecificSessionToken(region, pkRangeId));
            }
        }

        return mergeSessionToken(regionSpecificSessionTokens);
    }

    private ISessionToken resolveRegionSpecificSessionToken(String region, String pkRangeId) {
        ConcurrentHashMap<String, ISessionToken> pkRangeIdSpecificSessionTokenRegistry = this.pkRangeIdToRegionScopedSessionTokens.get(pkRangeId);

        if (pkRangeIdSpecificSessionTokenRegistry != null) {
            return pkRangeIdSpecificSessionTokenRegistry.get(region);
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
