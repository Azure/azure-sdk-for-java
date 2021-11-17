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
 * A processor container to subscribe on a {@link ServiceBusProcessorClient}.
 */
public class ServiceBusProcessorContainer implements Lifecycle, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusProcessorContainer.class);

    protected final ServiceBusProcessorFactory processorFactory;
    //TODO(yiliu6): map for client
    protected final List<ServiceBusProcessorClient> clients = new ArrayList<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private final List<ServiceBusProcessorFactory.Listener> listeners = new ArrayList<>();

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

    public ServiceBusProcessorClient subscribe(String queue, MessageProcessingListener listener) {
        ServiceBusProcessorClient processor = this.processorFactory.createProcessor(queue, listener);
        processor.start();
        this.clients.add(processor);
        return processor;
    }

    public boolean unsubscribe(String queue) {
        // TODO: stop and remove
        return false;
    }

    public ServiceBusProcessorClient subscribe(String topic, String subscription, MessageProcessingListener listener) {
        ServiceBusProcessorClient processor = this.processorFactory.createProcessor(topic, subscription, listener);
        processor.start();
        this.listeners.forEach(l -> l.processorAdded(topic, subscription));
        this.clients.add(processor);
        return processor;
    }

    public boolean unsubscribe(String topic, String subscription) {
        // TODO: stop and remove
        return false;
    }

}
