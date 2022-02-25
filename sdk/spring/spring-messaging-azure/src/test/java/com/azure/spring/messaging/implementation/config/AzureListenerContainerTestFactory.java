// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.config;

import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.implementation.listener.MessageListenerContainerFactory;
import com.azure.spring.messaging.listener.MessageListenerTestContainer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class AzureListenerContainerTestFactory implements MessageListenerContainerFactory<MessageListenerTestContainer> {

    private final Map<String, MessageListenerTestContainer> listenerContainers = new LinkedHashMap<>();
    private boolean autoStartup = true;
    protected AzureMessageConverter<?, ?> messageConverter;

    public void setAutoStartup(boolean autoStartup) {
        this.autoStartup = autoStartup;
    }

    public List<MessageListenerTestContainer> getListenerContainers() {
        return new ArrayList<>(this.listenerContainers.values());
    }

    public MessageListenerTestContainer getListenerContainer(String id) {
        return this.listenerContainers.get(id);
    }

    @Override
    public MessageListenerTestContainer createListenerContainer(AzureListenerEndpoint endpoint) {
        MessageListenerTestContainer container = new MessageListenerTestContainer(endpoint);
        container.setAutoStartup(this.autoStartup);
        this.listenerContainers.put(endpoint.getId(), container);
        endpoint.setupListenerContainer(container, messageConverter);
        return container;
    }
}
