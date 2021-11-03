// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.eventhubs.core.processor.EventHubProcessorFactory;
import com.azure.spring.service.eventhubs.processor.EventProcessingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.Lifecycle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 *
 */
public class EventHubProcessorContainer implements Lifecycle, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubProcessorContainer.class);

    private final EventHubProcessorFactory processorFactory;
    private final List<EventProcessorClient> clients = new ArrayList<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public EventHubProcessorContainer(EventHubProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    @Override
    public void destroy() {
        stop();
    }

    @Override
    public void start() {
        if (!isRunning.compareAndSet(false, true)) {
            LOGGER.info("Event processors container is already running");
            return;
        }
        this.clients.forEach(EventProcessorClient::start);
    }

    @Override
    public void stop() {
        if (!isRunning.compareAndSet(true, false)) {
            LOGGER.info("Event processors container has already stopped");
            return;
        }
        this.clients.forEach(EventProcessorClient::stop);
    }

    @Override
    public boolean isRunning() {
        return this.isRunning.get();
    }


    public EventProcessorClient subscribe(String eventHubName, String consumerGroup, EventProcessingListener listener) {
        EventProcessorClient processor = this.processorFactory.createProcessor(eventHubName, consumerGroup, listener);
        processor.start();

        this.clients.add(processor);
        return processor;
    }

}
