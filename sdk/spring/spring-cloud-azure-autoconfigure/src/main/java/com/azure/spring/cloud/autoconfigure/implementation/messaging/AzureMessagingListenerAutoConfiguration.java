// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.messaging;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.AzureEventHubsMessagingAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.AzureServiceBusMessagingAutoConfiguration;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.eventhubs.core.EventHubsProcessorFactory;
import com.azure.spring.messaging.eventhubs.core.listener.EventHubsMessageListenerContainer;
import com.azure.spring.messaging.eventhubs.implementation.core.annotation.EventHubsListenerAnnotationBeanPostProcessor;
import com.azure.spring.messaging.eventhubs.implementation.core.config.EventHubsMessageListenerContainerFactory;
import com.azure.spring.messaging.implementation.annotation.AzureListenerAnnotationBeanPostProcessorAdapter;
import com.azure.spring.messaging.implementation.annotation.EnableAzureMessaging;
import com.azure.spring.messaging.implementation.listener.MessageListenerContainerFactory;
import com.azure.spring.messaging.listener.MessageListenerContainer;
import com.azure.spring.messaging.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.messaging.servicebus.core.listener.ServiceBusMessageListenerContainer;
import com.azure.spring.messaging.servicebus.implementation.core.annotation.ServiceBusListenerAnnotationBeanPostProcessor;
import com.azure.spring.messaging.servicebus.implementation.core.config.ServiceBusMessageListenerContainerFactory;
import org.springframework.beans.factory.ObjectProvider;
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
 * {@link EnableAutoConfiguration Auto-configuration} for annotation-driven messaging.
 *
 * @since 4.0.0
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
            EventHubsProcessorFactory eventHubsProcessorFactory,
            ObjectProvider<AzureMessageConverter<EventData, EventData>> messageConverterProvider) {
            EventHubsMessageListenerContainerFactory containerFactory = new EventHubsMessageListenerContainerFactory(eventHubsProcessorFactory);
            messageConverterProvider.ifAvailable(containerFactory::setMessageConverter);
            return containerFactory;
        }

        @Configuration(proxyBeanMethods = false)
        @EnableAzureMessaging
        @ConditionalOnMissingBean(name = {
            EventHubsListenerAnnotationBeanPostProcessor.DEFAULT_EVENT_HUBS_LISTENER_ANNOTATION_BPP_BEAN_NAME,
            AzureListenerAnnotationBeanPostProcessorAdapter.DEFAULT_AZURE_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME })
        static class EnableEventHubsConfiguration {

        }

    }

    @Configuration
    @ConditionalOnBean(ServiceBusProcessorFactory.class)
    static class ServiceBusConfiguration {
        @Bean(name = "azureServiceBusListenerContainerFactory")
        @ConditionalOnMissingBean(name = "azureServiceBusListenerContainerFactory")
        public MessageListenerContainerFactory<? extends MessageListenerContainer> azureServiceBusListenerContainerFactory(
            ServiceBusProcessorFactory serviceBusProcessorFactory,
            ObjectProvider<AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage>> messageConverterProvider) {
            ServiceBusMessageListenerContainerFactory containerFactory = new ServiceBusMessageListenerContainerFactory(serviceBusProcessorFactory);
            messageConverterProvider.ifAvailable(containerFactory::setMessageConverter);
            return containerFactory;
        }

        @Configuration(proxyBeanMethods = false)
        @EnableAzureMessaging
        @ConditionalOnMissingBean(name = {
            ServiceBusListenerAnnotationBeanPostProcessor.DEFAULT_SERVICE_BUS_LISTENER_ANNOTATION_BPP_BEAN_NAME,
            AzureListenerAnnotationBeanPostProcessorAdapter.DEFAULT_AZURE_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME })
        static class EnableServiceBusConfiguration {

        }

    }

    static class MessagingListenerCondition extends AnyNestedCondition {

        MessagingListenerCondition() {
            super(ConfigurationPhase.PARSE_CONFIGURATION);
        }

        @ConditionalOnClass(EventHubsMessageListenerContainer.class)
        static class ConditionalOnEventHubs {

        }

        @ConditionalOnClass(ServiceBusMessageListenerContainer.class)
        static class ConditionalOnServiceBus {

        }

    }
}
