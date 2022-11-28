// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.jaas;

import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JaasResolverTest {

    @Test
    void testResolveJaasWithInvalidLoginModule() {
        Jaas jaas = JaasResolver.resolve("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required");
        assertNull(jaas.getLoginModule());
        assertNull(jaas.getControlFlag());
        assertTrue(jaas.getOptions().isEmpty());

        jaas = JaasResolver.resolve("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModulerequired;");
        assertNull(jaas.getLoginModule());
        assertNull(jaas.getControlFlag());
        assertTrue(jaas.getOptions().isEmpty());

        jaas = JaasResolver.resolve(null);
        assertNull(jaas.getLoginModule());
        assertNull(jaas.getControlFlag());
        assertTrue(jaas.getOptions().isEmpty());
    }

    @Test
    void testResolveJaasWithoutAzureProperties() {
        Jaas jaas = JaasResolver.resolve("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;");
        assertEquals("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule", jaas.getLoginModule());
        assertEquals("required", jaas.getControlFlag());
        assertTrue(jaas.getOptions().isEmpty());
    }

    @Test
    void testResolveJaasWithAzureProperties() {
        Jaas jaas = JaasResolver.resolve("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required "
            + "azure.credential.managed-identity-enabled=\"true\" azure.credential.client-id=\"test\" azure.profile.cloud-type=\"azure\";");
        assertEquals(3, jaas.getOptions().size());
        assertEquals("true", jaas.getOptions().get("azure.credential.managed-identity-enabled"));
        assertEquals("test", jaas.getOptions().get("azure.credential.client-id"));
        assertEquals("required", jaas.getControlFlag());
        assertEquals(OAuthBearerLoginModule.class.getName(), jaas.getLoginModule());
    }
}
