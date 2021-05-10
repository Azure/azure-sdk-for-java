// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.sample.servicebus;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusReceiverAsyncClient;
import com.azure.messaging.servicebus.ServiceBusSenderAsyncClient;
import com.azure.messaging.servicebus.models.ServiceBusReceiveMode;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.azure.messaging.servicebus.models.ServiceBusReceiveMode.PEEK_LOCK;

@Configuration
@EnableConfigurationProperties(ServiceBusProperties.class)
public class ServiceBusConfiguration {

    private final ServiceBusProperties properties;

    public ServiceBusConfiguration(ServiceBusProperties properties) {
        this.properties = properties;
    }

    @Bean
    public ServiceBusSenderAsyncClient queueSender() {
        return new ServiceBusClientBuilder()
            .connectionString(properties.getConnectionString())
            .sender()
            .queueName(properties.getQueueName())
            .buildAsyncClient();
    }

    @Bean
    public ServiceBusReceiverAsyncClient queueReceiver() {
        return new ServiceBusClientBuilder()
            .connectionString(properties.getConnectionString())
            .receiver()
            .receiveMode(properties.getQueueReceiveMode() == null ? PEEK_LOCK : properties.getQueueReceiveMode())
            .queueName(properties.getQueueName())
            .buildAsyncClient();
    }

    @Bean
    public ServiceBusSenderAsyncClient topicSender() {
        return new ServiceBusClientBuilder()
            .connectionString(properties.getConnectionString())
            .sender()
            .topicName(properties.getTopicName())
            .buildAsyncClient();
    }

    @Bean
    public ServiceBusReceiverAsyncClient topicSubscriber() {
        final ServiceBusReceiveMode subscriptionReceiveMode = properties.getSubscriptionReceiveMode();

        return new ServiceBusClientBuilder()
            .connectionString(properties.getConnectionString())
            .receiver()
            .receiveMode(subscriptionReceiveMode == null ? PEEK_LOCK : subscriptionReceiveMode)
            .topicName(properties.getTopicName())
            .subscriptionName(properties.getSubscriptionName())
            .buildAsyncClient();
    }

}
