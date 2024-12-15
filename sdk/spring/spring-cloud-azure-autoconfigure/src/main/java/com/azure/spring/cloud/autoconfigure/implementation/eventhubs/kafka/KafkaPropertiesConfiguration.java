// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs.kafka;

import com.azure.spring.cloud.autoconfigure.implementation.kafka.AzureEventHubsKafkaOAuth2AutoConfiguration;
import com.azure.spring.cloud.core.provider.connectionstring.ServiceConnectionStringProvider;
import com.azure.spring.cloud.core.service.AzureServiceType;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * {@code @Configuration} class that registers a {@link KafkaPropertiesBeanPostProcessor}
 * bean capable of processing Kafka properties @{@link KafkaProperties}.
 *
 * @since 5.19.0
 * @deprecated 4.3.0 in favor of {@link AzureEventHubsKafkaOAuth2AutoConfiguration}.
 */
@Deprecated
@Configuration(proxyBeanMethods = false)
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
class KafkaPropertiesConfiguration {

    @Bean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @ConditionalOnBean(value = AzureServiceType.EventHubs.class, parameterizedContainer = ServiceConnectionStringProvider.class)
    static KafkaPropertiesBeanPostProcessor kafkaPropertiesBeanPostProcessor(
        ServiceConnectionStringProvider<AzureServiceType.EventHubs> connectionStringProvider) {
        return new KafkaPropertiesBeanPostProcessor(connectionStringProvider);
    }
}
