// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.messaging;

import com.azure.spring.cloud.autoconfigure.eventhubs.AzureEventHubsMessagingAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.servicebus.AzureServiceBusMessagingAutoConfiguration;
import com.azure.spring.eventhubs.core.config.EventHubsMessageListenerContainerFactory;
import com.azure.spring.eventhubs.core.listener.EventHubsMessageListenerContainer;
import com.azure.spring.eventhubs.core.processor.EventHubsProcessorFactory;
import com.azure.spring.messaging.annotation.EnableAzureMessaging;
import com.azure.spring.messaging.listener.MessageListenerContainer;
import com.azure.spring.messaging.listener.MessageListenerContainerFactory;
import com.azure.spring.servicebus.core.config.ServiceBusMessageListenerContainerFactory;
import com.azure.spring.servicebus.core.listener.ServiceBusMessageListenerContainer;
import com.azure.spring.servicebus.core.processor.ServiceBusProcessorFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * {@link EnableAutoConfiguration Auto-configuration} for AzureMessagingListener.
 *
 * @since 4.0
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(EnableAzureMessaging.class)
@Conditional(AzureMessagingListenerAutoConfiguration.MessagingListenerCondition.class)
@AutoConfigureAfter({
    AzureEventHubsMessagingAutoConfiguration.class,
    AzureServiceBusMessagingAutoConfiguration.class
})
public class AzureMessagingListenerAutoConfiguration {

    @Configuration
    @ConditionalOnBean(EventHubsProcessorFactory.class)
    static class EventHubsConfiguration {
        @Bean(name = "azureEventHubsListenerContainerFactory")
        @ConditionalOnMissingBean(name = "azureEventHubsListenerContainerFactory")
        public MessageListenerContainerFactory<? extends MessageListenerContainer> azureEventHubsListenerContainerFactory(
            EventHubsProcessorFactory eventHubsProcessorFactory) {
            return new EventHubsMessageListenerContainerFactory(eventHubsProcessorFactory);
        }

    }

    @Configuration
    @ConditionalOnBean(ServiceBusProcessorFactory.class)
    static class ServiceBusConfiguration {
        @Bean(name = "azureServiceBusListenerContainerFactory")
        @ConditionalOnMissingBean(name = "azureServiceBusListenerContainerFactory")
        public MessageListenerContainerFactory<? extends MessageListenerContainer> azureServiceBusListenerContainerFactory(
            ServiceBusProcessorFactory serviceBusProcessorFactory) {
            return new ServiceBusMessageListenerContainerFactory(serviceBusProcessorFactory);
        }
    }

    static class MessagingListenerCondition extends AnyNestedCondition {

        MessagingListenerCondition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnClass(EventHubsMessageListenerContainer.class)
        static class ConditonalOnEventHubs {

        }

        @ConditionalOnClass(ServiceBusMessageListenerContainer.class)
        static class ConditonalOnServiceBus {

        }

    }
}
