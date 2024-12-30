package com.azure.identity.extensions.implementation.cache;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.extensions.implementation.credential.TokenCredentialProviderOptions;

import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTokenCredentialCache implements TokenCredentialCache {
    private static final ConcurrentHashMap<String, TokenCredential> cache = new ConcurrentHashMap<>();

    @Override
    public void put(TokenCredentialProviderOptions options, TokenCredential value) {
        cache.putIfAbsent(getKey(options), value);
    }

    @Override
    public TokenCredential get(TokenCredentialProviderOptions options) {
        return cache.get(getKey(options));
    }

    @Override
    public void remove(TokenCredentialProviderOptions options) {
        cache.remove(getKey(options));
    }
}
