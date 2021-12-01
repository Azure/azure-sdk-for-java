// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.impl;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.spring.integration.core.api.BatchConsumerConfig;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.core.api.PartitionSupplier;
import com.azure.spring.integration.core.api.StartPosition;
import com.azure.spring.integration.eventhub.api.EventHubClientFactory;
import com.azure.spring.integration.eventhub.converter.EventHubMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Abstract base implementation of event hub template.
 *
 * <p>
 * The main event hub component for sending to and consuming from event hub
 *
 * @author Warren Zhu
 * @author Xiaolu Dai
 */
public class AbstractEventHubTemplate {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractEventHubTemplate.class);

    private final EventHubClientFactory clientFactory;

    /**
     * The message converter.
     */
    protected EventHubMessageConverter messageConverter = new EventHubMessageConverter();

    private StartPosition startPosition = StartPosition.LATEST;

    private CheckpointConfig checkpointConfig = CheckpointConfig.builder()
        .checkpointMode(CheckpointMode.RECORD).build();

    private BatchConsumerConfig batchConsumerConfig;

    /**
     *
     * @param clientFactory The client factory.
     */
    AbstractEventHubTemplate(EventHubClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    private static EventPosition buildEventPosition(StartPosition startPosition) {
        return StartPosition.EARLIEST.equals(startPosition) ? EventPosition.earliest() : EventPosition.latest();
    }

    /**
     *
     * @param eventHubName The event hub name.
     * @param message The message.
     * @param partitionSupplier The partition supplier.
     * @param <T> The type of message.
     * @return The mono.
     */
    public <T> Mono<Void> sendAsync(String eventHubName, @NonNull Message<T> message,
                                    PartitionSupplier partitionSupplier) {
        return sendAsync(eventHubName, Collections.singleton(message), partitionSupplier);
    }

    /**
     *
     * @param eventHubName The event hub name.
     * @param messages The messages.
     * @param partitionSupplier The partition supplier.
     * @param <T> The type of message.
     * @return The mono.
     */
    public <T> Mono<Void> sendAsync(String eventHubName, Collection<Message<T>> messages,
                                    PartitionSupplier partitionSupplier) {
        Assert.hasText(eventHubName, "eventHubName can't be null or empty");
        List<EventData> eventData = messages.stream().map(m -> messageConverter.fromMessage(m, EventData.class))
            .collect(Collectors.toList());
        return doSend(eventHubName, partitionSupplier, eventData);
    }

    private Mono<Void> doSend(String eventHubName, PartitionSupplier partitionSupplier,
                              List<EventData> events) {

        EventHubProducerAsyncClient producer = this.clientFactory.getOrCreateProducerClient(eventHubName);

        CreateBatchOptions options = buildCreateBatchOptions(partitionSupplier);

        return producer.createBatch(options).flatMap(batch -> {
            for (EventData event : events) {
                try {
                    batch.tryAdd(event);
                } catch (AmqpException e) {
                    LOGGER.error("Event is larger than maximum allowed size. Exception: " + e);
                }
            }
            return producer.send(batch);
        });
    }

    private CreateBatchOptions buildCreateBatchOptions(PartitionSupplier partitionSupplier) {
        return new CreateBatchOptions()
            .setPartitionId(partitionSupplier != null ? partitionSupplier.getPartitionId() : null)
            .setPartitionKey(partitionSupplier != null ? partitionSupplier.getPartitionKey() : null);
    }

    /**
     *
     * @param name The name.
     * @param consumerGroup The consumer group.
     * @param eventHubProcessor The event hub processor.
     */
    protected void createEventProcessorClient(String name, String consumerGroup, EventHubProcessor eventHubProcessor) {
        eventHubProcessor.setEventPosition(buildEventPosition(startPosition));
        this.clientFactory.createEventProcessorClient(name, consumerGroup, eventHubProcessor, batchConsumerConfig);
    }

    /**
     *
     * @param name The name.
     * @param consumerGroup The consumer group.
     */
    protected void startEventProcessorClient(String name, String consumerGroup) {
        this.clientFactory.getEventProcessorClient(name, consumerGroup).ifPresent(EventProcessorClient::start);
    }

    /**
     *
     * @param name The name.
     * @param consumerGroup The consumer group.
     */
    protected void stopEventProcessorClient(String name, String consumerGroup) {
        this.clientFactory.getEventProcessorClient(name, consumerGroup).ifPresent(eventProcessor -> {
            this.clientFactory.removeEventProcessorClient(name, consumerGroup);
            eventProcessor.stop();
        });
    }

    /**
     *
     * @return The properties map.
     */
    protected Map<String, Object> buildPropertiesMap() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("startPosition", this.startPosition);
        properties.put("checkpointConfig", this.getCheckpointConfig());

        return properties;
    }

    /**
     *
     * @return The message converter.
     */
    public EventHubMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     *
     * @param messageConverter The message converter.
     */
    public void setMessageConverter(EventHubMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    /**
     *
     * @return The start position.
     */
    public StartPosition getStartPosition() {
        return startPosition;
    }

    /**
     *
     * @param startPosition That start position.
     */
    public void setStartPosition(StartPosition startPosition) {
        LOGGER.info("EventHubTemplate startPosition becomes: {}", startPosition);
        this.startPosition = startPosition;
    }

    /**
     *
     * @return The check point config.
     */
    public CheckpointConfig getCheckpointConfig() {
        return checkpointConfig;
    }

    /**
     *
     * @param checkpointConfig The check point config.
     */
    public void setCheckpointConfig(CheckpointConfig checkpointConfig) {
        LOGGER.info("EventHubTemplate checkpoint config becomes: {}", checkpointConfig);
        this.checkpointConfig = checkpointConfig;
    }

    /**
     *
     * @return The batch consumer config.
     */
    public BatchConsumerConfig getBatchConsumerConfig() {
        return batchConsumerConfig;
    }

    /**
     *
     * @param batchConsumerConfig The batch consumer config.
     */
    public void setBatchConsumerConfig(BatchConsumerConfig batchConsumerConfig) {
        this.batchConsumerConfig = batchConsumerConfig;
    }

}
