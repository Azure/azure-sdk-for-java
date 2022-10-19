// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.config;

import com.azure.spring.messaging.implementation.annotation.AzureListenerConfigurer;
import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.listener.MessageListenerContainer;

/**
 * Model for a Azure listener endpoint. Can be used against a {@link AzureListenerConfigurer AzureListenerConfigurer} to
 * register endpoints programmatically.
 */
public interface AzureListenerEndpoint {

    /**
     * Return the ID of this endpoint.
     *
     * @return String value
     */
    String getId();

    /**
     * Get the destination.
     *
     * @return the destination.
     */
    String getDestination();

    /**
     * Get the group for the corresponding listener container.
     *
     * @return the group for the corresponding listener container.
     */
    String getGroup();

    /**
     * Set up the specified message listener container with the model defined by this endpoint.
     * <p>This endpoint must provide the requested missing option(s) of
     * the specified container to make it usable. Usually, this is about setting the {@code destination} and the {@code
     * messageListener} to use but an implementation may override any default setting that was already set.
     *
     * @param listenerContainer the listener container to configure
     * @param messageConverter the message converter to configure
     */
    void setupListenerContainer(MessageListenerContainer listenerContainer,
                                AzureMessageConverter<?, ?> messageConverter);

}
