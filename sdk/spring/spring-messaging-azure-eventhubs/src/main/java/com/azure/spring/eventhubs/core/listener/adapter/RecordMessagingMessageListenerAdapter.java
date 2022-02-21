// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.listener.adapter;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.spring.eventhubs.support.EventHubsHeaders;
import com.azure.spring.eventhubs.support.converter.EventHubsMessageConverter;
import com.azure.spring.messaging.AzureHeaders;
import com.azure.spring.messaging.converter.AbstractAzureMessageConverter;
import com.azure.spring.messaging.listener.adapter.MessagingMessageListenerAdapter;
import com.azure.spring.service.eventhubs.processor.EventHubsRecordMessageListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter for Event Hubs record event listener.
 */
public class RecordMessagingMessageListenerAdapter extends MessagingMessageListenerAdapter
    implements EventHubsRecordMessageListener {

    private Class<?> payloadType = byte[].class;

    /**
     * Construct a {@link RecordMessagingMessageListenerAdapter} instance with default configuration.
     */
    public RecordMessagingMessageListenerAdapter() {
        this.messageConverter = new EventHubsMessageConverter();
    }

    @Override
    public void onEvent(EventContext eventContext) {
        PartitionContext partition = eventContext.getPartitionContext();

        Map<String, Object> headers = new HashMap<>();
        headers.put(AzureHeaders.RAW_PARTITION_ID, partition.getPartitionId());
        headers.put(EventHubsHeaders.LAST_ENQUEUED_EVENT_PROPERTIES, eventContext.getLastEnqueuedEventProperties());

        final EventData event = eventContext.getEventData();


        Message<?> message = this.getMessageConverter().toMessage(event, new MessageHeaders(headers), payloadType);

        invokeHandler(message);
    }

    /**
     * Set payload type.
     *
     * @param payloadType the payload type
     */
    public void setPayloadType(Class<?> payloadType) {
        this.payloadType = payloadType;
    }

    @Override
    @SuppressWarnings("unchecked")
    public AbstractAzureMessageConverter<EventData, EventData> getMessageConverter() {
        return (AbstractAzureMessageConverter<EventData, EventData>) super.getMessageConverter();
    }

}
