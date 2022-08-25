// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.cache;

import com.azure.core.credential.AccessToken;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StaticAccessTokenCacheTest {
    StaticAccessTokenCache instance = StaticAccessTokenCache.getInstance();
    private String cacheKey = "cacheKey";

    @BeforeEach
    void setUp() {
        AccessToken accessToken01 = mock(AccessToken.class);
        when(accessToken01.getToken()).thenReturn("accessToken01");

        Assertions.assertNotNull(instance);
        instance.put(cacheKey, accessToken01);
    }

    @Test
    void testGetTokenFromCache() {
        AccessToken tokenFromCache = instance.get(cacheKey);
        Assertions.assertEquals(tokenFromCache.getToken(), "accessToken01");

        AccessToken accessToken02 = mock(AccessToken.class);
        when(accessToken02.getToken()).thenReturn("accessToken02");
        instance.put(cacheKey, accessToken02);

        tokenFromCache = instance.get(cacheKey);
        Assertions.assertNotEquals(tokenFromCache.getToken(), "accessToken01");
        Assertions.assertEquals(tokenFromCache.getToken(), "accessToken02");
    }

}
