// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;

import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.SASL_MECHANISM_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.SECURITY_PROTOCOL_CONFIG_SASL;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.buildAzureProperties;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.SASL_JAAS_CONFIG_OAUTH_PREFIX;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractAzureKafkaOAuth2AutoConfigurationTests {
    protected static final String SPRING_BOOT_KAFKA_PROPERTIES_PREFIX = "spring.kafka.properties.";
    protected static final String SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX = "spring.kafka.producer.properties.";
    protected static final String SPRING_BOOT_KAFKA_CONSUMER_PROPERTIES_PREFIX = "spring.kafka.consumer.properties.";
    protected static final String CLIENT_ID = "azure.credential.client-id";

    protected static final String JAAS_PROPERTY_FORMAT = "%s" + SASL_JAAS_CONFIG
        + "=" + SASL_JAAS_CONFIG_OAUTH_PREFIX + " %s=\"%s\";";
    protected static final String MANAGED_IDENTITY_ENABLED = "azure.credential.managed-identity-enabled";

    protected abstract ApplicationContextRunner getContextRunner();
    protected abstract Map<String, Object> getConsumerProperties(ApplicationContext context);
    protected abstract Map<String, Object> getProducerProperties(ApplicationContext context);

    @Test
    @Disabled
    void testBindSpringBootKafkaProperties() {
        getContextRunner()
                .withPropertyValues(
                        SPRING_BOOT_KAFKA_PROPERTIES_PREFIX + CLIENT_ID + "=kafka-client-id",
                        SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX + CLIENT_ID + "=kafka-producer-client-id",
                        "spring.cloud.azure.credential.client-id=azure-client-id"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureGlobalProperties.class);

                    AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                    assertEquals("azure-client-id", azureGlobalProperties.getCredential().getClientId());
                    AzurePasswordlessProperties azureBuiltKafkaProducerProp = buildAzureProperties(
                            getProducerProperties(context), azureGlobalProperties);
                    assertEquals("kafka-producer-client-id", azureBuiltKafkaProducerProp.getCredential().getClientId());
                    AzurePasswordlessProperties azureBuiltKafkaConsumerProp = buildAzureProperties(
                            getConsumerProperties(context), azureGlobalProperties);
                    assertEquals("kafka-client-id", azureBuiltKafkaConsumerProp.getCredential().getClientId());
                });
    }

    @Test
    void testBindValidSpringBootKafkaJaasProperties() {
        getContextRunner()
            .withPropertyValues(
                String.format(JAAS_PROPERTY_FORMAT, SPRING_BOOT_KAFKA_PROPERTIES_PREFIX, CLIENT_ID, "kafka-client-id"),
                String.format(JAAS_PROPERTY_FORMAT, SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX, CLIENT_ID, "kafka-producer-client-id"),
                "spring.cloud.azure.credential.client-id=azure-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureGlobalProperties.class);

                AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                assertEquals("azure-client-id", azureGlobalProperties.getCredential().getClientId());
                AzurePasswordlessProperties azureBuiltKafkaProducerProp = buildAzureProperties(
                    getProducerProperties(context), azureGlobalProperties);
                assertEquals("kafka-producer-client-id", azureBuiltKafkaProducerProp.getCredential().getClientId());
                AzurePasswordlessProperties azureBuiltKafkaConsumerProp = buildAzureProperties(
                    getConsumerProperties(context), azureGlobalProperties);
                assertEquals("kafka-client-id", azureBuiltKafkaConsumerProp.getCredential().getClientId());
            });
    }

    @Test
    void testBindInvalidSpringBootKafkaJaasProperties() {
        getContextRunner()
            .withPropertyValues(
                String.format(JAAS_PROPERTY_FORMAT, SPRING_BOOT_KAFKA_PROPERTIES_PREFIX, CLIENT_ID, "kafka-client-id"),
                SPRING_BOOT_KAFKA_CONSUMER_PROPERTIES_PREFIX + SASL_JAAS_CONFIG + "=" + CLIENT_ID + "=\"kafka-consumer-client-id\";",
                "spring.cloud.azure.credential.client-id=azure-client-id"
            )
            .run(context -> {
                assertThat(context).hasSingleBean(AzureGlobalProperties.class);

                AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                assertEquals("azure-client-id", azureGlobalProperties.getCredential().getClientId());
                AzurePasswordlessProperties azureBuiltKafkaProducerProp = buildAzureProperties(
                    getProducerProperties(context), azureGlobalProperties);
                assertEquals("kafka-client-id", azureBuiltKafkaProducerProp.getCredential().getClientId());
                AzurePasswordlessProperties azureBuiltKafkaConsumerProp = buildAzureProperties(
                    getConsumerProperties(context), azureGlobalProperties);
                assertEquals("azure-client-id", azureBuiltKafkaConsumerProp.getCredential().getClientId());
            });
    }

    @Test
    void testBindAzureGlobalProperties() {
        getContextRunner()
                .withPropertyValues(
                        "spring.cloud.azure.credential.client-id=azure-client-id"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(AzureGlobalProperties.class);
                    AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                    assertEquals("azure-client-id", azureGlobalProperties.getCredential().getClientId());
                    Map<String, Object> producerProperties = getProducerProperties(context);
                    AzurePasswordlessProperties azureBuiltKafkaProducerProp = buildAzureProperties(
                            producerProperties, azureGlobalProperties);
                    assertEquals("azure-client-id", azureBuiltKafkaProducerProp.getCredential().getClientId());
                    Map<String, Object> consumerProperties = getConsumerProperties(context);
                    AzurePasswordlessProperties azureBuiltKafkaConsumerProp = buildAzureProperties(
                            consumerProperties, azureGlobalProperties);
                    assertEquals("azure-client-id", azureBuiltKafkaConsumerProp.getCredential().getClientId());
                });
    }

    protected void shouldConfigureOAuthProperties(Map<String, Object> configurationProperties) {
        assertEquals(SECURITY_PROTOCOL_CONFIG_SASL, configurationProperties.get(SECURITY_PROTOCOL_CONFIG));
        assertEquals(SASL_MECHANISM_OAUTH, configurationProperties.get(SASL_MECHANISM));
        assertTrue(((String) configurationProperties.get(SASL_JAAS_CONFIG)).startsWith(SASL_JAAS_CONFIG_OAUTH_PREFIX));
        assertEquals(SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH,
                configurationProperties.get(SASL_LOGIN_CALLBACK_HANDLER_CLASS));
    }

}
