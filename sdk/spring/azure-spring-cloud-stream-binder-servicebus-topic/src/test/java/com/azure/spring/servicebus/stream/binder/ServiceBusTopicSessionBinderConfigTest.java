// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.spring.cloud.autoconfigure.properties.AzureGlobalProperties;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.servicebus.stream.binder.config.ServiceBusTopicBinderConfiguration;
import com.azure.spring.servicebus.stream.binder.properties.ServiceBusConsumerProperties;
import com.azure.spring.servicebus.stream.binder.properties.ServiceBusTopicExtendedBindingProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceBusTopicSessionBinderConfigTest {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withBean(AzureGlobalProperties.class, AzureGlobalProperties::new)
        .withUserConfiguration(ServiceBusTopicBinderConfiguration.class)
        .withPropertyValues(
            "spring.cloud.azure.servicebus.connection-string=Endpoint=sb://test;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=test",
            "spring.cloud.stream.function.definition=consume;supply",
            "spring.cloud.stream.bindings.consume-in-0.destination=test",
            "spring.cloud.stream.bindings.consume-in-0.group=test",
            "spring.cloud.stream.bindings.supply-out-0.destination=test",
            "spring.cloud.stream.bindings.supply-out-0.group=test");

    @Test
    public void testServiceBusExtendedConsumerProperties() {
        contextRunner.withPropertyValues(
            "spring.cloud.stream.servicebus.topic.bindings.consume-in-0.consumer.sessionsEnabled:true",
            "spring.cloud.stream.servicebus.topic.bindings.consume-in-0.consumer.maxConcurrentCalls:10",
            "spring.cloud.stream.servicebus.topic.bindings.consume-in-0.consumer.maxConcurrentSessions:20",
            "spring.cloud.stream.servicebus.topic.bindings.consume-in-0.consumer.disableAutoComplete:true",
            "spring.cloud.stream.servicebus.topic.bindings.consume-in-0.consumer.serviceBusReceiveMode:RECEIVE_AND_DELETE")
            .run(context -> {
                ServiceBusConsumerProperties properties =
                    context.getBean(ServiceBusTopicExtendedBindingProperties.class).getBindings().get("consume-in-0").getConsumer();
                ExtendedConsumerProperties<ServiceBusConsumerProperties> serviceBusProperties = new ExtendedConsumerProperties<>(properties);
                ServiceBusClientConfig config = context.getBean(ServiceBusTopicMessageChannelBinder.class).buildClientConfig(serviceBusProperties);
                assertEquals(config.getMaxConcurrentCalls(), 10);
                assertEquals(config.getMaxConcurrentSessions(), 20);
                assertEquals(config.getServiceBusReceiveMode(), ServiceBusReceiveMode.RECEIVE_AND_DELETE);
                assertFalse(config.isEnableAutoComplete());
                assertTrue(config.isSessionsEnabled());
            });
    }

    @Test
    public void testServiceBusExtendedConsumerPropertiesSessionEnabledWithConcurrency() {
        contextRunner.withPropertyValues(
            "spring.cloud.stream.servicebus.topic.bindings.consume-in-0.consumer.sessionsEnabled:true",
            "spring.cloud.stream.servicebus.topic.bindings.consume-in-0.consumer.concurrency:20")
            .run(context -> {
                ServiceBusConsumerProperties properties =
                    context.getBean(ServiceBusTopicExtendedBindingProperties.class).getBindings().get("consume-in-0").getConsumer();
                ExtendedConsumerProperties<ServiceBusConsumerProperties> serviceBusProperties = new ExtendedConsumerProperties<>(properties);
                ServiceBusClientConfig config = context.getBean(ServiceBusTopicMessageChannelBinder.class).buildClientConfig(serviceBusProperties);
                assertEquals(config.getMaxConcurrentSessions(), 20);
                assertEquals(config.getMaxConcurrentCalls(), 1);
            });
    }

    @Test
    public void testServiceBusExtendedConsumerPropertiesSessionDisabledWithConcurrency() {
        contextRunner.withPropertyValues(
            "spring.cloud.stream.servicebus.topic.bindings.consume-in-0.consumer.sessionsEnabled:false",
            "spring.cloud.stream.servicebus.topic.bindings.consume-in-0.consumer.concurrency:20")
            .run(context -> {
                ServiceBusConsumerProperties properties =
                    context.getBean(ServiceBusTopicExtendedBindingProperties.class).getBindings().get("consume-in-0").getConsumer();
                ExtendedConsumerProperties<ServiceBusConsumerProperties> serviceBusProperties = new ExtendedConsumerProperties<>(properties);
                ServiceBusClientConfig config = context.getBean(ServiceBusTopicMessageChannelBinder.class).buildClientConfig(serviceBusProperties);
                assertEquals(config.getMaxConcurrentCalls(), 20);
                assertEquals(config.getMaxConcurrentSessions(), 1);
            });
    }
}
