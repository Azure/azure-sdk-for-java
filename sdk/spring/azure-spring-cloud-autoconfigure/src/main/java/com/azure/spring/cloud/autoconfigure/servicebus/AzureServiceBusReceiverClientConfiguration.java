// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSessionReceiverClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Configuration for a {@link ServiceBusReceiverClient} or a {@link ServiceBusReceiverAsyncClient}.
 */
@Configuration(proxyBeanMethods = false)
class AzureServiceBusReceiverClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusReceiverClientConfiguration.class);
    private final PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = AzureServiceBusProperties.PREFIX, name = "receiver.session-aware", havingValue = "false", matchIfMissing = true
    )
    public ServiceBusReceiverAsyncClient serviceBusReceiverAsyncClient(
        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverClientBuilder) {
        return receiverClientBuilder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = AzureServiceBusProperties.PREFIX, name = "receiver.session-aware", havingValue = "false", matchIfMissing = true
    )
    public ServiceBusReceiverClient serviceBusReceiverClient(
        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverClientBuilder) {
        return receiverClientBuilder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = AzureServiceBusProperties.PREFIX, name = "receiver.session-aware", havingValue = "false", matchIfMissing = true
    )
    public ServiceBusClientBuilder.ServiceBusReceiverClientBuilder serviceBusReceiverClientBuilder(
        ServiceBusClientBuilder serviceBusClientBuilder,
        AzureServiceBusProperties serviceBusProperties) {

        final AzureServiceBusProperties.ServiceBusReceiver receiverProperties = serviceBusProperties.getReceiver();
        final ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverClientBuilder = serviceBusClientBuilder.receiver();

        propertyMapper.from(receiverProperties.getQueueName()).to(receiverClientBuilder::queueName);
        propertyMapper.from(receiverProperties.getTopicName()).to(receiverClientBuilder::topicName);
        propertyMapper.from(receiverProperties.getSubscriptionName()).to(receiverClientBuilder::subscriptionName);
        propertyMapper.from(receiverProperties.getReceiveMode()).to(receiverClientBuilder::receiveMode);
        propertyMapper.from(receiverProperties.getSubQueue()).to(receiverClientBuilder::subQueue);
        propertyMapper.from(receiverProperties.getPrefetchCount()).to(receiverClientBuilder::prefetchCount);
        propertyMapper.from(receiverProperties.getMaxAutoLockRenewDuration())
                      .to(receiverClientBuilder::maxAutoLockRenewDuration);
        propertyMapper.from(receiverProperties.isAutoComplete()).whenFalse().to(t -> receiverClientBuilder.disableAutoComplete());

        if (StringUtils.hasText(receiverProperties.getQueueName())
                && StringUtils.hasText(receiverProperties.getTopicName())
                && StringUtils.hasText(receiverProperties.getSubscriptionName())) {
            LOGGER.warn("Both queue and topic name configured for a service bus receiver, but only the queue name will take effective");
        }
        return receiverClientBuilder;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureServiceBusProperties.PREFIX, name = "receiver.session-aware")
    public ServiceBusSessionReceiverAsyncClient serviceBusSessionReceiverAsyncClient(
        ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverClientBuilder) {
        return receiverClientBuilder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureServiceBusProperties.PREFIX, name = "receiver.session-aware")
    public ServiceBusSessionReceiverClient serviceBusSessionReceiverClient(
        ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverClientBuilder) {
        return receiverClientBuilder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureServiceBusProperties.PREFIX, name = "receiver.session-aware")
    public ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder serviceBusSessionReceiverClientBuilder(
        ServiceBusClientBuilder serviceBusClientBuilder,
        AzureServiceBusProperties serviceBusProperties) {

        final AzureServiceBusProperties.ServiceBusReceiver receiverProperties = serviceBusProperties.getReceiver();
        final ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder sessionReceiverClientBuilder = serviceBusClientBuilder.sessionReceiver();

        propertyMapper.from(receiverProperties.getQueueName()).to(sessionReceiverClientBuilder::queueName);
        propertyMapper.from(receiverProperties.getTopicName()).to(sessionReceiverClientBuilder::topicName);
        propertyMapper.from(receiverProperties.getSubscriptionName()).to(sessionReceiverClientBuilder::subscriptionName);
        propertyMapper.from(receiverProperties.getReceiveMode()).to(sessionReceiverClientBuilder::receiveMode);
        propertyMapper.from(receiverProperties.getSubQueue()).to(sessionReceiverClientBuilder::subQueue);
        propertyMapper.from(receiverProperties.getPrefetchCount()).to(sessionReceiverClientBuilder::prefetchCount);
        propertyMapper.from(receiverProperties.getMaxAutoLockRenewDuration())
                      .to(sessionReceiverClientBuilder::maxAutoLockRenewDuration);
        propertyMapper.from(receiverProperties.isAutoComplete()).whenFalse().to(t -> sessionReceiverClientBuilder.disableAutoComplete());

        if (StringUtils.hasText(receiverProperties.getQueueName())
                && StringUtils.hasText(receiverProperties.getTopicName())
                && StringUtils.hasText(receiverProperties.getSubscriptionName())) {
            LOGGER.warn("Both queue and topic name configured for a service bus receiver, but only the queue name will take effective");
        }
        return sessionReceiverClientBuilder;
    }

}
