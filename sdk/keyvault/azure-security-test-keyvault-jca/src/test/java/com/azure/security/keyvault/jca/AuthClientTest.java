// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.URLEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The JUnit test for the AuthClient.
 */
@EnabledIfEnvironmentVariable(named = "azure.keyvault.certificate-name", matches = ".*")
public class AuthClientTest {

    /**
     * Test getAuthorizationToken method.
     * 
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testGetAuthorizationToken() throws Exception {
        String tenantId = System.getenv("azure.keyvault.tenant-id");
        String clientId = System.getenv("azure.keyvault.client-id");
        String clientSecret = System.getenv("azure.keyvault.client-secret");
        AuthClient authClient = new AuthClient();
        String result = authClient.getAccessToken(
            "https://management.azure.com/",
            System.getProperty("azure.keyvault.aad-authentication-url"),
            tenantId,
            clientId,
            URLEncoder.encode(clientSecret, "UTF-8")
        );
        assertNotNull(result);
    }
}
