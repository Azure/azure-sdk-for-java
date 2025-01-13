// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import com.azure.spring.cloud.autoconfigure.implementation.context.AzureGlobalPropertiesAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.stream.binder.kafka.KafkaMessageChannelBinder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Role;

/**
 * Configuration for OAuth2 support on Spring Cloud Stream Kafka Binder. Provide Azure Identity-based
 * OAuth2 authentication for Event Hubs for Kafka on the basis of Spring Cloud Stream configuration.
 *
 * @since 4.3.0
 */
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
@ConditionalOnClass(KafkaMessageChannelBinder.class)
@Import(AzureGlobalPropertiesAutoConfiguration.class)
public class AzureKafkaSpringCloudStreamConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    static KafkaBinderConfigurationPropertiesBeanPostProcessor kafkaBinderConfigurationPropertiesBeanPostProcessor(
            AzureGlobalProperties azureGlobalProperties) {
        return new KafkaBinderConfigurationPropertiesBeanPostProcessor(azureGlobalProperties);
    }
}
