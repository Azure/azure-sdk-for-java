// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity;

import com.azure.core.credential.AccessToken;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link Cache} that provide static cache.
 */
public final class StaticAccessTokenCache implements Cache<String, AccessToken> {
    private static StaticAccessTokenCache singleInstance;
    private final Map<String, AccessToken> cache = new ConcurrentHashMap<>();

    @Override
    public void put(String key, AccessToken value) {
        cache.put(key, value);
    }

    @Override
    public AccessToken get(String key) {
        return cache.get(key);
    }

    private StaticAccessTokenCache() {
    }

    public static StaticAccessTokenCache getInstance() {

        if (singleInstance == null) {
            synchronized (StaticAccessTokenCache.class) {
                if (singleInstance == null) {
                    singleInstance = new StaticAccessTokenCache();
                }
            }
        }
        return singleInstance;
    }
}
