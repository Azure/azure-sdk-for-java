// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.service.implementation.jaas;

import com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils;
import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.JAAS_OPTIONS_PATTERN;
import static org.junit.jupiter.api.Assertions.*;

class JaasTest {

    @Test
    void testToString() {
        Jaas jaas = new Jaas("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule");
        String jaasStr = jaas.toString();
        assertEquals(AzureKafkaPropertiesUtils.SASL_JAAS_CONFIG_OAUTH_PREFIX + ";", jaasStr);
    }

    @Test
    void testToStringWithNullParameters() {
        Jaas jaas = new Jaas(null, null);
        assertNull(jaas.getControlFlag());
        assertNull(jaas.getLoginModule());
        assertTrue(jaas.getOptions().isEmpty());
        String jaasStr = jaas.toString();
        assertEquals(" ;", jaasStr);
    }

    @Test
    void testToStringWithOptions() {
        Jaas jaas = new Jaas("org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule");
        jaas.getOptions().put("azure.credential.client-id", "test");
        jaas.getOptions().put("azure.credential.managed-identity-enabled", "true");
        String jaasStr = jaas.toString();
        assertTrue(jaasStr.startsWith(AzureKafkaPropertiesUtils.SASL_JAAS_CONFIG_OAUTH_PREFIX));
        assertTrue(jaasStr.contains(String.format(JAAS_OPTIONS_PATTERN, "azure.credential.client-id", "test")));
        assertTrue(jaasStr.contains(String.format(JAAS_OPTIONS_PATTERN, "azure.credential.managed-identity-enabled", "true")));
    }
}
