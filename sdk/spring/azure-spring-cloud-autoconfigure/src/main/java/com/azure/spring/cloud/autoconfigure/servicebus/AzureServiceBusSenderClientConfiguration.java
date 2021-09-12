// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Configuration for a {@link ServiceBusSenderClient} or a {@link ServiceBusSenderAsyncClient}.
 */
@Configuration(proxyBeanMethods = false)
class AzureServiceBusSenderClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusSenderClientConfiguration.class);

    private final PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
    private final AzureServiceBusProperties serviceBusProperties;

    AzureServiceBusSenderClientConfiguration(AzureServiceBusProperties serviceBusProperties) {
        this.serviceBusProperties = serviceBusProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusSenderClientBuilder.class)
    public ServiceBusSenderAsyncClient serviceBusSenderAsyncClient(
        ServiceBusClientBuilder.ServiceBusSenderClientBuilder receiverClientBuilder) {
        return receiverClientBuilder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusSenderClientBuilder.class)
    public ServiceBusSenderClient serviceBusSenderClient(
        ServiceBusClientBuilder.ServiceBusSenderClientBuilder receiverClientBuilder) {
        return receiverClientBuilder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    public ServiceBusClientBuilder.ServiceBusSenderClientBuilder serviceBusSenderClientBuilder(
        ServiceBusClientBuilder serviceBusClientBuilder) {
        final AzureServiceBusProperties.ServiceBusSender senderProperties = this.serviceBusProperties.getSender();
        final ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderClientBuilder = serviceBusClientBuilder.sender();
        propertyMapper.from(senderProperties.getQueueName()).to(senderClientBuilder::queueName);
        propertyMapper.from(senderProperties.getTopicName()).to(senderClientBuilder::topicName);

        if (StringUtils.hasText(senderProperties.getQueueName())
                && StringUtils.hasText(senderProperties.getTopicName())) {
            LOGGER.warn("Both queue and topic name configured for a service bus sender, but only the queue name will take effective");
        }

        return senderClientBuilder;
    }

}
