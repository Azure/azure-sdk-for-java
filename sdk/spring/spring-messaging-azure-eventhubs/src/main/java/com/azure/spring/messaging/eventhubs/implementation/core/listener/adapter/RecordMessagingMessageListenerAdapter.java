// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.core.listener.adapter;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.spring.cloud.service.eventhubs.consumer.EventHubsRecordMessageListener;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.eventhubs.support.EventHubsHeaders;
import com.azure.spring.messaging.eventhubs.implementation.support.converter.EventHubsMessageConverter;
import com.azure.spring.messaging.implementation.listener.adapter.MessagingMessageListenerAdapter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter for Event Hubs record event listener.
 */
public class RecordMessagingMessageListenerAdapter extends MessagingMessageListenerAdapter
    implements EventHubsRecordMessageListener {

    /**
     * Construct a {@link RecordMessagingMessageListenerAdapter} instance with default configuration.
     */
    public RecordMessagingMessageListenerAdapter() {
        this.messageConverter = new EventHubsMessageConverter();
    }

    @Override
    public void onMessage(EventContext eventContext) {
        PartitionContext partition = eventContext.getPartitionContext();

        Map<String, Object> headers = new HashMap<>();
        headers.put(AzureHeaders.RAW_PARTITION_ID, partition.getPartitionId());
        headers.put(EventHubsHeaders.LAST_ENQUEUED_EVENT_PROPERTIES, eventContext.getLastEnqueuedEventProperties());

        final EventData event = eventContext.getEventData();


        Message<?> message = this.getMessageConverter().toMessage(event, new MessageHeaders(headers), payloadType);

        invokeHandler(message);
    }

    @SuppressWarnings("unchecked")
    @Override
    public AzureMessageConverter<EventData, EventData> getMessageConverter() {
        return (AzureMessageConverter<EventData, EventData>) super.getMessageConverter();
    }

}
