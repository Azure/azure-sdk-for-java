// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.listener;

import com.azure.spring.messaging.implementation.config.AzureListenerEndpoint;
import com.azure.spring.messaging.listener.MessageListenerContainer;

/**
 * Factory of {@link MessageListenerContainer} based on a
 * {@link AzureListenerEndpoint} definition.
 *
 * @param <C> the container type
 * @see AzureListenerEndpoint
 */
public interface MessageListenerContainerFactory<C extends MessageListenerContainer> {

    /**
     * Create a {@link MessageListenerContainer} for the given {@link AzureListenerEndpoint}.
     *
     * @param endpoint the endpoint to configure
     * @return the created container
     */
    C createListenerContainer(AzureListenerEndpoint endpoint);

}
