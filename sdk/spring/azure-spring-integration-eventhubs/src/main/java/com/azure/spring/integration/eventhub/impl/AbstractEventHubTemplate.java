// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.impl;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.core.api.PartitionSupplier;
import com.azure.spring.integration.core.api.StartPosition;
import com.azure.spring.integration.eventhub.converter.EventHubMessageConverter;
import com.azure.spring.integration.eventhub.api.EventHubClientFactory;
import io.micrometer.core.instrument.Counter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
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

    private EventHubMessageConverter messageConverter = new EventHubMessageConverter();

    private StartPosition startPosition = StartPosition.LATEST;

    private CheckpointConfig checkpointConfig = CheckpointConfig.builder()
        .checkpointMode(CheckpointMode.RECORD).build();

    AbstractEventHubTemplate(EventHubClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    private static EventPosition buildEventPosition(StartPosition startPosition) {
        return StartPosition.EARLIEST.equals(startPosition) ? EventPosition.earliest() : EventPosition.latest();
    }

    public <T> Mono<Void> sendAsync(String eventHubName, @NonNull Message<T> message,
                                    PartitionSupplier partitionSupplier) {
        return sendAsync(eventHubName, Collections.singleton(message), partitionSupplier);
    }

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

    protected void createEventProcessorClient(String name, String consumerGroup, EventHubProcessor eventHubProcessor) {
        eventHubProcessor.setEventPosition(buildEventPosition(startPosition));
        this.clientFactory.createEventProcessorClient(name, consumerGroup, eventHubProcessor);
    }

    protected void startEventProcessorClient(String name, String consumerGroup) {
        this.clientFactory.getEventProcessorClient(name, consumerGroup).ifPresent(EventProcessorClient::start);
    }

    protected void stopEventProcessorClient(String name, String consumerGroup) {
        this.clientFactory.getEventProcessorClient(name, consumerGroup).ifPresent(eventProcessor -> {
            this.clientFactory.removeEventProcessorClient(name, consumerGroup);
            eventProcessor.stop();
        });
    }

    protected Map<String, Object> buildPropertiesMap() {
        Map<String, Object> properties = new HashMap<>();
        properties.put("startPosition", this.startPosition);
        properties.put("checkpointConfig", this.getCheckpointConfig());

        return properties;
    }

    public EventHubMessageConverter getMessageConverter() {
        return messageConverter;
    }

    public void setMessageConverter(EventHubMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }

    public StartPosition getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(StartPosition startPosition) {
        LOGGER.info("EventHubTemplate startPosition becomes: {}", startPosition);
        this.startPosition = startPosition;
    }

    public CheckpointConfig getCheckpointConfig() {
        return checkpointConfig;
    }

    public void setCheckpointConfig(CheckpointConfig checkpointConfig) {
        LOGGER.info("EventHubTemplate checkpoint config becomes: {}", checkpointConfig);
        this.checkpointConfig = checkpointConfig;
    }

}
