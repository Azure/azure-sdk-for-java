// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.postgresql;

import com.azure.identity.providers.jdbc.implementation.enums.AuthProperty;
import com.azure.identity.providers.jdbc.implementation.template.AzureAuthenticationTemplate;
import org.junit.jupiter.api.Test;
import org.postgresql.plugin.AuthenticationRequestType;
import org.postgresql.util.PSQLException;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class AzureIdentityPostgresqlAuthenticationPluginTest {
    private static final String OSSRDBMS_SCOPES = "https://ossrdbms-aad.database.windows.net/.default";

    @Test
    void testTokenCredentialProvider() {
        Properties properties = new Properties();
        AzureIdentityPostgresqlAuthenticationPlugin plugin = new AzureIdentityPostgresqlAuthenticationPlugin(properties);
        assertNotNull(plugin.getAzureAuthenticationTemplate());
    }

    @Test
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

    @Test
    void testGetPassword() throws PSQLException {
        Properties properties = new Properties();
        AzureAuthenticationTemplate template = mock(AzureAuthenticationTemplate.class);
        when(template.getTokenAsPassword()).thenReturn("fake-password");
        AzureIdentityPostgresqlAuthenticationPlugin plugin = new AzureIdentityPostgresqlAuthenticationPlugin(template, properties);
        assertEquals(new String(plugin.getPassword(AuthenticationRequestType.MD5_PASSWORD)), template.getTokenAsPassword());
    }

}
