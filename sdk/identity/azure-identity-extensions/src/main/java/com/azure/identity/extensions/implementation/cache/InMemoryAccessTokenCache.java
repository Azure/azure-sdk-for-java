package com.azure.identity.extensions.implementation.cache;

import com.azure.core.credential.AccessToken;

import java.util.concurrent.ConcurrentHashMap;

public class InMemoryAccessTokenCache implements IdentityCache<String, AccessToken> {

    private static final ConcurrentHashMap<String, AccessToken> cache = new ConcurrentHashMap<>();

    @Override
    public synchronized void put(String key, AccessToken value) {
        cache.putIfAbsent(key, value);
    }

    @Override
    public AccessToken get(String key) {
        return cache.get(key);
    }

    @Override
    public synchronized void remove(String key) {
        cache.remove(key);
    }

}
