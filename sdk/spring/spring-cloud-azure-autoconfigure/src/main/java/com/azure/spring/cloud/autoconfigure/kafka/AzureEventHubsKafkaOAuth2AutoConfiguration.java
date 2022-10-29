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
        this.consumerCustomizedProperties.putAll(kafkaProperties.buildConsumerProperties());
        this.producerCustomizedProperties.putAll(kafkaProperties.buildProducerProperties());
        if (needConfigureSaslOAuth(kafkaProperties.buildAdminProperties())) {
            configureKafkaOAuth2Properties(kafkaProperties.buildAdminProperties(), azureGlobalProperties,
                    kafkaProperties.getAdmin().getProperties());
        }
        clearAzurePropertiesInKafkaProperties();
    }

    @Bean
    DefaultKafkaConsumerFactoryCustomizer azureOAuth2KafkaConsumerFactoryCustomizer() {
        Map<String, Object> updateConfigs = new HashMap<>();
        if (needConfigureSaslOAuth(consumerCustomizedProperties)) {
            configureKafkaOAuth2Properties(consumerCustomizedProperties, azureGlobalProperties, kafkaProperties.getConsumer()
                    .getProperties());
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
            configureKafkaOAuth2Properties(producerCustomizedProperties, azureGlobalProperties, kafkaProperties.getProducer()
                    .getProperties());
            updateConfigs.putAll(kafkaProperties.getProducer().getProperties());
            updateConfigs.put(AZURE_TOKEN_CREDENTIAL, resolveSpringCloudAzureTokenCredential(
                    buildAzureProperties(producerCustomizedProperties, azureGlobalProperties)));
            configureKafkaUserAgent();
        }
        return factory -> factory.updateConfigs(updateConfigs);
    }


//    private void configureFactoryOAuth2Properties(AzurePasswordlessProperties azurePasswordlessProperties, Map<String, Object> updateConfigs) {
//        updateConfigs.putAll(KAFKA_OAUTH_CONFIGS);
//        if (!sourceKafkaProperties.containsKey(SASL_JAAS_CONFIG)
//                || (sourceKafkaProperties.get(SASL_JAAS_CONFIG) != null
//                && !((String) sourceKafkaProperties.get(SASL_JAAS_CONFIG)).startsWith(AzureKafkaPropertiesUtils.SASL_JAAS_CONFIG_OAUTH_PREFIX))) {
//            updateConfigs.put(SASL_JAAS_CONFIG, SASL_JAAS_CONFIG_OAUTH);
//        }
//
//        updateConfigs.put(AZURE_TOKEN_CREDENTIAL, resolveSpringCloudAzureTokenCredential(azurePasswordlessProperties));
//        logConfigureOAuthProperties();
//    }

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
