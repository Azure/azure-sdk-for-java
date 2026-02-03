// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.messaging;

import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.AzureEventHubsMessagingAutoConfiguration;
import com.azure.spring.cloud.autoconfigure.implementation.servicebus.AzureServiceBusMessagingAutoConfiguration;
import com.azure.spring.messaging.eventhubs.core.EventHubsProcessorFactory;
import com.azure.spring.messaging.eventhubs.core.listener.EventHubsMessageListenerContainer;
import com.azure.spring.messaging.eventhubs.implementation.core.annotation.EventHubsListenerAnnotationBeanPostProcessor;
import com.azure.spring.messaging.eventhubs.implementation.core.config.EventHubsMessageListenerContainerFactory;
import com.azure.spring.messaging.eventhubs.implementation.support.converter.EventHubsMessageConverter;
import com.azure.spring.messaging.implementation.annotation.AzureListenerAnnotationBeanPostProcessorAdapter;
import com.azure.spring.messaging.implementation.annotation.EnableAzureMessaging;
import com.azure.spring.messaging.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.messaging.servicebus.core.listener.ServiceBusMessageListenerContainer;
import com.azure.spring.messaging.servicebus.implementation.core.annotation.ServiceBusListenerAnnotationBeanPostProcessor;
import com.azure.spring.messaging.servicebus.implementation.core.config.ServiceBusMessageListenerContainerFactory;
import com.azure.spring.messaging.servicebus.implementation.support.converter.ServiceBusMessageConverter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

public class AzureMessagingListenerAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            AzureEventHubsMessagingAutoConfiguration.class,
            AzureServiceBusMessagingAutoConfiguration.class,
            AzureMessagingListenerAutoConfiguration.class));

    @Test
    void withoutEnableAzureMessagingShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(EnableAzureMessaging.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureMessagingListenerAutoConfiguration.EventHubsConfiguration.class);
                assertThat(context).doesNotHaveBean(AzureMessagingListenerAutoConfiguration.ServiceBusConfiguration.class);
            });
    }

    @Test
    void withoutEventHubsMessageListenerContainerShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(EventHubsMessageListenerContainer.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureMessagingListenerAutoConfiguration.EventHubsConfiguration.class);
            });
    }

    @Test
    void withoutServiceBusMessageListenerContainerShouldNotConfigure() {
        this.contextRunner
            .withClassLoader(new FilteredClassLoader(ServiceBusMessageListenerContainer.class))
            .run(context -> {
                assertThat(context).doesNotHaveBean(AzureMessagingListenerAutoConfiguration.ServiceBusConfiguration.class);
            });
    }

    @Test
    void configureContainerFactories() {
        this.contextRunner
            .withBean(EventHubsMessageConverter.class, () -> mock(EventHubsMessageConverter.class))
            .withBean(EventHubsProcessorFactory.class, () -> mock(EventHubsProcessorFactory.class))
            .withBean(ServiceBusMessageConverter.class, () -> mock(ServiceBusMessageConverter.class))
            .withBean(ServiceBusProcessorFactory.class, () -> mock(ServiceBusProcessorFactory.class))
            .run(context -> {
                assertThat(context).hasBean("azureEventHubsListenerContainerFactory");
                assertThat(context).hasBean("azureServiceBusListenerContainerFactory");

                assertThat(context).hasBean(EventHubsListenerAnnotationBeanPostProcessor.DEFAULT_EVENT_HUBS_LISTENER_ANNOTATION_BPP_BEAN_NAME);
                assertThat(context).hasBean(ServiceBusListenerAnnotationBeanPostProcessor.DEFAULT_SERVICE_BUS_LISTENER_ANNOTATION_BPP_BEAN_NAME);
                assertThat(context).hasBean(AzureListenerAnnotationBeanPostProcessorAdapter.DEFAULT_AZURE_LISTENER_ENDPOINT_REGISTRY_BEAN_NAME);

                EventHubsMessageConverter eventHubsMessageConverter = context.getBean(EventHubsMessageConverter.class);
                EventHubsMessageListenerContainerFactory eventHubsContainerFactory = (EventHubsMessageListenerContainerFactory) context.getBean("azureEventHubsListenerContainerFactory");
                assertSame(eventHubsMessageConverter, eventHubsContainerFactory.getMessageConverter());

                ServiceBusMessageConverter messageConverter = context.getBean(ServiceBusMessageConverter.class);
                ServiceBusMessageListenerContainerFactory serviceBusContainerFactory = (ServiceBusMessageListenerContainerFactory) context.getBean("azureServiceBusListenerContainerFactory");
                assertSame(messageConverter, serviceBusContainerFactory.getMessageConverter());
            });
    }
}
