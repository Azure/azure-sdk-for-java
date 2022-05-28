// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.kafka;

import com.azure.spring.cloud.autoconfigure.context.AzureGlobalProperties;
import com.azure.spring.cloud.autoconfigure.kafka.properties.AzureEventHubsKafkaProperties;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Event Hubs Kafka support.
 * Provide Managed Identity-based OAuth2 authentication for Event Hubs for Kafka.
 *
 * @since 4.3.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(KafkaTemplate.class)
@AutoConfigureBefore(KafkaAutoConfiguration.class)
@ConditionalOnProperty(prefix = AzureEventHubsKafkaProperties.PREFIX, value = "enabled", havingValue = "true",
        matchIfMissing = true)
@EnableConfigurationProperties({AzureEventHubsKafkaProperties.class, KafkaProperties.class})
public class AzureEventHubsKafkaAutoConfiguration {

    public AzureEventHubsKafkaAutoConfiguration(AzureGlobalProperties azureGlobalProperties,
                                                AzureEventHubsKafkaProperties azureEventHubsKafkaProperties) {
        AzurePropertiesUtils.copyPropertiesIgnoreTargetNonNull(azureGlobalProperties.getProfile(),
                azureEventHubsKafkaProperties.getProfile());
        AzurePropertiesUtils.copyPropertiesIgnoreTargetNonNull(azureGlobalProperties.getCredential(),
                azureEventHubsKafkaProperties.getCredential());
    }

    /**
     * The BeanPostProcessor to instrument the {@link KafkaProperties} bean with provided OAuth2 configuration.
     *
     * @param azureEventHubsKafkaProperties the properties for profile and credential of Azure Event Hubs for Kafka.
     * @return the bean post processor.
     */
    @Bean
    public KafkaPropertiesBeanPostProcessor kafkaPropertiesBeanPostProcessor(
            AzureEventHubsKafkaProperties azureEventHubsKafkaProperties) {
        return new KafkaPropertiesBeanPostProcessor(azureEventHubsKafkaProperties);
    }

}
