package com.azure.identity.extensions.implementation.cache;

import com.azure.core.credential.TokenCredential;

import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTokenCredentialCache implements IdentityCache<String, TokenCredential> {

    private static final ConcurrentHashMap<String, TokenCredential> CACHE = new ConcurrentHashMap<>();

    @Override
    public synchronized void put(String key, TokenCredential value) {
        CACHE.putIfAbsent(key, value);
    }

    @Override
    public TokenCredential get(String key) {
        return CACHE.get(key);
    }

    @Override
    public synchronized void remove(String key) {
        CACHE.remove(key);
    }

}
