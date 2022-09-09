// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.service.implementation.passwordless.AzurePasswordlessProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.KAFKA_OAUTH_CONFIGS;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.buildAzureProperties;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.configureKafkaUserAgent;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.logConfigureOAuthProperties;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.needConfigureSaslOAuth;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.AZURE_TOKEN_CREDENTIAL;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Event Hubs Kafka support. Provide Azure Identity-based
 * OAuth2 authentication for Event Hubs for Kafka on the basis of Spring Boot Autoconfiguration.
 *
 * @since 4.3.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(KafkaTemplate.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventhubs.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class AzureEventHubsKafkaOAuth2AutoConfiguration {

    private final KafkaProperties kafkaProperties;
    private final AzureTokenCredentialResolver tokenCredentialResolver;
    private final AzureGlobalProperties azureGlobalProperties;
    private final TokenCredential defaultTokenCredential;

    AzureEventHubsKafkaOAuth2AutoConfiguration(KafkaProperties kafkaProperties,
                                               AzureTokenCredentialResolver resolver,
                                               @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultTokenCredential,
                                               AzureGlobalProperties azureGlobalProperties) {
        this.kafkaProperties = kafkaProperties;
        this.tokenCredentialResolver = resolver;
        this.defaultTokenCredential = defaultTokenCredential;
        this.azureGlobalProperties = azureGlobalProperties;
    }

    @Bean
    DefaultKafkaConsumerFactoryCustomizer azureOAuth2KafkaConsumerFactoryCustomizer() {
        Map<String, Object> updateConfigs = new HashMap<>();
        Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();
        if (needConfigureSaslOAuth(consumerProperties)) {
            configureOAuth2Properties(consumerProperties, updateConfigs);
            configureKafkaUserAgent();
        }
        return factory -> factory.updateConfigs(updateConfigs);
    }

    @Bean
    DefaultKafkaProducerFactoryCustomizer azureOAuth2KafkaProducerFactoryCustomizer() {
        Map<String, Object> updateConfigs = new HashMap<>();
        Map<String, Object> producerProperties = kafkaProperties.buildProducerProperties();
        if (needConfigureSaslOAuth(producerProperties)) {
            configureOAuth2Properties(producerProperties, updateConfigs);
            configureKafkaUserAgent();
        }
        return factory -> factory.updateConfigs(updateConfigs);
    }


    private void configureOAuth2Properties(Map<String, Object> sourceKafkaProperties, Map<String, Object> updateConfigs) {
        AzurePasswordlessProperties azurePasswordlessProperties = buildAzureProperties(sourceKafkaProperties,
                azureGlobalProperties);
        updateConfigs.put(AZURE_TOKEN_CREDENTIAL, resolveSpringCloudAzureTokenCredential(azurePasswordlessProperties));
        updateConfigs.putAll(KAFKA_OAUTH_CONFIGS);
        logConfigureOAuthProperties();
    }

    private TokenCredential resolveSpringCloudAzureTokenCredential(AzurePasswordlessProperties azurePasswordlessProperties) {
        TokenCredential tokenCredential = tokenCredentialResolver.resolve(azurePasswordlessProperties);
        return tokenCredential == null ? defaultTokenCredential : tokenCredential;
    }

}
