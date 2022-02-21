// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.endpoint;

import com.azure.spring.messaging.config.AbstractAzureListenerEndpoint;
import com.azure.spring.messaging.config.AzureListenerEndpoint;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.listener.MessageListenerContainer;
import com.azure.spring.messaging.listener.SimpleMessagingMessageListenerTestAdapter;
import com.azure.spring.messaging.listener.adapter.MessagingMessageListenerAdapter;

/**
 * A simple {@link AzureListenerEndpoint} simply providing the handler to
 * invoke to process an incoming message for this endpoint.
 *
 */
public class SimpleAzureListenerTestEndpoint extends AbstractAzureListenerEndpoint {


    @Override
    protected MessagingMessageListenerAdapter createMessageListener(MessageListenerContainer listenerContainer,
                                                                    AzureMessageConverter<?, ?> messageConverter) {
        return new SimpleMessagingMessageListenerTestAdapter();
    }
}
