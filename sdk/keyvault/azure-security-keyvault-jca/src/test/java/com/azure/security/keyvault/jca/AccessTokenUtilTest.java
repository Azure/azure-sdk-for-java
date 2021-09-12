// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.implementation.utils.AccessTokenUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.URLEncoder;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The JUnit test for the AuthClient.
 */
@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class AccessTokenUtilTest {

    /**
     * Test getAuthorizationToken method.
     *
     * @throws Exception when a serious error occurs.
     */
    @Test
    public void testGetAuthorizationToken() throws Exception {
        String tenantId = System.getenv("AZURE_KEYVAULT_TENANT_ID");
        String clientId = System.getenv("AZURE_KEYVAULT_CLIENT_ID");
        String clientSecret = System.getenv("AZURE_KEYVAULT_CLIENT_SECRET");
        String result = AccessTokenUtil.getAccessToken(
            "https://management.azure.com/",
            null,
            tenantId,
            clientId,
            URLEncoder.encode(clientSecret, "UTF-8")
        );
        assertNotNull(result);
    }
}
