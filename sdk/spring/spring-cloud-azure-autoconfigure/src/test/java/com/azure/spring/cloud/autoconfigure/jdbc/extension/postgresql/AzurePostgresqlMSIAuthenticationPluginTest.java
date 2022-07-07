// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbc.extension.postgresql;


import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.ManagedIdentityCredential;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Properties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzurePostgresqlMSIAuthenticationPluginTest {

    @Test
    void testDefaultScope() {
        AzurePostgresqlMSIAuthenticationPlugin postgresqlPlugin = new AzurePostgresqlMSIAuthenticationPlugin(new Properties());
        String scope = (String) ReflectionTestUtils.getField(postgresqlPlugin, "OSSRDBMS_SCOPE");
        assertEquals("https://ossrdbms-aad.database.windows.net/.default", scope);
    }

    @Test
    void testCreateDefaultTokenCredential() {

        Properties properties = new Properties();
        AzurePostgresqlMSIAuthenticationPlugin postgresqlPlugin = new AzurePostgresqlMSIAuthenticationPlugin(properties);

        TokenCredential getTokenCredential = ReflectionTestUtils.invokeMethod(postgresqlPlugin, "getTokenCredential");
        assertNotNull(getTokenCredential);
        TokenCredential tokenCredential = (TokenCredential) ReflectionTestUtils.getField(postgresqlPlugin, "credential");
        assertNotNull(tokenCredential);
        assertTrue(getTokenCredential instanceof DefaultAzureCredential);
        assertEquals(tokenCredential, getTokenCredential);
    }

    @Test
    void testCreateClientSecretCredential() {

        Properties properties = new Properties();
        properties.put("azure.credential.client-id", "mock-client-id");
        properties.put("azure.credential.client-secret", "mock-client-secret");
        properties.put("azure.profile.tenant-id", "mock-tenant-id");

        AzurePostgresqlMSIAuthenticationPlugin postgresqlPlugin = new AzurePostgresqlMSIAuthenticationPlugin(properties);

        TokenCredential getTokenCredential = ReflectionTestUtils.invokeMethod(postgresqlPlugin, "getTokenCredential");
        assertNotNull(getTokenCredential);
        TokenCredential tokenCredential = (TokenCredential) ReflectionTestUtils.getField(postgresqlPlugin, "credential");
        assertNotNull(tokenCredential);
        assertTrue(getTokenCredential instanceof ClientSecretCredential);
        assertEquals(tokenCredential, getTokenCredential);
    }

    @Test
    void testCreateManagedIdentityCredential() {

        Properties properties = new Properties();
        properties.put("azure.credential.managed-identity-enabled", "true");

        AzurePostgresqlMSIAuthenticationPlugin postgresqlPlugin = new AzurePostgresqlMSIAuthenticationPlugin(properties);

        TokenCredential getTokenCredential = ReflectionTestUtils.invokeMethod(postgresqlPlugin, "getTokenCredential");
        assertNotNull(getTokenCredential);
        TokenCredential tokenCredential = (TokenCredential) ReflectionTestUtils.getField(postgresqlPlugin, "credential");
        assertNotNull(tokenCredential);
        assertTrue(getTokenCredential instanceof ManagedIdentityCredential);
        assertEquals(tokenCredential, getTokenCredential);
    }

}
