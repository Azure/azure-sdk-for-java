// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.impl;

import com.azure.core.credential.AccessToken;
import com.azure.spring.cloud.service.implementation.identity.api.Cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class StaticAccessTokenCache implements Cache<String, AccessToken> {
    private static Map<String, AccessToken> cache = new ConcurrentHashMap<>();

    @Override
    public void put(String key, AccessToken value) {
        cache.put(key, value);
    }

    @Override
    public AccessToken get(String key) {
        return cache.get(key);
    }
}
