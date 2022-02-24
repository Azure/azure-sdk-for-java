// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.spring.messaging.ConsumerIdentifier;
import com.azure.spring.service.eventhubs.processor.EventProcessingListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.Lifecycle;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * A processor container using {@link EventProcessorClient} to subscribe to event hubs and consumer events from
 * all the partitions of each event hub.
 *
 * <p>
 * For different combinations of event hub and consumer group, different {@link EventProcessorClient}s will be created to
 * subscribe to it.
 * </p>
 *
 * Implementation of {@link EventProcessingListener} is required to be provided when using {@link EventHubsProcessorContainer}
 * to consume events.
 * @see EventProcessingListener
 */
public class EventHubsProcessorContainer implements Lifecycle, DisposableBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubsProcessorContainer.class);

    private final EventHubsProcessorFactory processorFactory;
    private final Map<ConsumerIdentifier, EventProcessorClient> clients = new HashMap<>();
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    /**
     * Create an instance using the supplied processor factory.
     * @param processorFactory the processor factory.
     */
    public EventHubsProcessorContainer(EventHubsProcessorFactory processorFactory) {
        this.processorFactory = processorFactory;
    }

    @Override
    public void destroy() {
        stop();
    }

    /**
     * Start all {@link EventProcessorClient}s created by this processor container to consume events from all partitions
     * of the associated destinations and consumer groups.
     */
    @Override
    public void start() {
        if (!isRunning.compareAndSet(false, true)) {
            LOGGER.info("Event processors container is already running");
            return;
        }
        this.clients.values().forEach(EventProcessorClient::start);
    }

    /**
     * Stop all {@link EventProcessorClient}s owned by this processor container to process events
     * for all partitions owned by the related event processor clients.
     */
    @Override
    public void stop() {
        if (!isRunning.compareAndSet(true, false)) {
            LOGGER.info("Event processors container has already stopped");
            return;
        }
        this.clients.values().forEach(EventProcessorClient::stop);
    }

    @Override
    public boolean isRunning() {
        return this.isRunning.get();
    }


    /**
     * Subscribe to an event hub in the context of a consumer group to consumer events from all the partitions.
     *
     * @param eventHubName the event hub name
     * @param consumerGroup the consumer group name
     * @param listener the listener to process Event Hub events.
     * @return the {@link EventProcessorClient} created to subscribe.
     */
    public EventProcessorClient subscribe(String eventHubName, String consumerGroup, EventProcessingListener listener) {
        EventProcessorClient processor = this.processorFactory.createProcessor(eventHubName, consumerGroup, listener);
        processor.start();

        this.clients.putIfAbsent(new ConsumerIdentifier(eventHubName, consumerGroup), processor);
        return processor;
    }

    /**
     * Unsubscribe to an event hub from a consumer group.
     * @param eventHubName the event hub name
     * @param consumerGroup the consumer group name
     * @return true if unsubscribe successfully
     */
    public boolean unsubscribe(String eventHubName, String consumerGroup) {
        synchronized (this.clients) {
            EventProcessorClient processor = this.clients.remove(new ConsumerIdentifier(eventHubName, consumerGroup));
            if (processor == null) {
                LOGGER.warn("No EventProcessorClient for event hub {}, consumer group {}", eventHubName, consumerGroup);
                return false;
            }
            processor.stop();
            return true;
        }
    }

}
