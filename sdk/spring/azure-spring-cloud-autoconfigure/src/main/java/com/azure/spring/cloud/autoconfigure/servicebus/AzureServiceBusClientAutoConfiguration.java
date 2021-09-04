// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * Auto-configuration for a {@link ServiceBusClientBuilder}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ServiceBusClientBuilder.class)
@ConditionalOnProperty(prefix = "spring.cloud.azure.servicebus", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(AzureServiceBusProperties.class)
public class AzureServiceBusClientAutoConfiguration {

    // TODO (xiada) should we auto-configure the receiver, sender, processor, sessionReceiver, sessionProcessor clients?

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusClientBuilderFactory factory(AzureServiceBusProperties properties) {
        return new ServiceBusClientBuilderFactory(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusClientBuilder serviceBusClientBuilder(ServiceBusClientBuilderFactory factory) {
        return factory.build();
    }
}
