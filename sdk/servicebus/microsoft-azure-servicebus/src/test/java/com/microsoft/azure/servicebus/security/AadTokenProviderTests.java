// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.servicebus.security;

import static org.junit.Assert.assertEquals;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.junit.Test;

import com.microsoft.azure.servicebus.TestUtils;
import com.microsoft.azure.servicebus.security.AzureActiveDirectoryTokenProvider.AuthenticationCallback;

public class AadTokenProviderTests {
    
    @Test
    public void aadCallbackTokenProviderTest() {
        String TEST_TOKEN = "eyJhbGciOiJIUzI1NiJ9.e30.ZRrHA1JJJW8opsbCGfG_HACGpVUMN_a9IV7pAx_Zmeo";
        String TEST_AUDIENCE = "testAudience";
        String TEST_AUTHORITY = "https://login.microsoftonline.com/common";
        
        AuthenticationCallback callback = (String audience, String authority, Object state) -> {
            assertEquals(SecurityConstants.SERVICEBUS_AAD_AUDIENCE_RESOURCE_URL, audience);
            assertEquals(TEST_AUTHORITY, authority);
            return CompletableFuture.completedFuture(TEST_TOKEN);
        };
        TokenProvider tokenProvider = TokenProvider.createAzureActiveDirectoryTokenProvider(callback, TEST_AUTHORITY, null);
        SecurityToken token = tokenProvider.getSecurityTokenAsync(TEST_AUDIENCE).join();
        assertEquals(TEST_TOKEN, token.getTokenValue());
        assertEquals(TEST_AUDIENCE, token.getTokenAudience());

        // Should throw when null callback is provided
        TestUtils.assertThrows(IllegalArgumentException.class, () -> {
            TokenProvider.createAzureActiveDirectoryTokenProvider(null, TEST_AUTHORITY, null);
        });

        // Should throw when null authority is provided
        TestUtils.assertThrows(IllegalArgumentException.class, () -> {
            TokenProvider.createAzureActiveDirectoryTokenProvider(callback, null, null);
        });
    }
}
