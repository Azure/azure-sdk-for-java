// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.jaas;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JaasTest {

    @Test
    void testToString() {
        Jaas jaas = new Jaas("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule");
        String jaasStr = jaas.toString();
        assertEquals("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required;", jaasStr);
    }

    @Test
    void testToStringWithNullParameters() {
        assertThrows(IllegalArgumentException.class, () -> new Jaas(null, null));
    }

    @Test
    void testToStringWithOptions() {
        Jaas jaas = new Jaas("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule");
        jaas.getOptions().put("azure.credential.client-id", "test");
        jaas.getOptions().put("azure.credential.managed-identity-enabled", "true");
        String jaasStr = jaas.toString();
        assertTrue(jaasStr.startsWith("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required"));
        assertTrue(jaasStr.contains("azure.credential.client-id=\"test\""));
        assertTrue(jaasStr.contains("azure.credential.managed-identity-enabled=\"true\""));
    }
}
