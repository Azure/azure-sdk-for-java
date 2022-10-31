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
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.configureKafkaOAuth2Properties;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.needConfigureSaslOAuth;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.configureKafkaUserAgent;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaConfigurationUtils.buildAzureProperties;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.AZURE_TOKEN_CREDENTIAL;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.clearAzureProperties;

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
    private final Map<String, Object> consumerCustomizedProperties = new HashMap<>();
    private final Map<String, Object> producerCustomizedProperties = new HashMap<>();

    AzureEventHubsKafkaOAuth2AutoConfiguration(KafkaProperties kafkaProperties,
                                               AzureTokenCredentialResolver resolver,
                                               @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultTokenCredential,
                                               AzureGlobalProperties azureGlobalProperties) {
        this.kafkaProperties = kafkaProperties;
        this.tokenCredentialResolver = resolver;
        this.defaultTokenCredential = defaultTokenCredential;
        this.azureGlobalProperties = azureGlobalProperties;
        //Move customized properties to a new map to parse since we need to remove it from KafkaProperties.
        this.consumerCustomizedProperties.putAll(kafkaProperties.buildConsumerProperties());
        this.producerCustomizedProperties.putAll(kafkaProperties.buildProducerProperties());
        //Configure admin with OAuth2 configs in the ctor instead of customizer bean, since the configuration for KafkaAdmin only accept properties
        if (needConfigureSaslOAuth(kafkaProperties.buildAdminProperties())) {
            configureKafkaOAuth2Properties(kafkaProperties.buildAdminProperties(), azureGlobalProperties,
                    kafkaProperties.getAdmin().getProperties());
        }
        //TODO(yiliu6): refactor this class to KafkaPropertiesBeanPostProcessor
        clearAzurePropertiesInKafkaProperties();
    }

    @Bean
    DefaultKafkaConsumerFactoryCustomizer azureOAuth2KafkaConsumerFactoryCustomizer() {
        Map<String, Object> updateConfigs = new HashMap<>();
        if (needConfigureSaslOAuth(consumerCustomizedProperties)) {
            //Merge all configured consumer OAuth2 properties to KafkaProperties in order to pass to SCS properties
            configureKafkaOAuth2Properties(consumerCustomizedProperties, azureGlobalProperties, kafkaProperties.getConsumer()
                    .getProperties());
            //Pass all merged OAuth2 properties to consumer factory for the usage of Spring Boot Kafka
            updateConfigs.putAll(kafkaProperties.getConsumer().getProperties());
            updateConfigs.put(AZURE_TOKEN_CREDENTIAL, resolveSpringCloudAzureTokenCredential(
                    buildAzureProperties(consumerCustomizedProperties, azureGlobalProperties)));
            configureKafkaUserAgent();
        }
        return factory -> factory.updateConfigs(updateConfigs);
    }

    @Bean
    DefaultKafkaProducerFactoryCustomizer azureOAuth2KafkaProducerFactoryCustomizer() {
        Map<String, Object> updateConfigs = new HashMap<>();
        if (needConfigureSaslOAuth(producerCustomizedProperties)) {
            //Merge all configured producer OAuth2 properties to KafkaProperties in order to pass to SCS properties
            configureKafkaOAuth2Properties(producerCustomizedProperties, azureGlobalProperties, kafkaProperties.getProducer()
                    .getProperties());
            //Pass all merged OAuth2 properties to producer factory for the usage of Spring Boot Kafka
            updateConfigs.putAll(kafkaProperties.getProducer().getProperties());
            updateConfigs.put(AZURE_TOKEN_CREDENTIAL, resolveSpringCloudAzureTokenCredential(
                    buildAzureProperties(producerCustomizedProperties, azureGlobalProperties)));
            configureKafkaUserAgent();
        }
        return factory -> factory.updateConfigs(updateConfigs);
    }

    private TokenCredential resolveSpringCloudAzureTokenCredential(AzurePasswordlessProperties azurePasswordlessProperties) {
        TokenCredential tokenCredential = tokenCredentialResolver.resolve(azurePasswordlessProperties);
        return tokenCredential == null ? defaultTokenCredential : tokenCredential;
    }

    private void clearAzurePropertiesInKafkaProperties() {
        clearAzureProperties(kafkaProperties.getProperties());
        clearAzureProperties(kafkaProperties.getConsumer().getProperties());
        clearAzureProperties(kafkaProperties.getProducer().getProperties());
        clearAzureProperties(kafkaProperties.getAdmin().getProperties());
    }
}
