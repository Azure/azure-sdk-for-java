// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.SASL_JAAS_CONFIG_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.SASL_MECHANISM_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.SECURITY_PROTOCOL_CONFIG_SASL;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.configureOAuthProperties;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.needConfigureSaslOAuth;
import static org.apache.kafka.clients.CommonClientConfigs.DEFAULT_SECURITY_PROTOCOL;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.DEFAULT_SASL_MECHANISM;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AzureKafkaAutoconfigurationUtilsTest {

    @Test
    void testWhenSecurityProtocolNotConfigured() {
        Map<String, Object> sourceConfigs = new HashMap<>();
        Map<String, String> targetConfigs = new HashMap<>();
        assertTrue(needConfigureSaslOAuth(sourceConfigs));
        configureOAuthProperties(targetConfigs);
        shouldConfigureOAuthTargetProperties();
    }

    @Test
    void testWhenSecurityProtocolConfiguredOthers() {
        Map<String, Object> sourceConfigs = new HashMap<>();
        Map<String, String> targetConfigs = new HashMap<>();
        sourceConfigs.put(SECURITY_PROTOCOL_CONFIG, DEFAULT_SECURITY_PROTOCOL);
        assertFalse(needConfigureSaslOAuth(sourceConfigs));
    }

    @Test
    void testWhenSaslMechanismNotConfigured() {
        Map<String, Object> sourceConfigs = new HashMap<>();
        Map<String, String> targetConfigs = new HashMap<>();
        sourceConfigs.put(SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        targetConfigs.put(SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        assertTrue(needConfigureSaslOAuth(sourceConfigs));
        configureOAuthProperties(targetConfigs);
        shouldConfigureOAuthTargetProperties();
    }

    @Test
    void testWhenSaslMechanismConfiguredOthers() {
        Map<String, Object> sourceConfigs = new HashMap<>();
        Map<String, String> targetConfigs = new HashMap<>();
        sourceConfigs.put(SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        sourceConfigs.put(SASL_MECHANISM, DEFAULT_SASL_MECHANISM);
        assertFalse(needConfigureSaslOAuth(sourceConfigs));
    }

    @Test
    void testWhenSaslMechanismConfiguredOAUTH() {
        Map<String, Object> sourceConfigs = new HashMap<>();
        Map<String, String> targetConfigs = new HashMap<>();
        sourceConfigs.put(SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        sourceConfigs.put(SASL_MECHANISM, SASL_MECHANISM_OAUTH);
        targetConfigs.put(SECURITY_PROTOCOL_CONFIG, SECURITY_PROTOCOL_CONFIG_SASL);
        targetConfigs.put(SASL_MECHANISM, SASL_MECHANISM_OAUTH);
        targetConfigs.put(SASL_JAAS_CONFIG, "fake-value");
        targetConfigs.put(SASL_LOGIN_CALLBACK_HANDLER_CLASS, "fake-value");
        assertTrue(needConfigureSaslOAuth(sourceConfigs));
        configureOAuthProperties(targetConfigs);
        shouldConfigureOAuthTargetProperties();
    }

    private void shouldConfigureOAuthTargetProperties() {
        Map<String, Object> sourceConfigs = new HashMap<>();
        Map<String, String> targetConfigs = new HashMap<>();
        assertEquals(SECURITY_PROTOCOL_CONFIG_SASL, targetConfigs.get(SECURITY_PROTOCOL_CONFIG));
        assertEquals(SASL_MECHANISM_OAUTH, targetConfigs.get(SASL_MECHANISM));
        assertEquals(SASL_JAAS_CONFIG_OAUTH, targetConfigs.get(SASL_JAAS_CONFIG));
        assertEquals(SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH, targetConfigs.get(SASL_LOGIN_CALLBACK_HANDLER_CLASS));
    }

}
