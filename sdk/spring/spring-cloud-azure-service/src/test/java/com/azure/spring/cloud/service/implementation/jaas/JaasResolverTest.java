// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.jaas;

import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JaasResolverTest {

    @Test
    void testResolveJaasWithInvalidPattern() {
        JaasResolver resolver = new JaasResolver();
        assertThrows(NoSuchElementException.class, () -> resolver.resolve("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required").get());
        assertThrows(NoSuchElementException.class, () -> resolver.resolve("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModulerequired;").get());
        assertThrows(NoSuchElementException.class, () -> resolver.resolve("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule require;").get());
        assertThrows(NoSuchElementException.class, () -> resolver.resolve("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required test;").get());
    }

    @Test
    void testResolveJaasWithoutAzureProperties() {
        JaasResolver resolver = new JaasResolver();
        Jaas jaas = resolver.resolve("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;").get();
        assertEquals("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule", jaas.getLoginModule());
        assertEquals(Jaas.ControlFlag.REQUIRED, jaas.getControlFlag());
        assertTrue(jaas.getOptions().isEmpty());
    }

    @Test
    void testResolveJaasWithAzureProperties() {
        JaasResolver resolver = new JaasResolver();
        Jaas jaas = resolver.resolve("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required "
            + "azure.credential.managed-identity-enabled=\"true\" azure.credential.client-id=\"test\" azure.profile.cloud-type=\"azure\";").get();
        assertEquals(3, jaas.getOptions().size());
        assertEquals("true", jaas.getOptions().get("azure.credential.managed-identity-enabled"));
        assertEquals("test", jaas.getOptions().get("azure.credential.client-id"));
        assertEquals(Jaas.ControlFlag.REQUIRED, jaas.getControlFlag());
        assertEquals(OAuthBearerLoginModule.class.getName(), jaas.getLoginModule());
    }
}
