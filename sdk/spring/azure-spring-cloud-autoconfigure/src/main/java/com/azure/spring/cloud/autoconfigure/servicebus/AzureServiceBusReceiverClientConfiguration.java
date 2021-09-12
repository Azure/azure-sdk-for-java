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
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.PropertyMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configuration for a {@link ServiceBusReceiverClient} or a {@link ServiceBusReceiverAsyncClient}.
 */
@Configuration(proxyBeanMethods = false)
@AzureServiceBusReceiverClientConfiguration.ConditionalOnServiceBusReceiver
class AzureServiceBusReceiverClientConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(AzureServiceBusReceiverClientConfiguration.class);

    private final PropertyMapper propertyMapper = PropertyMapper.get().alwaysApplyingWhenNonNull();
    private final AzureServiceBusProperties serviceBusProperties;

    AzureServiceBusReceiverClientConfiguration(AzureServiceBusProperties serviceBusProperties) {
        this.serviceBusProperties = serviceBusProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = AzureServiceBusProperties.PREFIX, name = "receiver.session-aware", havingValue = "false", matchIfMissing = true
    )
    @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class)
    public ServiceBusReceiverAsyncClient serviceBusReceiverAsyncClient(
        ServiceBusClientBuilder.ServiceBusReceiverClientBuilder receiverClientBuilder) {
        return receiverClientBuilder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(
        prefix = AzureServiceBusProperties.PREFIX, name = "receiver.session-aware", havingValue = "false", matchIfMissing = true
    )
    @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusReceiverClientBuilder.class)
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
        ServiceBusClientBuilder serviceBusClientBuilder) {

        final AzureServiceBusProperties.ServiceBusReceiver receiverProperties = this.serviceBusProperties.getReceiver();
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
    @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class)
    public ServiceBusSessionReceiverAsyncClient serviceBusSessionReceiverAsyncClient(
        ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverClientBuilder) {
        return receiverClientBuilder.buildAsyncClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean(ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder.class)
    public ServiceBusSessionReceiverClient serviceBusSessionReceiverClient(
        ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder receiverClientBuilder) {
        return receiverClientBuilder.buildClient();
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = AzureServiceBusProperties.PREFIX, name = "receiver.session-aware")
    public ServiceBusClientBuilder.ServiceBusSessionReceiverClientBuilder serviceBusSessionReceiverClientBuilder(
        ServiceBusClientBuilder serviceBusClientBuilder) {

        final AzureServiceBusProperties.ServiceBusReceiver receiverProperties = this.serviceBusProperties.getReceiver();
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

    @Target({ ElementType.TYPE, ElementType.METHOD })
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.receiver.queue-name:}') or "
                                 + "!T(org.springframework.util.StringUtils).isEmpty('${spring.cloud.azure.servicebus.receiver.topic-name:}')")
    public @interface ConditionalOnServiceBusReceiver {
    }

}
