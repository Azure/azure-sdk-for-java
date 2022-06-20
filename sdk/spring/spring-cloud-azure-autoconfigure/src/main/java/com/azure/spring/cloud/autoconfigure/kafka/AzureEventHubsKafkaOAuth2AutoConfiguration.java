// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.service.implementation.kafka.AzureKafkaProperties;
import org.apache.kafka.common.message.ApiVersionsRequestData;
import org.apache.kafka.common.requests.ApiVersionsRequest;

import org.springframework.beans.factory.annotation.Qualifier;
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
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.KAFKA_OAUTH_CONFIGS;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.buildAzureProperties;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.logConfigureOAuthProperties;
import static com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureKafkaAutoconfigurationUtils.needConfigureSaslOAuth;
import static com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier.AZURE_SPRING_EVENT_HUBS_KAFKA_OAUTH;
import static com.azure.spring.cloud.core.implementation.util.AzureSpringIdentifier.VERSION;
import static com.azure.spring.cloud.service.implementation.kafka.AzureKafkaPropertiesUtils.AZURE_TOKEN_CREDENTIAL;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Event Hubs Kafka support. Provide Managed Identity-based
 * OAuth2 authentication for Event Hubs for Kafka on the basis of Spring Boot Autoconfiguration.
 *
 * @since 4.3.0
 */
@Configuration(proxyBeanMethods = false)
@Import(AzureEventHubsKafkaOAuth2AutoConfiguration.AzureKafkaSpringCloudStreamConfiguration.class)
@ConditionalOnClass(KafkaTemplate.class)
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
        configureKafkaUserAgent();
    }

    @Bean
    DefaultKafkaConsumerFactoryCustomizer azureOAuth2KafkaConsumerFactoryCustomizer() {
        Map<String, Object> updateConfigs = new HashMap<>();
        Map<String, Object> consumerProperties = kafkaProperties.buildConsumerProperties();
        configureOAuth2Properties(updateConfigs, consumerProperties);
        return factory -> factory.updateConfigs(updateConfigs);
    }

    @Bean
    DefaultKafkaProducerFactoryCustomizer azureOAuth2KafkaProducerFactoryCustomizer() {
        Map<String, Object> updateConfigs = new HashMap<>();
        Map<String, Object> producerProperties = kafkaProperties.buildProducerProperties();
        configureOAuth2Properties(updateConfigs, producerProperties);
        return factory -> factory.updateConfigs(updateConfigs);
    }

    private void configureOAuth2Properties(Map<String, Object> updateConfigs, Map<String, Object> sourceKafkaProperties) {
        if (needConfigureSaslOAuth(sourceKafkaProperties)) {
            AzureKafkaProperties azureKafkaProperties = buildAzureProperties(sourceKafkaProperties,
                    azureGlobalProperties);
            updateConfigs.put(AZURE_TOKEN_CREDENTIAL, resolveSpringCloudAzureTokenCredential(azureKafkaProperties));
            updateConfigs.putAll(KAFKA_OAUTH_CONFIGS);
            logConfigureOAuthProperties();
        }
    }

    private TokenCredential resolveSpringCloudAzureTokenCredential(AzureKafkaProperties azureKafkaConsumerProperties) {
        TokenCredential tokenCredential = tokenCredentialResolver.resolve(azureKafkaConsumerProperties);
        return tokenCredential == null ? defaultTokenCredential : tokenCredential;
    }

    private void configureKafkaUserAgent() {
        Method dataMethod = ReflectionUtils.findMethod(ApiVersionsRequest.class, "data");
        if (dataMethod != null) {
            ApiVersionsRequest apiVersionsRequest = new ApiVersionsRequest.Builder().build();
            ApiVersionsRequestData apiVersionsRequestData = (ApiVersionsRequestData) ReflectionUtils.invokeMethod(dataMethod, apiVersionsRequest);
            if (apiVersionsRequestData != null) {
                apiVersionsRequestData.setClientSoftwareName(apiVersionsRequestData.clientSoftwareName()
                        + AZURE_SPRING_EVENT_HUBS_KAFKA_OAUTH);
                apiVersionsRequestData.setClientSoftwareVersion(VERSION);
            }
        }
    }

    @ConditionalOnClass(KafkaMessageChannelBinder.class)
    @Configuration(proxyBeanMethods = false)
    @Import(KafkaBinderConfiguration.class)
    static class AzureKafkaSpringCloudStreamConfiguration {
        @Bean
        KafkaBinderConfigurationPropertiesBeanPostProcessor kafkaBinderConfigurationPropertiesBeanPostProcessor(
            AzureGlobalProperties azureGlobalProperties) {
            return new KafkaBinderConfigurationPropertiesBeanPostProcessor(azureGlobalProperties);
        }
    }

}
