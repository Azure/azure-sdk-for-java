// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.impl;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.*;
import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.core.api.reactor.AzureCheckpointer;
import com.azure.spring.integration.core.api.reactor.Checkpointer;
import com.azure.spring.integration.eventhub.checkpoint.BatchCheckpointManager;
import com.azure.spring.integration.eventhub.checkpoint.CheckpointManager;
import com.azure.spring.integration.eventhub.converter.EventHubMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Mainly handle message conversion and checkpoint
 *
 * @author Warren Zhu
 */
public class EventHubProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubProcessor.class);
    protected final Consumer<Message<?>> consumer;
//    protected final Consumer<List<Message<?>>> batchConsumer;
    protected final Class<?> payloadType;
    protected final CheckpointConfig checkpointConfig;
    protected final EventHubMessageConverter messageConverter;
    protected final CheckpointManager checkpointManager;
    protected EventPosition eventPosition = EventPosition.latest();

//    public EventHubProcessor(Consumer<Message<?>> consumer, Class<?> payloadType, CheckpointConfig checkpointConfig,
//                             EventHubMessageConverter messageConverter) {
//        this(consumer, null, payloadType, checkpointConfig, messageConverter);
//    }
//
//    public EventHubProcessor(Consumer<Message<?>> consumer, Consumer<List<Message<?>>> batchConsumer, Class<?> payloadType, CheckpointConfig checkpointConfig,
//                             EventHubMessageConverter messageConverter) {
//        this.consumer = consumer;
//        this.batchConsumer = batchConsumer;
//        this.payloadType = payloadType;
//        this.checkpointConfig = checkpointConfig;
//        this.messageConverter = messageConverter;
//        this.checkpointManager = CheckpointManager.of(checkpointConfig);
//    }

    public EventHubProcessor(Consumer<Message<?>> consumer, Class<?> payloadType, CheckpointConfig checkpointConfig,
                             EventHubMessageConverter messageConverter) {
        this.consumer = consumer;
        this.payloadType = payloadType;
        this.checkpointConfig = checkpointConfig;
        this.messageConverter = messageConverter;
        this.checkpointManager = CheckpointManager.of(checkpointConfig);
    }

    public void onInitialize(InitializationContext context) {
        LOGGER.info("Started receiving on partition: {}", context.getPartitionContext().getPartitionId());
    }

    public void onClose(CloseContext context) {
        LOGGER.info("Stopped receiving on partition: {}. Reason: {}", context.getPartitionContext().getPartitionId(),
            context.getCloseReason());
    }

    public void onEvent(EventContext context) {
        PartitionContext partition = context.getPartitionContext();

        Map<String, Object> headers = new HashMap<>();
        headers.put(AzureHeaders.RAW_PARTITION_ID, partition.getPartitionId());

        final EventData event = context.getEventData();

        Checkpointer checkpointer = new AzureCheckpointer(context::updateCheckpointAsync);
        if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.MANUAL) {
            headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
        }

        this.consumer.accept(messageConverter.toMessage(event, new MessageHeaders(headers), payloadType));
        this.checkpointManager.onMessage(context, context.getEventData());

        if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.BATCH) {
            this.checkpointManager.completeBatch(context);
        }
    }

//    public void onEventBatch(EventBatchContext context) {
//        PartitionContext partition = context.getPartitionContext();
//
//        Map<String, Object> headers = new HashMap<>();
//        headers.put(AzureHeaders.RAW_PARTITION_ID, partition.getPartitionId());
//
//        final List<EventData> events = context.getEvents();
//
//        Checkpointer checkpointer = new AzureCheckpointer(context::updateCheckpointAsync);
//        if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.MANUAL) {
//            headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
//        }
//
//        List<Message<?>> springMessageList = new ArrayList<>();
//        for(EventData event: events) {
//            springMessageList.add(messageConverter.toMessage(event, new MessageHeaders(headers), payloadType))
//        }
//        this.batchConsumer.accept(springMessageList);
//
//        if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.BATCH) {
//            ((BatchCheckpointManager) this.checkpointManager).onMessages(context);
//        }
//    }
    public void onEventBatch(EventBatchContext context) {
        PartitionContext partition = context.getPartitionContext();

        Map<String, Object> headers = new HashMap<>();
        headers.put(AzureHeaders.RAW_PARTITION_ID, partition.getPartitionId());

        final List<EventData> events = context.getEvents();

        Checkpointer checkpointer = new AzureCheckpointer(context::updateCheckpointAsync);
        if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.MANUAL) {
            headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
        }

        List<Message<?>> springMessageList = new ArrayList<>();
        for(EventData event: events) {
            springMessageList.add(messageConverter.toMessage(event, new MessageHeaders(headers), payloadType))
        }
        this.consumer.accept(MessageBuilder.withPayload(springMessageList).copyHeaders(headers).build());

        if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.BATCH) {
            ((BatchCheckpointManager) this.checkpointManager).onMessages(context);
        }
    }
    public void onError(ErrorContext context) {
        LOGGER.error("Error occurred on partition: {}. Error: {}", context.getPartitionContext().getPartitionId(),
            context.getThrowable());
    }

    public void setEventPosition(EventPosition eventPosition) {
        this.eventPosition = eventPosition;
    }
}
