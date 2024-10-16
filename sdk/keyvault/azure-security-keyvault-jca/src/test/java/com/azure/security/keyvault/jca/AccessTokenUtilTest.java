// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca;

import com.azure.security.keyvault.jca.implementation.model.AccessToken;
import com.azure.security.keyvault.jca.implementation.utils.AccessTokenUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import java.net.URI;

import static com.azure.security.keyvault.jca.implementation.utils.AccessTokenUtil.getLoginUri;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.API_VERSION_POSTFIX;
import static com.azure.security.keyvault.jca.implementation.utils.HttpUtil.addTrailingSlashIfRequired;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * The JUnit test for the AuthClient.
 */
@EnabledIfEnvironmentVariable(named = "AZURE_KEYVAULT_CERTIFICATE_NAME", matches = "myalias")
public class AccessTokenUtilTest {
    /**
     * Test getAuthorizationToken method.
     */
    @Test
    public void testGetAuthorizationToken() {
        String tenantId = PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_TENANT_ID");
        String clientId = PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_CLIENT_ID");
        String clientSecret = PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_CLIENT_SECRET");
        String keyVaultEndpoint =
            addTrailingSlashIfRequired(PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_ENDPOINT"));
        String aadAuthenticationUri = getLoginUri(keyVaultEndpoint + "certificates" + API_VERSION_POSTFIX, false);
        AccessToken result =
            AccessTokenUtil.getAccessToken(keyVaultEndpoint, aadAuthenticationUri, tenantId, clientId, clientSecret);

        assertNotNull(result);
    }

    @Test
    public void testGetLoginUri() {
        String keyVaultEndpoint = PropertyConvertorUtils.getPropertyValue("AZURE_KEYVAULT_ENDPOINT");
        String result = getLoginUri(keyVaultEndpoint + "certificates" + API_VERSION_POSTFIX, false);

        assertNotNull(result);
        assertDoesNotThrow(() -> new URI(result));
    }
}
