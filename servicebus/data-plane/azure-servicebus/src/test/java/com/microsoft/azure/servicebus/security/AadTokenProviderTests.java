// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.security;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;

import com.microsoft.azure.servicebus.security.AzureActiveDirectoryTokenProvider.AuthenticationCallback;

public class AadTokenProviderTests {
    private static final SecurityToken TEST_TOKEN = new SecurityToken(SecurityTokenType.JWT, "testAudience", "tokenString", Instant.now(), Instant.MAX);

    @Test
    public void aadCallbackTokenProviderTest() {
        AuthenticationCallback callback = (String audience, String authority, Object state) -> CompletableFuture.completedFuture(TEST_TOKEN);
        TokenProvider tokenProvider = TokenProvider.createAzureActiveDirectoryTokenProvider(callback, null, null);

        assertEquals(TEST_TOKEN, tokenProvider.getSecurityTokenAsync("testAudience").join());
    }
}
