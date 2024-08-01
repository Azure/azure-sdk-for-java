// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import com.azure.spring.cloud.autoconfigure.implementation.context.properties.AzureGlobalProperties;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import static com.azure.spring.cloud.autoconfigure.implementation.context.AzureContextUtils.PASSWORDLESS_KAFKA_PROPERTIES_BEAN_POST_PROCESSOR_BEAN_NAME;

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

    @Bean(PASSWORDLESS_KAFKA_PROPERTIES_BEAN_POST_PROCESSOR_BEAN_NAME)
    static BeanPostProcessor kafkaPropertiesBeanPostProcessor(AzureGlobalProperties properties) {
        return new KafkaPropertiesBeanPostProcessor(properties);
    }
}
