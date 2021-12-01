// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.impl;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.InitializationContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.spring.integration.core.AzureHeaders;
import com.azure.spring.integration.core.api.CheckpointConfig;
import com.azure.spring.integration.core.api.CheckpointMode;
import com.azure.spring.integration.core.api.reactor.AzureCheckpointer;
import com.azure.spring.integration.core.api.reactor.Checkpointer;
import com.azure.spring.integration.eventhub.checkpoint.BatchCheckpointManager;
import com.azure.spring.integration.eventhub.checkpoint.CheckpointManager;
import com.azure.spring.integration.eventhub.converter.EventHubBatchMessageConverter;
import com.azure.spring.integration.eventhub.converter.EventHubMessageConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Mainly handle message conversion and checkpoint
 *
 * @author Warren Zhu
 */
public class EventHubProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubProcessor.class);

    /**
     * The consumer.
     */
    protected final Consumer<Message<?>> consumer;

    /**
     * The payload type.
     */
    protected final Class<?> payloadType;

    /**
     * The check point config.
     */
    protected final CheckpointConfig checkpointConfig;

    /**
     * The message converter.
     */
    protected final EventHubMessageConverter messageConverter;

    /**
     * The batch message converter.
     */
    protected final EventHubBatchMessageConverter batchMessageConverter = new EventHubBatchMessageConverter();

    /**
     * The check pointer manager.
     */
    protected final CheckpointManager checkpointManager;

    /**
     * The event position.
     */
    protected EventPosition eventPosition = EventPosition.latest();

    /**
     *
     * @param consumer The consumer.
     * @param payloadType The payload type.
     * @param checkpointConfig The check point config.
     * @param messageConverter The message converter.
     */
    public EventHubProcessor(Consumer<Message<?>> consumer, Class<?> payloadType, CheckpointConfig checkpointConfig,
                             EventHubMessageConverter messageConverter) {
        this.consumer = consumer;
        this.payloadType = payloadType;
        this.checkpointConfig = checkpointConfig;
        this.messageConverter = messageConverter;
        this.checkpointManager = CheckpointManager.of(checkpointConfig);
    }

    /**
     *
     * @param context The initialization context.
     */
    public void onInitialize(InitializationContext context) {
        LOGGER.info("Started receiving on partition: {}", context.getPartitionContext().getPartitionId());
    }

    /**
     *
     * @param context The close context.
     */
    public void onClose(CloseContext context) {
        LOGGER.info("Stopped receiving on partition: {}. Reason: {}", context.getPartitionContext().getPartitionId(),
            context.getCloseReason());
    }

    /**
     *
     * @param context The event context.
     */
    public void onEvent(EventContext context) {
        Map<String, Object> headers = new HashMap<>();

        PartitionContext partition = context.getPartitionContext();
        headers.put(AzureHeaders.RAW_PARTITION_ID, partition.getPartitionId());

        final EventData event = context.getEventData();

        if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.MANUAL) {
            Checkpointer checkpointer = new AzureCheckpointer(context::updateCheckpointAsync);
            headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
        }

        this.consumer.accept(messageConverter.toMessage(event, new MessageHeaders(headers), payloadType));
        this.checkpointManager.onMessage(context, context.getEventData());

        if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.BATCH) {
            this.checkpointManager.completeBatch(context);
        }
    }

    /**
     *
     * @param context The event batch context.
     */
    public void onEventBatch(EventBatchContext context) {
        Map<String, Object> headers = new HashMap<>();

        if (context.getEvents().size() == 0) {
            return;
        }

        PartitionContext partition = context.getPartitionContext();
        headers.put(AzureHeaders.RAW_PARTITION_ID, partition.getPartitionId());

        if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.MANUAL) {
            Checkpointer checkpointer = new AzureCheckpointer(context::updateCheckpointAsync);
            headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
        }

        this.consumer.accept(batchMessageConverter.toMessage(context, new MessageHeaders(headers), payloadType));
        if (this.checkpointConfig.getCheckpointMode() == CheckpointMode.BATCH) {
            ((BatchCheckpointManager) this.checkpointManager).onMessages(context);
        }
    }

    /**
     *
     * @param context The error context.
     */
    public void onError(ErrorContext context) {
        LOGGER.error("Error occurred on partition: {}. Error: {}", context.getPartitionContext().getPartitionId(),
            context.getThrowable());
    }

    /**
     *
     * @param eventPosition The event position.
     */
    public void setEventPosition(EventPosition eventPosition) {
        this.eventPosition = eventPosition;
    }
}
