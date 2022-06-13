// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.implementation.properties.AzureThirdPartyServiceProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaConsumerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.DefaultKafkaProducerFactoryCustomizer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.cloud.stream.binder.kafka.config.KafkaBinderConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.KAFKA_OAUTH_CONFIGS;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.buildAzureProperties;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.needConfigureSaslOAuth;
import static com.azure.spring.cloud.core.implementation.util.AzureConfigUtils.AZURE_TOKEN_CREDENTIAL;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Event Hubs Kafka support. Provide Managed Identity-based
 * OAuth2 authentication for Event Hubs for Kafka on the basis of Spring Boot Autoconfiguration.
 *
 * @since 4.3.0
 */
@Configuration(proxyBeanMethods = false)
@Import(AzureEventHubsKafkaOAUTH2Configuration.AzureKafkaSpringCloudStreamAutoConfiguration.class)
@ConditionalOnClass(KafkaTemplate.class)
@AutoConfigureAfter(AzureTokenCredentialAutoConfiguration.class)
public class AzureEventHubsKafkaOAUTH2Configuration {

    private final KafkaProperties kafkaProperties;
    private final AzureTokenCredentialResolver tokenCredentialResolver;
    private final AzureGlobalProperties azureGlobalProperties;
    private final TokenCredential defaultTokenCredential;


    AzureEventHubsKafkaOAUTH2Configuration(KafkaProperties kafkaProperties, AzureTokenCredentialResolver resolver,
                                           @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultTokenCredential,
                                           AzureGlobalProperties azureGlobalProperties) {
        this.kafkaProperties = kafkaProperties;
        this.tokenCredentialResolver = resolver;
        this.defaultTokenCredential = defaultTokenCredential;
        this.azureGlobalProperties = azureGlobalProperties;
    }

    @Bean
    DefaultKafkaConsumerFactoryCustomizer defaultKafkaConsumerFactoryCustomizer() {
        Map<String, Object> updateConfigs = new HashMap<>();
        Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();
        if (needConfigureSaslOAuth(consumerProperties)) {
            AzureThirdPartyServiceProperties azureKafkaConsumerProperties = buildAzureProperties(consumerProperties,
                azureGlobalProperties);
            updateConfigs.put(AZURE_TOKEN_CREDENTIAL, buildCredentialMapFromProperties(azureKafkaConsumerProperties));
            updateConfigs.putAll(KAFKA_OAUTH_CONFIGS);
        }
        return factory -> factory.updateConfigs(updateConfigs);
    }

    @Bean
    DefaultKafkaProducerFactoryCustomizer defaultKafkaProducerFactoryCustomizer() {
        Map<String, Object> updateConfigs = new HashMap<>();
        Map<String, Object> producerProperties = kafkaProperties.buildProducerProperties();
        if (needConfigureSaslOAuth(producerProperties)) {
            AzureThirdPartyServiceProperties azureKafkaProducerProperties = buildAzureProperties(producerProperties,
                azureGlobalProperties);
            updateConfigs.put(AZURE_TOKEN_CREDENTIAL, buildCredentialMapFromProperties(azureKafkaProducerProperties));
            updateConfigs.putAll(KAFKA_OAUTH_CONFIGS);
        }
        return factory -> factory.updateConfigs(updateConfigs);
    }

    private TokenCredential buildCredentialMapFromProperties(AzureThirdPartyServiceProperties azureKafkaConsumerProperties) {
        TokenCredential tokenCredential = tokenCredentialResolver.resolve(azureKafkaConsumerProperties);
        if (tokenCredential == null) {
            tokenCredential = defaultTokenCredential;
        }
        return tokenCredential;
    }

    @ConditionalOnClass(KafkaMessageChannelBinder.class)
    @Configuration(proxyBeanMethods = false)
    @Import(KafkaBinderConfiguration.class)
    static class AzureKafkaSpringCloudStreamAutoConfiguration {
        @Bean
        KafkaBinderConfigurationPropertiesBeanPostProcessor kafkaBinderConfigurationPropertiesBeanPostProcessor(
            AzureGlobalProperties azureGlobalProperties) {
            return new KafkaBinderConfigurationPropertiesBeanPostProcessor(azureGlobalProperties);
        }
    }

}
