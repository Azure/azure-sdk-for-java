// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.container;

import com.azure.spring.messaging.listener.AzureMessageHandler;
import org.springframework.context.SmartLifecycle;

/**
 * Internal abstraction used by the framework representing a message
 * listener container.
 *
 * @author Warren Zhu
 */
public interface MessageListenerContainer extends SmartLifecycle {

    /**
     *
     * @return The message handler
     */
    AzureMessageHandler getMessageHandler();

    /**
     *
     * @param messageHandler The message handler
     */
    void setMessageHandler(AzureMessageHandler messageHandler);

    /**
     *
     * @param destination The destination
     */
    void setDestination(String destination);

    /**
     *
     * @param group The group
     */
    void setGroup(String group);

}
