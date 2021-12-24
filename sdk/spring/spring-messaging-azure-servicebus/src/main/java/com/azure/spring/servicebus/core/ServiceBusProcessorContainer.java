// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.servicebus.core;

import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.spring.service.servicebus.processor.MessageProcessingListener;
import com.azure.spring.servicebus.core.processor.ServiceBusProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.Lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A processor container using {@link ServiceBusProcessorClient} to subscribe to Service Bus queue/topic entities and
 * consumer messages.
 * <p>
 * For different combinations of Service Bus entity name and subscription, different {@link ServiceBusProcessorClient}s will be created to
 * subscribe to it.
 * </p>
 *
 * Implementation of {@link MessageProcessingListener} is required to be provided when using {@link ServiceBusProcessorClient}
 * to consume messages.
 * @see MessageProcessingListener
 */
public class ServiceBusProcessorContainer implements Lifecycle, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusProcessorContainer.class);

    protected final ServiceBusProcessorFactory processorFactory;
    //TODO(yiliu6): map for client
    protected final List<ServiceBusProcessorClient> clients = new ArrayList<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final List<ServiceBusProcessorFactory.Listener> listeners = new ArrayList<>();

    /**
     * Create an instance using the supplied processor factory.
     * @param processorFactory the processor factory.
     */
    public ServiceBusProcessorContainer(ServiceBusProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    @Override
    public void destroy() throws Exception {
        this.clients.forEach(ServiceBusProcessorClient::close);
    }

    @Override
    public void start() {
        if (!isRunning.compareAndSet(false, true)) {
            LOGGER.info("Service Bus processors container is already running");
            return;
        }
        this.clients.forEach(ServiceBusProcessorClient::start);
    }

    @Override
    public void stop() {
        if (!isRunning.compareAndSet(true, false)) {
            LOGGER.info("Service Bus processors container has already stopped");
            return;
        }
        this.clients.forEach(ServiceBusProcessorClient::stop);
    }

    @Override
    public boolean isRunning() {
        return this.isRunning.get();
    }

    /**
     * Subscribe to a queue to consumer messages.
     *
     * @param queue the queue
     * @param listener the listener to process messages.
     * @return the {@link ServiceBusProcessorClient} created to subscribe to the queue.
     */
    public ServiceBusProcessorClient subscribe(String queue, MessageProcessingListener listener) {
        ServiceBusProcessorClient processor = this.processorFactory.createProcessor(queue, listener);
        processor.start();
        this.clients.add(processor);
        return processor;
    }

    /**
     * Unsubscribe to a queue.
     * @param queue the queue.
     * @return true if unsubscribe successfully.
     */
    public boolean unsubscribe(String queue) {
        // TODO: stop and remove
        return false;
    }

    /**
     * Subscribe to a topic in the context of a subscription to consumer messages.
     *
     * @param topic the topic.
     * @param subscription the subscription.
     * @param listener the listener to process messages.
     * @return the {@link ServiceBusProcessorClient} created to subscribe to the topic.
     */
    public ServiceBusProcessorClient subscribe(String topic, String subscription, MessageProcessingListener listener) {
        ServiceBusProcessorClient processor = this.processorFactory.createProcessor(topic, subscription, listener);
        processor.start();
        this.listeners.forEach(l -> l.processorAdded(topic, subscription, processor));
        this.clients.add(processor);
        return processor;
    }

    /**
     * Unsubscribe to a queue.
     * @param topic the topic.
     * @param subscription the subscription.
     * @return true if unsubscribe successfully.
     */
    public boolean unsubscribe(String topic, String subscription) {
        // TODO: stop and remove
        return false;
    }

}
