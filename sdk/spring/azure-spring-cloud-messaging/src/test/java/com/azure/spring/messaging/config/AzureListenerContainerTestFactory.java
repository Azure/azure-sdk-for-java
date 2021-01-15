// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.config;

import com.azure.spring.messaging.container.ListenerContainerFactory;
import com.azure.spring.messaging.endpoint.AzureListenerEndpoint;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Warreen Zhu
 */
public class AzureListenerContainerTestFactory implements ListenerContainerFactory<MessageListenerTestContainer> {

    private final Map<String, MessageListenerTestContainer> listenerContainers = new LinkedHashMap<>();
    private boolean autoStartup = true;

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
        endpoint.setupListenerContainer(container);
        return container;
    }
}
