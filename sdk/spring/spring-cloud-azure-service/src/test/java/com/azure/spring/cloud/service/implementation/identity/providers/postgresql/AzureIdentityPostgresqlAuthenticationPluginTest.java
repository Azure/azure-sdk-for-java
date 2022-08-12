// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.providers.postgresql;

import com.azure.spring.cloud.service.implementation.identity.AuthProperty;
import com.azure.spring.cloud.service.implementation.identity.AzureAuthenticationTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AzureIdentityPostgresqlAuthenticationPluginTest {

    private static final String OSSRDBMS_SCOPES = "https://ossrdbms-aad.database.windows.net/.default";

    @Test
    void testTokenCredentialProvider() {
        Properties properties = new Properties();
        AzureIdentityPostgresqlAuthenticationPlugin plugin = new AzureIdentityPostgresqlAuthenticationPlugin(properties);
        AzureAuthenticationTemplate authenticationTemplate
            = (AzureAuthenticationTemplate) ReflectionTestUtils.getField(plugin, "azureAuthenticationTemplate");
        assertNotNull(authenticationTemplate);
    }

    @Test
    void testTokenAudienceShouldConfig() {
        Properties properties = new Properties();
        new AzureIdentityPostgresqlAuthenticationPlugin(properties);
        assertEquals(OSSRDBMS_SCOPES, properties.getProperty(AuthProperty.SCOPES.getPropertyKey()));
    }

}
