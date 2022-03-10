// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.listener;

import com.azure.spring.cloud.service.listener.MessageListener;
import org.springframework.context.SmartLifecycle;

/**
 * Internal abstraction used by the framework representing a message
 * listener container. Not meant to be implemented externally.
 */
public interface MessageListenerContainer extends SmartLifecycle {

    /**
     * Set up the message listener to use. Throws an {@link IllegalArgumentException} if that message listener type is
     * not supported.
     *
     * @param messageListener the object to wrapped to the MessageListener.
     */
    void setupMessageListener(MessageListener<?> messageListener);

    /**
     * Return the container properties for this container.
     * @return the container properties.
     */
    Object getContainerProperties();

}
