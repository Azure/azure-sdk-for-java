package com.azure.cosmos.implementation;

import java.util.concurrent.ConcurrentHashMap;

public class RegionBasedSessionTokenRegistry {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, ISessionToken>> regionToPkRangeBasedSessionTokens;

    public RegionBasedSessionTokenRegistry() {
        this.regionToPkRangeBasedSessionTokens = new ConcurrentHashMap<>();
    }
}
