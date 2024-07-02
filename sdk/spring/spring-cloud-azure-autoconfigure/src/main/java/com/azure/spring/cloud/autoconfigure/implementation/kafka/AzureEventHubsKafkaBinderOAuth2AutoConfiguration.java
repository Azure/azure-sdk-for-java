// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.autoconfigure.implementation.kafka;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.stream.binder.kafka.config.KafkaBinderConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for Azure Event Hubs Kafka Azure Identity support on Spring Cloud Stream framework.
 *
 * To trigger the {@link BindingServicePropertiesBeanPostProcessor} when kafka binder is being used, it enables {@link AzureEventHubsKafkaOAuth2AutoConfiguration}
 * for Spring Cloud Stream Kafka Binder context which is to support Azure Identity-based OAuth2 authentication.
 *
 * @since 4.4.0
 */
@ConditionalOnClass(KafkaBinderConfiguration.class)
@ConditionalOnProperty(value = "spring.cloud.azure.eventhubs.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class AzureEventHubsKafkaBinderOAuth2AutoConfiguration {

    @Bean
    static BeanPostProcessor bindingServicePropertiesBeanPostProcessor() {
        return new BindingServicePropertiesBeanPostProcessor();
    }
}
