// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.core.credential.TokenCredential;
import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.context.AzureTokenCredentialAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.kafka.properties.AzureEventHubsKafkaProperties;
import com.azure.spring.cloud.core.implementation.credential.provider.TokenCredentialProvider;
import com.azure.spring.cloud.core.implementation.credential.resolver.AzureTokenCredentialResolver;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.service.kafka.KafkaOAuth2AuthenticateCallbackHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import static com.azure.spring.cloud.autoconfigure.context.AzureContextUtils.DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Event Hubs Kafka support.
 * Provide Managed Identity-based OAuth2 authentication for Event Hubs for Kafka.
 *
 * @since 4.3.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(KafkaTemplate.class)
@AutoConfigureBefore(KafkaAutoConfiguration.class)
@AutoConfigureAfter(AzureTokenCredentialAutoConfiguration.class)
@ConditionalOnProperty(prefix = AzureEventHubsKafkaProperties.PREFIX, value = "enabled", havingValue = "true",
        matchIfMissing = true)
public class AzureEventHubsKafkaOAuth2AutoConfiguration {

    @Bean
    @ConfigurationProperties(AzureEventHubsKafkaProperties.PREFIX)
    AzureEventHubsKafkaProperties azureEventHubsKafkaProperties(AzureGlobalProperties azureGlobalProperties) {
        AzureEventHubsKafkaProperties azureEventHubsKafkaProperties = new AzureEventHubsKafkaProperties();
        AzurePropertiesUtils.copyPropertiesIgnoreNull(azureGlobalProperties.getProfile(),
                azureEventHubsKafkaProperties.getProfile());
        AzurePropertiesUtils.copyPropertiesIgnoreNull(azureGlobalProperties.getCredential(),
                azureEventHubsKafkaProperties.getCredential());
        return azureEventHubsKafkaProperties;
    }

    public static void main(String[] args) {
        AzureGlobalProperties azureGlobalProperties = new AzureGlobalProperties();
        AzureEventHubsKafkaProperties azureEventHubsKafkaProperties = new AzureEventHubsKafkaProperties();
        System.out.println(azureGlobalProperties.getProfile().equals(azureEventHubsKafkaProperties.getProfile()));
    }

    @Bean
    TokenCredentialProvider kafkaOAuth2TokenCredentialProvider(
            @Qualifier(DEFAULT_TOKEN_CREDENTIAL_BEAN_NAME) TokenCredential defaultTokenCredential,
            AzureTokenCredentialResolver azureTokenCredentialResolver,
            AzureGlobalProperties azureGlobalProperties,
            AzureEventHubsKafkaProperties azureEventHubsKafkaProperties) {
        TokenCredentialProvider tokenCredentialProvider = new TokenCredentialProvider(defaultTokenCredential, azureTokenCredentialResolver,
                azureGlobalProperties, azureEventHubsKafkaProperties);
        KafkaOAuth2AuthenticateCallbackHandler.tokenCredentialProvider = tokenCredentialProvider;
        return tokenCredentialProvider;
    }

    @Bean
    static KafkaPropertiesBeanPostProcessor kafkaPropertiesBeanPostProcessor() {
        return new KafkaPropertiesBeanPostProcessor();
    }

}
