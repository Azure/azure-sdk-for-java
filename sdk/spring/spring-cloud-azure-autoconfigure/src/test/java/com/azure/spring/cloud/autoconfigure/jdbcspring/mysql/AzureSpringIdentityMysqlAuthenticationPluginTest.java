// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.jdbcspring.mysql;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.spring.cloud.autoconfigure.jdbc.extension.mysql.AzureIdentityMysqlAuthenticationPlugin;
import com.mysql.cj.conf.DefaultPropertySet;
import com.mysql.cj.conf.PropertySet;
import com.mysql.cj.protocol.a.NativeProtocol;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AzureSpringIdentityMysqlAuthenticationPluginTest {

    //TODO
    @Test
    void testDefaultScope() {
        AzureIdentityMysqlAuthenticationPlugin mysqlPlugin = new AzureIdentityMysqlAuthenticationPlugin();
        String scope = (String) ReflectionTestUtils.getField(mysqlPlugin, "OSSRDBMS_SCOPE");
        assertEquals("https://ossrdbms-aad.database.windows.net/.default", scope);
    }

    @Test
    void testCreateDefaultTokenCredential() {

        NativeProtocol protocol = mock(NativeProtocol.class);
        PropertySet propertySet = new DefaultPropertySet();
        when(protocol.getPropertySet()).thenReturn(propertySet);
        AzureIdentityMysqlAuthenticationPlugin mysqlPlugin = new AzureIdentityMysqlAuthenticationPlugin();
        mysqlPlugin.init(protocol);

        TokenCredential getTokenCredential = ReflectionTestUtils.invokeMethod(mysqlPlugin, "getTokenCredential");
        assertNotNull(getTokenCredential);
        TokenCredential tokenCredential = (TokenCredential) ReflectionTestUtils.getField(mysqlPlugin, "credential");
        assertNotNull(tokenCredential);
        assertTrue(getTokenCredential instanceof DefaultAzureCredential);
        assertEquals(tokenCredential, getTokenCredential);
    }


    @Test
    void testCreateClientSecretCredential() {

        NativeProtocol protocol = mock(NativeProtocol.class);
        PropertySet propertySet = new DefaultPropertySet();
        Properties properties = new Properties();
        properties.put("azure.credential.client-id", "mock-client-id");
        properties.put("azure.credential.client-secret", "mock-client-secret");
        properties.put("azure.profile.tenant-id", "mock-tenant-id");
        propertySet.initializeProperties(properties);
        when(protocol.getPropertySet()).thenReturn(propertySet);
        AzureIdentityMysqlAuthenticationPlugin mysqlPlugin = new AzureIdentityMysqlAuthenticationPlugin();
        mysqlPlugin.init(protocol);

        TokenCredential getTokenCredential = ReflectionTestUtils.invokeMethod(mysqlPlugin, "getTokenCredential");
        assertNotNull(getTokenCredential);
        TokenCredential tokenCredential = (TokenCredential) ReflectionTestUtils.getField(mysqlPlugin, "credential");
        assertNotNull(tokenCredential);
        assertTrue(getTokenCredential instanceof ClientSecretCredential);
        assertEquals(tokenCredential, getTokenCredential);
    }

    @Test
    void testCreateManagedIdentityCredential() {

        NativeProtocol protocol = mock(NativeProtocol.class);
        PropertySet propertySet = new DefaultPropertySet();
        Properties properties = new Properties();
        properties.put("azure.credential.managed-identity-enabled", "true");
        propertySet.initializeProperties(properties);
        when(protocol.getPropertySet()).thenReturn(propertySet);
        AzureIdentityMysqlAuthenticationPlugin mysqlPlugin = new AzureIdentityMysqlAuthenticationPlugin();
        mysqlPlugin.init(protocol);

        TokenCredential getTokenCredential = ReflectionTestUtils.invokeMethod(mysqlPlugin, "getTokenCredential");
        assertNotNull(getTokenCredential);
        TokenCredential tokenCredential = (TokenCredential) ReflectionTestUtils.getField(mysqlPlugin, "credential");
        assertNotNull(tokenCredential);
        assertTrue(getTokenCredential instanceof ManagedIdentityCredential);
        assertEquals(tokenCredential, getTokenCredential);
    }

}
