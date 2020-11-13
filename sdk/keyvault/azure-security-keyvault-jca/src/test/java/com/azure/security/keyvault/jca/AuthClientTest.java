// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Test;

import java.net.URLEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The JUnit test for the AuthClient.
 */
public class AuthClientTest {

    /**
     * Test getAuthorizationToken method.
     * 
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testGetAuthorizationToken() throws Exception {
        String tenantId = System.getProperty("azure.tenant.id");
        String clientId = System.getProperty("azure.client.id");
        String clientSecret = System.getProperty("azure.client.secret");
        AuthClient authClient = new AuthClient();
        String result = authClient.getAccessToken(
            "https://management.azure.com/",
            System.getProperty("azure.keyvault.aadAuthenticationUrl"),
            tenantId,
            clientId,
            URLEncoder.encode(clientSecret, "UTF-8")
        );
        assertNotNull(result);
    }
}
