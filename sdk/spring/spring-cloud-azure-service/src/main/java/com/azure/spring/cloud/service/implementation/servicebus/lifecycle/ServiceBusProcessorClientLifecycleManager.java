// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.lifecycle;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.SmartLifecycle;

import java.util.Objects;

/**
 * Helper class to control the lifecycle of a {@link ServiceBusProcessorClient}.
 * This class implements {@code SmartLifecycle} to auto start {@link ServiceBusProcessorClient} when the Spring Application Context starts,
 * And stop the {@link ServiceBusProcessorClient} when the Spring Application Context stops.
 * NOTE, there is not need to call {@link #start()} or {@link #stop()} explicitly, as the {@link ServiceBusProcessorClient} will be started and stopped automatically.
 * And since the {@link ServiceBusProcessorClient} is a {@link AutoCloseable}, there is no need to call {@link ServiceBusProcessorClient#close()} explicitly.
 */
public class ServiceBusProcessorClientLifecycleManager implements SmartLifecycle {
    private final Logger logger = LoggerFactory.getLogger(ServiceBusProcessorClientLifecycleManager.class);
    /**
     * The {@link ServiceBusProcessorClient} to be controlled.
     */
    private final ServiceBusProcessorClient processorClient;

    /**
     * Construct the {@link ServiceBusProcessorClientLifecycleManager} with the {@link ServiceBusProcessorClient}.
     * @param processorClient
     */
    public ServiceBusProcessorClientLifecycleManager(ServiceBusProcessorClient processorClient) {
        Objects.requireNonNull(processorClient);
        this.processorClient = processorClient;
    }


    @Override
    public void start() {
        logger.debug("Starting Azure Service Bus processor client with queue name: {}, topic name: {} and subscription: {}", processorClient.getQueueName(), processorClient.getTopicName(), processorClient.getSubscriptionName());
        processorClient.start();
    }

    @Override
    public void stop() {
        logger.debug("Stopping Azure Service Bus processor client with queue name: {}, topic name: {} and subscription: {}", processorClient.getQueueName(), processorClient.getTopicName(), processorClient.getSubscriptionName());
        processorClient.stop();
    }

    @Override
    public boolean isRunning() {
        return processorClient.isRunning();
    }
}
