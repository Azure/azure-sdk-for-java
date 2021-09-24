// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.checkpoint.CheckpointConfig;
import com.azure.spring.messaging.checkpoint.CheckpointMode;
import com.azure.spring.messaging.checkpoint.reactor.AzureCheckpointer;
import com.azure.spring.messaging.checkpoint.reactor.Checkpointer;
import com.azure.spring.eventhubs.support.converter.EventHubMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Mainly handle message conversion and checkpoint
 *
 * @author Xiaolu Dai
 */
public class EventHubProcessorSupport extends EventHubProcessor {

    public EventHubProcessorSupport(Consumer<Message<?>> consumer, Class<?> payloadType,
                                    CheckpointConfig checkpointConfig,
                                    EventHubMessageConverter messageConverter) {
        super(consumer, payloadType, checkpointConfig, messageConverter);
    }

    public void onEvent(EventContext context, EventData eventData) {
        PartitionContext partition = context.getPartitionContext();

        Map<String, Object> headers = new HashMap<>();
        headers.put(AzureHeaders.RAW_PARTITION_ID, partition.getPartitionId());

        Checkpointer checkpointer = new AzureCheckpointer(context::updateCheckpointAsync);
        if (checkpointConfig.getCheckpointMode() == CheckpointMode.MANUAL) {
            headers.put(AzureHeaders.CHECKPOINTER, checkpointer);
        }

        consumer.accept(messageConverter.toMessage(eventData, new MessageHeaders(headers), payloadType));
        checkpointManager.onMessage(context, eventData);
    }
}
