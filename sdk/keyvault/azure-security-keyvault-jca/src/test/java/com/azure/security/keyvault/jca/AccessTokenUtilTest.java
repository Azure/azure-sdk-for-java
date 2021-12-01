// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import com.azure.core.util.Configuration;
import com.azure.security.keyvault.jca.implementation.model.AccessToken;
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
        Configuration globalConfiguration = Configuration.getGlobalConfiguration();
        String tenantId = globalConfiguration.get("AZURE_KEYVAULT_TENANT_ID", System.getenv("AZURE_KEYVAULT_TENANT_ID"));
        String clientId = globalConfiguration.get("AZURE_KEYVAULT_CLIENT_ID", System.getenv("AZURE_KEYVAULT_CLIENT_ID"));
        String clientSecret = globalConfiguration.get("AZURE_KEYVAULT_CLIENT_SECRET", System.getenv("AZURE_KEYVAULT_CLIENT_SECRET"));
        String keyVaultEndPoint = globalConfiguration.get("KEY_VAULT_ENDPOINT_SUFFIX", ".vault.azure.net");
        String resourceUrl = ".vault.azure.net".equals(keyVaultEndPoint)
            ? "https://management.azure.com/" : ".vault.usgovcloudapi.net".equals(keyVaultEndPoint)
            ? "https://management.usgovcloudapi.net/" : "https://management.chinacloudapi.cn/";
        String aadAuthenticationUrl = ".vault.azure.net".equals(keyVaultEndPoint)
            ? "https://login.microsoftonline.com/"  : ".vault.usgovcloudapi.net".equals(keyVaultEndPoint)
            ? "https://login.microsoftonline.us/" : "https://login.partner.microsoftonline.cn/";
        AccessToken result = AccessTokenUtil.getAccessToken(
            resourceUrl,
            aadAuthenticationUrl,
            tenantId,
            clientId,
            URLEncoder.encode(clientSecret, "UTF-8")
        );
        assertNotNull(result);
    }
}
