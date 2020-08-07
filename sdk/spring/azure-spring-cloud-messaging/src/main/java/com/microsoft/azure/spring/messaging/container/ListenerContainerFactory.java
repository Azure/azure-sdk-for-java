// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.messaging.container;

import com.microsoft.azure.spring.messaging.endpoint.AzureListenerEndpoint;

/**
 * Factory of {@link MessageListenerContainer} based on a
 * {@link AzureListenerEndpoint} definition.
 *
 * @param <C> the container type
 * @author Warren Zhu
 * @see AzureListenerEndpoint
 */
public interface ListenerContainerFactory<C extends MessageListenerContainer> {

    /**
     * Create a {@link MessageListenerContainer} for the given {@link AzureListenerEndpoint}.
     *
     * @param endpoint the endpoint to configure
     * @return the created container
     */
    C createListenerContainer(AzureListenerEndpoint endpoint);

}
