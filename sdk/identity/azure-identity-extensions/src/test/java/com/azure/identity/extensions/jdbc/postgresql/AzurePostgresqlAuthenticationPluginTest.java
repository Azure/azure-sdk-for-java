// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.extensions.jdbc.postgresql;

import com.azure.identity.extensions.implementation.template.AzureAuthenticationTemplate;
import org.junit.jupiter.api.Test;
import org.postgresql.plugin.AuthenticationRequestType;
import org.postgresql.util.PSQLException;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class AzurePostgresqlAuthenticationPluginTest {

    @Test
    void testTokenCredentialProvider() {
        Properties properties = new Properties();
        AzurePostgresqlAuthenticationPlugin plugin = new AzurePostgresqlAuthenticationPlugin(properties);
        assertNotNull(plugin.getAzureAuthenticationTemplate());
    }

    @Test
    void shouldThrowPSQLException() {
        Properties properties = new Properties();
        AzureAuthenticationTemplate template = mock(AzureAuthenticationTemplate.class);
        when(template.getTokenAsPassword()).thenReturn(null);
        AzurePostgresqlAuthenticationPlugin plugin = new AzurePostgresqlAuthenticationPlugin(template, properties);
        assertThrowsExactly(PSQLException.class, () -> plugin.getPassword(AuthenticationRequestType.MD5_PASSWORD));
    }

    @Test
    void testGetPassword() throws PSQLException {
        Properties properties = new Properties();
        AzureAuthenticationTemplate template = mock(AzureAuthenticationTemplate.class);
        when(template.getTokenAsPassword()).thenReturn("fake-password");
        AzurePostgresqlAuthenticationPlugin plugin = new AzurePostgresqlAuthenticationPlugin(template, properties);
        assertEquals(new String(plugin.getPassword(AuthenticationRequestType.MD5_PASSWORD)), template.getTokenAsPassword());
    }

}
