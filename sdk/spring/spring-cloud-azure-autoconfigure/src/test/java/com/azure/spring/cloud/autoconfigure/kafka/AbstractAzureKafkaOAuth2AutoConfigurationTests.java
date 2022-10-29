// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredential;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.ProducerFactory;

import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.SASL_MECHANISM_OAUTH;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.SECURITY_PROTOCOL_CONFIG_SASL;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.buildAzureProperties;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.AZURE_TOKEN_CREDENTIAL;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.SASL_JAAS_CONFIG_OAUTH_PREFIX;
import static org.apache.kafka.clients.CommonClientConfigs.SECURITY_PROTOCOL_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_JAAS_CONFIG;
import static org.apache.kafka.common.config.SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS;
import static org.apache.kafka.common.config.SaslConfigs.SASL_MECHANISM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class AbstractAzureKafkaOAuth2AutoConfigurationTests {
    protected static final String SPRING_BOOT_KAFKA_PROPERTIES_PREFIX = "spring.kafka.properties.";
    protected static final String SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX = "spring.kafka.producer.properties.";
    protected static final String CLIENT_ID = "azure.credential.client-id";

    protected static final String JAAS_PROPERTY_FORMAT = "%s" + SASL_JAAS_CONFIG
        + "=" + SASL_JAAS_CONFIG_OAUTH_PREFIX + " %s=\"%s\";";
    protected static final String MANAGED_IDENTITY_ENABLED = "azure.credential.managed-identity-enabled";

    protected abstract ApplicationContextRunner getContextRunner();
    protected abstract Map<String, Object> getConsumerProperties(ApplicationContext context);
    protected abstract Map<String, Object> getProducerProperties(ApplicationContext context);
    protected abstract Map<String, Object> getAdminProperties(ApplicationContext context);
    protected abstract String getConsumerJaasProperties(ApplicationContext context);
    protected abstract String getProducerJaasProperties(ApplicationContext context);
    protected abstract String getAdminJaasProperties(ApplicationContext context);

    @Test
    void testBindAzureGlobalProperties() {
        getContextRunner()
                .withPropertyValues(
                    "spring.cloud.azure.credential.managed-identity-enabled=true",
                    "spring.kafka.bootstrap-servers=myehnamespace.servicebus.windows.net:9093"
                )
                .run(context -> {
                    AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                    assertTrue(azureGlobalProperties.getCredential().isManagedIdentityEnabled());

                    assertProducerFactoryAndPropsConfigured(context, MANAGED_IDENTITY_ENABLED, MANAGED_IDENTITY_ENABLED + "=\"true\"", ManagedIdentityCredential.class);
                    assertPropertiesConfigured(getAdminProperties(context), getAdminJaasProperties(context),
                            MANAGED_IDENTITY_ENABLED, MANAGED_IDENTITY_ENABLED + "=\"true\"");
                });
    }

    @Test
    void testFactoryConfigureOAuthAndTokenCredentialOnBootCommonConfig() {
        getContextRunner()
            .withPropertyValues(
                SPRING_BOOT_KAFKA_PROPERTIES_PREFIX + MANAGED_IDENTITY_ENABLED + "=true",
                "spring.kafka.bootstrap-servers=myehnamespace.servicebus.windows.net:9093"
            )
            .run(context -> {
                AzureGlobalProperties azureGlobalProperties = context.getBean(AzureGlobalProperties.class);
                assertFalse(azureGlobalProperties.getCredential().isManagedIdentityEnabled());

                assertConsumerFactoryAndPropsConfigured(context, MANAGED_IDENTITY_ENABLED, MANAGED_IDENTITY_ENABLED + "=\"true\"", ManagedIdentityCredential.class);
                assertProducerFactoryAndPropsConfigured(context, MANAGED_IDENTITY_ENABLED, MANAGED_IDENTITY_ENABLED + "=\"true\"", ManagedIdentityCredential.class);
                assertPropertiesConfigured(getAdminProperties(context), getAdminJaasProperties(context),
                    MANAGED_IDENTITY_ENABLED, MANAGED_IDENTITY_ENABLED + "=\"true\"");
            });
    }

    @Test
    void testFactoryConfigureOAuthAndTokenCredentialOnBootClientConfig() {
        getContextRunner()
            .withPropertyValues(
                SPRING_BOOT_KAFKA_PRODUCER_PROPERTIES_PREFIX + MANAGED_IDENTITY_ENABLED + "=true",
                "spring.kafka.bootstrap-servers=myehnamespace.servicebus.windows.net:9093"
            )
            .run(context -> {
                assertConsumerFactoryAndPropsConfigured(context, MANAGED_IDENTITY_ENABLED, MANAGED_IDENTITY_ENABLED + "=\"false\"", DefaultAzureCredential.class);
                assertProducerFactoryAndPropsConfigured(context, MANAGED_IDENTITY_ENABLED, MANAGED_IDENTITY_ENABLED + "=\"true\"", ManagedIdentityCredential.class);
                assertPropertiesConfigured(getAdminProperties(context), getAdminJaasProperties(context),
                    MANAGED_IDENTITY_ENABLED, MANAGED_IDENTITY_ENABLED + "=\"false\"");
            });
    }

    @Test
    @Disabled
    void testBindSpringBootKafkaJaasProperties() {
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

    protected void shouldConfigureOAuthProperties(Map<String, Object> configurationProperties) {
        assertEquals(SECURITY_PROTOCOL_CONFIG_SASL, configurationProperties.get(SECURITY_PROTOCOL_CONFIG));
        assertEquals(SASL_MECHANISM_OAUTH, configurationProperties.get(SASL_MECHANISM));
        assertTrue(((String) configurationProperties.get(SASL_JAAS_CONFIG)).startsWith(SASL_JAAS_CONFIG_OAUTH_PREFIX));
        assertEquals(SASL_LOGIN_CALLBACK_HANDLER_CLASS_OAUTH,
                configurationProperties.get(SASL_LOGIN_CALLBACK_HANDLER_CLASS));
    }

    protected void assertConsumerFactoryAndPropsConfigured(ApplicationContext context, String removedProperty, String filledProperty, Class<?> targetCredentialType) {
        Map<String, Object> consumerProperties = ((DefaultKafkaConsumerFactory<?, ?>) context.getBean(ConsumerFactory.class))
            .getConfigurationProperties();
        assertPropertiesConfigured(consumerProperties, (String) consumerProperties.get(SASL_JAAS_CONFIG),
            removedProperty, filledProperty);
        assertPropertiesConfigured(getConsumerProperties(context), getConsumerJaasProperties(context),
            removedProperty, filledProperty);
        assertCredentialConfigured(consumerProperties, (TokenCredential) context.getBean(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME), targetCredentialType);
    }

    protected void assertProducerFactoryAndPropsConfigured(ApplicationContext context, String removedProperty, String filledProperty, Class<?> targetCredentialType) {
        Map<String, Object> producerProperties = ((DefaultKafkaProducerFactory<?, ?>) context.getBean(ProducerFactory.class))
                .getConfigurationProperties();
        assertPropertiesConfigured(producerProperties, (String) producerProperties.get(SASL_JAAS_CONFIG),
                removedProperty, filledProperty);
        assertPropertiesConfigured(getProducerProperties(context), getProducerJaasProperties(context),
                removedProperty, filledProperty);
        assertCredentialConfigured(producerProperties, (TokenCredential) context.getBean(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME), targetCredentialType);
    }

    protected void assertCredentialConfigured(Map<String, Object> factoryProperties, TokenCredential defaultAzureCredential, Class<?> targetCredentialType) {
        if (targetCredentialType.isAssignableFrom(DefaultAzureCredential.class)) {
            assertEquals(defaultAzureCredential, factoryProperties.get(AZURE_TOKEN_CREDENTIAL));
        } else {
            assertNotEquals(defaultAzureCredential, factoryProperties.get(AZURE_TOKEN_CREDENTIAL));
            assertTrue(targetCredentialType.isInstance(factoryProperties.get(AZURE_TOKEN_CREDENTIAL)));
        }
    }

    protected void assertPropertiesConfigured(Map<String, Object> configs, String jaas, String removedProperty, String filledProperty) {
        shouldConfigureOAuthProperties(configs);
        assertFalse(configs.containsKey(removedProperty));
        assertTrue(jaas.contains(filledProperty));
    }

}
