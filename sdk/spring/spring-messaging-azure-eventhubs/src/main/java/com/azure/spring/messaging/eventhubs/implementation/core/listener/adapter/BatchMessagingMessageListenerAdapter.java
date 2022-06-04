// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.core.listener.adapter;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsBatchMessageListener;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.converter.AbstractAzureMessageConverter;
import com.azure.spring.messaging.eventhubs.support.EventHubsHeaders;
import com.azure.spring.messaging.eventhubs.support.converter.EventHubsBatchMessageConverter;
import com.azure.spring.messaging.implementation.listener.adapter.MessagingMessageListenerAdapter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter for Event Hubs batch message listener.
 */
public class BatchMessagingMessageListenerAdapter extends MessagingMessageListenerAdapter
    implements EventHubsBatchMessageListener {

    /**
     * Construct a {@link BatchMessagingMessageListenerAdapter} instance with default configuration.
     */
    public BatchMessagingMessageListenerAdapter() {
        this.messageConverter = new EventHubsBatchMessageConverter();
    }

    @Override
    public void onMessage(EventBatchContext eventBatchContext) {
        PartitionContext partition = eventBatchContext.getPartitionContext();

        Map<String, Object> headers = new HashMap<>();
        headers.put(AzureHeaders.RAW_PARTITION_ID, partition.getPartitionId());
        headers.put(EventHubsHeaders.LAST_ENQUEUED_EVENT_PROPERTIES,
            eventBatchContext.getLastEnqueuedEventProperties());

        Message<?> message = getMessageConverter().toMessage(eventBatchContext, new MessageHeaders(headers),
            payloadType);

        invokeHandler(message);
    }

    @Override
    @SuppressWarnings("unchecked")
    public AbstractAzureMessageConverter<EventBatchContext, EventData> getMessageConverter() {
        return (AbstractAzureMessageConverter<EventBatchContext, EventData>) super.getMessageConverter();
    }
}
