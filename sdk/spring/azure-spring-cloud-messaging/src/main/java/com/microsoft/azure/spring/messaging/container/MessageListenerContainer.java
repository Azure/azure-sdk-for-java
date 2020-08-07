// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.messaging.container;

import com.microsoft.azure.spring.messaging.listener.AzureMessageHandler;
import org.springframework.context.SmartLifecycle;

/**
 * Internal abstraction used by the framework representing a message
 * listener container.
 *
 * @author Warren Zhu
 */
public interface MessageListenerContainer extends SmartLifecycle {

    AzureMessageHandler getMessageHandler();

    void setMessageHandler(AzureMessageHandler messageHandler);

    void setDestination(String destination);

    void setGroup(String group);

}
