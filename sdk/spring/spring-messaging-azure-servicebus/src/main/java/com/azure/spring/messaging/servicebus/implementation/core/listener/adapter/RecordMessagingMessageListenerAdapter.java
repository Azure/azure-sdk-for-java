// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.core.listener.adapter;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.implementation.listener.adapter.MessagingMessageListenerAdapter;
import com.azure.spring.cloud.service.servicebus.consumer.ServiceBusRecordMessageListener;
import com.azure.spring.messaging.servicebus.support.ServiceBusMessageHeaders;
import com.azure.spring.messaging.servicebus.implementation.support.converter.ServiceBusMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;

import java.util.HashMap;
import java.util.Map;

/**
 * Adapter for record event listener.
 */
public class RecordMessagingMessageListenerAdapter extends MessagingMessageListenerAdapter
    implements ServiceBusRecordMessageListener {

    /**
     * Construct a {@link RecordMessagingMessageListenerAdapter} instance with default configuration.
     */
    public RecordMessagingMessageListenerAdapter() {
        this.messageConverter = new ServiceBusMessageConverter();
    }

    @Override
    public void onMessage(ServiceBusReceivedMessageContext messageContext) {
        Map<String, Object> headers = new HashMap<>();
        headers.put(ServiceBusMessageHeaders.RECEIVED_MESSAGE_CONTEXT, messageContext);

        Message<?> message = getMessageConverter().toMessage(messageContext.getMessage(), new MessageHeaders(headers),
            payloadType);
        invokeHandler(message);
    }

    @SuppressWarnings("unchecked")
    @Override
    public AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage> getMessageConverter() {
        return (AzureMessageConverter<ServiceBusReceivedMessage, ServiceBusMessage>) super.getMessageConverter();
    }
}
