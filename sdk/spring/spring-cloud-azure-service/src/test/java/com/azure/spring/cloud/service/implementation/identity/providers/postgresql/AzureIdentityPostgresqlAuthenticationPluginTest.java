// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.providers.postgresql;

import com.azure.spring.cloud.service.implementation.identity.AuthProperty;
import com.azure.spring.cloud.service.implementation.identity.AzureAuthenticationTemplate;
import com.azure.spring.cloud.service.implementation.identity.providers.AbstractAuthenticationPluginTest;
import org.junit.jupiter.api.Test;
import org.postgresql.plugin.AuthenticationRequestType;
import org.postgresql.util.PSQLException;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureIdentityPostgresqlAuthenticationPluginTest extends AbstractAuthenticationPluginTest {

    @Test
    void testTokenCredentialProvider() {
        Properties properties = new Properties();
        AzureIdentityPostgresqlAuthenticationPlugin plugin = new AzureIdentityPostgresqlAuthenticationPlugin(properties);
        AzureAuthenticationTemplate authenticationTemplate
            = (AzureAuthenticationTemplate) ReflectionTestUtils.getField(plugin, "azureAuthenticationTemplate");
        assertNotNull(authenticationTemplate);
    }

    @Override
    protected void tokenAudienceShouldConfig() {
        Properties properties = new Properties();
        new AzureIdentityPostgresqlAuthenticationPlugin(properties);
        assertEquals(OSSRDBMS_SCOPES, properties.getProperty(AuthProperty.SCOPES.getPropertyKey()));
    }

    @Test
    void shouldThrowPSQLException() {
        Properties properties = new Properties();
        AzureAuthenticationTemplate template = mock(AzureAuthenticationTemplate.class);
        when(template.getTokenAsPassword()).thenReturn(null);
        AzureIdentityPostgresqlAuthenticationPlugin plugin = new AzureIdentityPostgresqlAuthenticationPlugin(template, properties);
        assertThrowsExactly(PSQLException.class, () -> plugin.getPassword(AuthenticationRequestType.MD5_PASSWORD));
    }

}
