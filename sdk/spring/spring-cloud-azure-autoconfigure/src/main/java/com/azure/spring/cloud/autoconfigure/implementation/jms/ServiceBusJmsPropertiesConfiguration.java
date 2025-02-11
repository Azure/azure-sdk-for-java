// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.jms;

import com.azure.spring.cloud.autoconfigure.implementation.condition.ConditionalOnMissingProperty;
import com.azure.spring.cloud.autoconfigure.implementation.jms.properties.AzureServiceBusJmsProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * {@code @Configuration} class that registers a {@link AzureServiceBusJmsPropertiesBeanPostProcessor}
 * bean capable of processing Service Bus JMS properties @{@link AzureServiceBusJmsProperties}.
 *
 * @since 5.19.0
 */
@Configuration(proxyBeanMethods = false)
class ServiceBusJmsPropertiesConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnMissingProperty(prefix = "spring.jms.servicebus", name = "connection-string")
    static AzureServiceBusJmsPropertiesBeanPostProcessor azureServiceBusJmsPropertiesBeanPostProcessor() {
        return new AzureServiceBusJmsPropertiesBeanPostProcessor();
    }
}
