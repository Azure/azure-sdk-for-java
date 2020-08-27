// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.messaging.endpoint;

import com.microsoft.azure.spring.messaging.config.AzureListenerConfigurer;
import com.microsoft.azure.spring.messaging.container.MessageListenerContainer;

/**
 * Model for a Azure listener endpoint. Can be used against a
 * {@link AzureListenerConfigurer
 * AzureListenerConfigurer} to register endpoints programmatically.
 *
 * @author Warren Zhu
 */
public interface AzureListenerEndpoint {

    /**
     * Return the id of this endpoint.
     *
     * @return String value
     */
    String getId();

    /**
     * Setup the specified message listener container with the model
     * defined by this endpoint.
     * <p>This endpoint must provide the requested missing option(s) of
     * the specified container to make it usable. Usually, this is about
     * setting the {@code destination} and the {@code messageListener} to
     * use but an implementation may override any default setting that
     * was already set.
     *
     * @param listenerContainer the listener container to configure
     */
    void setupListenerContainer(MessageListenerContainer listenerContainer);

}
