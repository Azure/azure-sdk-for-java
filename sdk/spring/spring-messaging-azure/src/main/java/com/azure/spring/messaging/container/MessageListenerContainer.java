// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.container;

import com.azure.spring.messaging.listener.AzureMessageHandler;
import org.springframework.context.SmartLifecycle;

/**
 * Internal abstraction used by the framework representing a message
 * listener container.
 */
public interface MessageListenerContainer extends SmartLifecycle {

    /**
     * Get an {@link AzureMessageHandler} implementation.
     * @return an AzureMessageHandler implementation.
     */
    AzureMessageHandler getMessageHandler();

    /**
     * Set an {@link AzureMessageHandler} implementation.
     * @param messageHandler an AzureMessageHandler implementation.
     */
    void setMessageHandler(AzureMessageHandler messageHandler);

    /**
     * Set the destination.
     * @param destination the destination.
     */
    void setDestination(String destination);

    /**
     * Set the consumer group.
     * @param group the consumer group.
     */
    void setGroup(String group);

}
