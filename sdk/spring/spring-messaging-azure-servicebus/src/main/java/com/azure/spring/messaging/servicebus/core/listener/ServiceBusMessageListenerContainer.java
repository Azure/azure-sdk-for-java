// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.messaging.servicebus.core.listener;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.cloud.service.listener.MessageListener;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusErrorHandler;
import com.azure.spring.messaging.listener.AbstractMessageListenerContainer;
import com.azure.spring.messaging.servicebus.core.ServiceBusProcessorFactory;
import com.azure.spring.messaging.servicebus.core.properties.ServiceBusContainerProperties;
import org.springframework.util.StringUtils;

/**
 * A message listener container wrapping {@link ServiceBusProcessorClient} to subscribe to Service Bus queue/topic
 * entities and consume messages.
 * <p>
 * For different combinations of Service Bus entity name and subscription, different {@link ServiceBusProcessorClient}s
 * will be created to subscribe to it.
 * </p>
 *
 * @see AbstractMessageListenerContainer
 */
public class ServiceBusMessageListenerContainer extends AbstractMessageListenerContainer {

    private final ServiceBusProcessorFactory processorFactory;
    private final ServiceBusContainerProperties containerProperties;
    private ServiceBusErrorHandler errorHandler;
    private ServiceBusProcessorClient delegate;

    /**
     * Create an instance using the supplied processor factory and container properties.
     *
     * @param processorFactory the processor factory.
     * @param containerProperties the container properties.
     */
    public ServiceBusMessageListenerContainer(ServiceBusProcessorFactory processorFactory,
                                              ServiceBusContainerProperties containerProperties) {
        this.processorFactory = processorFactory;
        this.containerProperties = containerProperties == null ? new ServiceBusContainerProperties() : containerProperties;
    }

    @Override
    protected void doStart() {
        String entityName = containerProperties.getEntityName();
        String subscriptionName = containerProperties.getSubscriptionName();
        if (this.errorHandler != null) {
            this.containerProperties.setErrorHandler(errorHandler);
        }

        if (StringUtils.hasText(subscriptionName)) {
            this.delegate = this.processorFactory.createProcessor(entityName, subscriptionName, containerProperties);
        } else {
            this.delegate = this.processorFactory.createProcessor(entityName, containerProperties);
        }

        this.delegate.start();
    }

    @Override
    protected void doStop() {
        if (this.delegate != null) {
            this.delegate.stop();
        }
    }

    @Override
    public void setupMessageListener(MessageListener<?> messageListener) {
        this.containerProperties.setMessageListener(messageListener);
    }

    @Override
    public ServiceBusContainerProperties getContainerProperties() {
        return containerProperties;
    }

    /**
     * Set the error handler to call when the listener throws an exception.
     * @param errorHandler the error handler.
     */
    public void setErrorHandler(ServiceBusErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
}
