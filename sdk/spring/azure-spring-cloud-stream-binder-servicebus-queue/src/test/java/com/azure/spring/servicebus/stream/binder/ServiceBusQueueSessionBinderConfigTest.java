package com.azure.spring.servicebus.stream.binder;

import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import com.azure.spring.integration.servicebus.ServiceBusClientConfig;
import com.azure.spring.servicebus.stream.binder.config.ServiceBusQueueBinderConfiguration;
import com.azure.spring.servicebus.stream.binder.properties.ServiceBusConsumerProperties;
import com.azure.spring.servicebus.stream.binder.properties.ServiceBusQueueExtendedBindingProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ServiceBusQueueSessionBinderConfigTest {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withUserConfiguration(ServiceBusQueueBinderConfiguration.class)
        .withPropertyValues(
            "spring.cloud.azure.servicebus.connection-string=Endpoint=sb://test;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=test",
            "spring.cloud.stream.function.definition=consume;supply",
            "spring.cloud.stream.bindings.consume-in-0.destination=test",
            "spring.cloud.stream.bindings.supply-out-0.destination=test");

    @Test
    public void testServiceBusExtendedConsumerProperties() {
        contextRunner.withPropertyValues(
            "spring.cloud.stream.servicebus.queue.bindings.consume-in-0.consumer.sessionsEnabled:true",
            "spring.cloud.stream.servicebus.queue.bindings.consume-in-0.consumer.maxConcurrentCalls:10",
            "spring.cloud.stream.servicebus.queue.bindings.consume-in-0.consumer.maxConcurrentSessions:20",
            "spring.cloud.stream.servicebus.queue.bindings.consume-in-0.consumer.disableAutoComplete:true",
            "spring.cloud.stream.servicebus.queue.bindings.consume-in-0.consumer.serviceBusReceiveMode:RECEIVE_AND_DELETE")
            .run(context -> {
                ServiceBusConsumerProperties properties =
                    context.getBean(ServiceBusQueueExtendedBindingProperties.class).getBindings().get("consume-in-0").getConsumer();
                ExtendedConsumerProperties<ServiceBusConsumerProperties> serviceBusProperties = new ExtendedConsumerProperties<>(properties);
                ServiceBusClientConfig config = context.getBean(ServiceBusQueueMessageChannelBinder.class).buildClientConfig(serviceBusProperties);
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
            "spring.cloud.stream.servicebus.queue.bindings.consume-in-0.consumer.sessionsEnabled:true",
            "spring.cloud.stream.servicebus.queue.bindings.consume-in-0.consumer.concurrency:20")
            .run(context -> {
                ServiceBusConsumerProperties properties =
                    context.getBean(ServiceBusQueueExtendedBindingProperties.class).getBindings().get("consume-in-0").getConsumer();
                ExtendedConsumerProperties<ServiceBusConsumerProperties> serviceBusProperties = new ExtendedConsumerProperties<>(properties);
                ServiceBusClientConfig config = context.getBean(ServiceBusQueueMessageChannelBinder.class).buildClientConfig(serviceBusProperties);
                assertEquals(config.getMaxConcurrentSessions(), 20);
                assertEquals(config.getMaxConcurrentCalls(), 1);
            });
    }

    @Test
    public void testServiceBusExtendedConsumerPropertiesSessionDisabledWithConcurrency() {
        contextRunner.withPropertyValues(
            "spring.cloud.stream.servicebus.queue.bindings.consume-in-0.consumer.sessionsEnabled:false",
            "spring.cloud.stream.servicebus.queue.bindings.consume-in-0.consumer.concurrency:20")
            .run(context -> {
                ServiceBusConsumerProperties properties =
                    context.getBean(ServiceBusQueueExtendedBindingProperties.class).getBindings().get("consume-in-0").getConsumer();
                ExtendedConsumerProperties<ServiceBusConsumerProperties> serviceBusProperties = new ExtendedConsumerProperties<>(properties);
                ServiceBusClientConfig config = context.getBean(ServiceBusQueueMessageChannelBinder.class).buildClientConfig(serviceBusProperties);
                assertEquals(config.getMaxConcurrentCalls(), 20);
                assertEquals(config.getMaxConcurrentSessions(), 1);
            });
    }
}
