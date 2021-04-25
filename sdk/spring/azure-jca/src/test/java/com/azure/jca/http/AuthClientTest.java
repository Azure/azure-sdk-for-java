// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.jca.http;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The JUnit test for the AuthClient.
 */
@Disabled
public class AuthClientTest {

    @Test
    public void testGetAuthorizationToken() {
        String tenantId = System.getProperty("azure.keyvault.tenant-id");
        String clientId = System.getProperty("azure.keyvault.client-id");
        String clientSecret = System.getProperty("azure.keyvault.client-secret");
        AuthClient authClient = new AuthClient();
        String result = authClient.getAccessToken(
            "https://management.azure.com/",
            System.getProperty("azure.keyvault.aad-authentication-url"),
            tenantId,
            clientId,
            URLEncoder.encode(clientSecret, StandardCharsets.UTF_8)
        );
        assertNotNull(result);
    }
}
