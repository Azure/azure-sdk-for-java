// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.integration.servicebus.ServiceBusMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Configuration for a {@link ServiceBusProcessorClient}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnBean(ServiceBusMessageProcessor.class)
class AzureServiceBusProcessorConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusProcessorConfiguration.class);

    private final PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
    private final AzureServiceBusProperties serviceBusProperties;

    AzureServiceBusProcessorConfiguration(AzureServiceBusProperties serviceBusProperties) {
        this.serviceBusProperties = serviceBusProperties;
    }

    // TODO (xiada): how to apply the processError and processMessage functions
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = AzureServiceBusProperties.PREFIX, name = "processor.session-aware", havingValue = "false", matchIfMissing = true
    )
    @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusProcessorClientBuilder.class)
    public ServiceBusProcessorClient serviceBusProcessorClient(
        ServiceBusClientBuilder.ServiceBusProcessorClientBuilder processorClientBuilder) {
        return processorClientBuilder.buildProcessorClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = AzureServiceBusProperties.PREFIX, name = "processor.session-aware", havingValue = "false", matchIfMissing = true
    )
    public ServiceBusClientBuilder.ServiceBusProcessorClientBuilder serviceBusProcessorClientBuilder(
        ServiceBusClientBuilder serviceBusClientBuilder) {
        final AzureServiceBusProperties.ServiceBusProcessor processorProperties = serviceBusProperties.getProcessor();
        final ServiceBusClientBuilder.ServiceBusProcessorClientBuilder processorClientBuilder = serviceBusClientBuilder.processor();

        propertyMapper.from(processorProperties.getQueueName()).to(processorClientBuilder::queueName);
        propertyMapper.from(processorProperties.getTopicName()).to(processorClientBuilder::topicName);
        propertyMapper.from(processorProperties.getSubscriptionName()).to(processorClientBuilder::subscriptionName);
        propertyMapper.from(processorProperties.getReceiveMode()).to(processorClientBuilder::receiveMode);
        propertyMapper.from(processorProperties.getSubQueue()).to(processorClientBuilder::subQueue);
        propertyMapper.from(processorProperties.getPrefetchCount()).to(processorClientBuilder::prefetchCount);
        propertyMapper.from(processorProperties.getMaxAutoLockRenewDuration()).to(processorClientBuilder::maxAutoLockRenewDuration);
        propertyMapper.from(processorProperties.isAutoComplete()).whenFalse().to(t -> processorClientBuilder.disableAutoComplete());
        propertyMapper.from(processorProperties.getMaxConcurrentCalls()).to(processorClientBuilder::maxConcurrentCalls);

        if (StringUtils.hasText(processorProperties.getQueueName())
                && StringUtils.hasText(processorProperties.getTopicName())
                && StringUtils.hasText(processorProperties.getSubscriptionName())) {
            LOGGER.warn("Both queue and topic name configured for a service bus processor, but only the queue name will take effective");
        }

        return processorClientBuilder;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureServiceBusProperties.PREFIX, name = "processor.session-aware")
    @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder.class)
    public ServiceBusProcessorClient serviceBusSessionProcessorClient(
        ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder sessionProcessorClientBuilder) {
        return sessionProcessorClientBuilder.buildProcessorClient();
    }

    // TODO (xiada): how to apply the processError and processMessage functions
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureServiceBusProperties.PREFIX, name = "processor.session-aware")
    public ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder serviceBusSessionProcessorClientBuilder(
        ServiceBusClientBuilder serviceBusClientBuilder) {

        final AzureServiceBusProperties.ServiceBusProcessor processorProperties = serviceBusProperties.getProcessor();
        final ServiceBusClientBuilder.ServiceBusSessionProcessorClientBuilder sessionProcessorClientBuilder = serviceBusClientBuilder.sessionProcessor();

        propertyMapper.from(processorProperties.getQueueName()).to(sessionProcessorClientBuilder::queueName);
        propertyMapper.from(processorProperties.getTopicName()).to(sessionProcessorClientBuilder::topicName);
        propertyMapper.from(processorProperties.getSubscriptionName()).to(sessionProcessorClientBuilder::subscriptionName);
        propertyMapper.from(processorProperties.getReceiveMode()).to(sessionProcessorClientBuilder::receiveMode);
        propertyMapper.from(processorProperties.getSubQueue()).to(sessionProcessorClientBuilder::subQueue);
        propertyMapper.from(processorProperties.getPrefetchCount()).to(sessionProcessorClientBuilder::prefetchCount);
        propertyMapper.from(processorProperties.getMaxAutoLockRenewDuration()).to(sessionProcessorClientBuilder::maxAutoLockRenewDuration);
        propertyMapper.from(processorProperties.isAutoComplete()).whenFalse().to(t -> sessionProcessorClientBuilder.disableAutoComplete());
        propertyMapper.from(processorProperties.getMaxConcurrentCalls()).to(sessionProcessorClientBuilder::maxConcurrentCalls);
        propertyMapper.from(processorProperties.getMaxConcurrentSessions()).to(sessionProcessorClientBuilder::maxConcurrentSessions);

        if (StringUtils.hasText(processorProperties.getQueueName())
                && StringUtils.hasText(processorProperties.getTopicName())
                && StringUtils.hasText(processorProperties.getSubscriptionName())) {
            LOGGER.warn("Both queue and topic name configured for a service bus processor, but only the queue name will take effective");
        }

        return sessionProcessorClientBuilder;
    }

}
