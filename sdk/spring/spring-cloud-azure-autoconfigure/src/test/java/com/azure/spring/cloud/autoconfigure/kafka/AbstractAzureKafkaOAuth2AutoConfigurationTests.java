// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.kafka.AbstractKafkaPropertiesBeanPostProcessor.AZURE_CONFIGURED_JAAS_OPTIONS;
import static com.azure.spring.cloud.autoconfigure.kafka.AbstractKafkaPropertiesBeanPostProcessor.SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH;
import static com.azure.spring.cloud.autoconfigure.kafka.AbstractKafkaPropertiesBeanPostProcessor.SASL_MECHANISM_OAUTH;
import static com.azure.spring.cloud.autoconfigure.kafka.AbstractKafkaPropertiesBeanPostProcessor.SECURITY_PROTOCOL_CONFIG_SASL;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.SASL_JAAS_CONFIG_OAUTH_PREFIX;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractAzureKafkaOAuth2AutoConfigurationTests<P, B> {
    protected static final String SPRING_BOOT_KAFKA_PROPERTIES_PREFIX = "spring.kafka.properties.";
    protected static final String SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX = "spring.kafka.producer.properties.";
    protected static final String CLIENT_ID = "azure.credential.client-id";
    protected static final String MANAGED_IDENTITY_ENABLED = "azure.credential.managed-identity-enabled";

    protected abstract ApplicationContextRunner getContextRunnerWithEventHubsURL();
    protected abstract ApplicationContextRunner getContextRunnerWithoutEventHubsURL();
    protected abstract P getKafkaSpringProperties(ApplicationContext context);

    protected final B processor;

    AbstractAzureKafkaOAuth2AutoConfigurationTests(B processor) {
        this.processor = processor;
    }

    @Test
    void testBindAzureGlobalProperties() {
        getContextRunnerWithEventHubsURL()
            .withPropertyValues(
                "spring.cloud.azure.credential.managed-identity-enabled=true"
            )
            .run(context -> {
                AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                assertTrue(azureGlobalProperties.getCredential().isManagedIdentityEnabled());
                P kafkaSpringProperties = getKafkaSpringProperties(context);
                assertConsumerPropsConfigured(kafkaSpringProperties, MANAGED_IDENTITY_ENABLED, MANAGED_IDENTITY_ENABLED + "=\"true\"");
                assertProducerPropsConfigured(kafkaSpringProperties, MANAGED_IDENTITY_ENABLED, MANAGED_IDENTITY_ENABLED + "=\"true\"");
                assertAdminPropsConfigured(kafkaSpringProperties, MANAGED_IDENTITY_ENABLED, MANAGED_IDENTITY_ENABLED + "=\"true\"");
            });
    }


    @Test
    void testNotBindSpringBootKafkaProperties() {
        getContextRunnerWithoutEventHubsURL()
            .withPropertyValues(
                SPRING_BOOT_KAFKA_PROPERTIES_PREFIX + MANAGED_IDENTITY_ENABLED + "=true"
            )
            .run(context -> {
                AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                assertFalse(azureGlobalProperties.getCredential().isManagedIdentityEnabled());
                P kafkaSpringProperties = getKafkaSpringProperties(context);
                Map<String, Object> consumerProperties = getConsumerProperties(kafkaSpringProperties);
                assertFalse(consumerProperties.containsKey(MANAGED_IDENTITY_ENABLED));
                String adminJaasProperties = getAdminJaasProperties(kafkaSpringProperties);
                assertNull(adminJaasProperties);
            });
    }

    protected Map<String, Object> getProducerProperties(P properties) {
        return ReflectionTestUtils.invokeMethod(processor,
            "getMergedProducerProperties", properties);
    }

    protected Map<String, Object> getConsumerProperties(P properties) {
        return ReflectionTestUtils.invokeMethod(processor,
            "getMergedConsumerProperties", properties);
    }

    protected Map<String, Object> getAdminProperties(P properties) {
        return ReflectionTestUtils.invokeMethod(processor,
            "getMergedAdminProperties", properties);
    }

    protected String getProducerJaasProperties(P properties) {
        return (String) getProducerProperties(properties).get(SASL_JAAS_CONFIG);
    }

    protected String getConsumerJaasProperties(P properties) {
        return (String) getConsumerProperties(properties).get(SASL_JAAS_CONFIG);
    }

    protected String getAdminJaasProperties(P properties) {
        return (String) getAdminProperties(properties).get(SASL_JAAS_CONFIG);
    }

    protected void shouldConfigureOAuthProperties(Map<String, Object> configurationProperties) {
        assertEquals(SECURITY_PROTOCOL_CONFIG_SASL, configurationProperties.get(SECURITY_PROTOCOL_CONFIG));
        assertEquals(SASL_MECHANISM_OAUTH, configurationProperties.get(SASL_MECHANISM));
        assertTrue(((String) configurationProperties.get(SASL_JAAS_CONFIG)).startsWith(SASL_JAAS_CONFIG_OAUTH_PREFIX));
        assertTrue(((String) configurationProperties.get(SASL_JAAS_CONFIG)).contains(AZURE_CONFIGURED_JAAS_OPTIONS));
        assertEquals(SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH,
                configurationProperties.get(SASL_LOGIN_CALLBACK_HANDLER_CLASS));
    }

    protected void assertConsumerPropsConfigured(P properties, String removedProperty, String filledProperty) {
        assertPropertiesConfigured(getConsumerProperties(properties), getConsumerJaasProperties(properties),
            removedProperty, filledProperty);
    }

    protected void assertProducerPropsConfigured(P properties, String removedProperty, String filledProperty) {
        assertPropertiesConfigured(getProducerProperties(properties), getProducerJaasProperties(properties),
            removedProperty, filledProperty);
    }

    protected void assertAdminPropsConfigured(P properties, String removedProperty, String filledProperty) {
        assertPropertiesConfigured(getAdminProperties(properties), getAdminJaasProperties(properties),
            removedProperty, filledProperty);
    }

    protected void assertPropertiesConfigured(Map<String, Object> configs, String jaas, String removedProperty, String filledProperty) {
        shouldConfigureOAuthProperties(configs);
        assertFalse(configs.containsKey(removedProperty));
        assertTrue(jaas.contains(filledProperty));
    }

}
