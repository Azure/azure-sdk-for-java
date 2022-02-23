// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.config;

import com.azure.spring.messaging.converter.AzureMessageConverter;
import com.azure.spring.messaging.listener.AbstractMessageListenerContainer;
import com.azure.spring.messaging.listener.MessageListenerContainerFactory;

/**
 * Adapter for Azure message listener container factory {@link MessageListenerContainerFactory} implementation.
 *
 * @param <C> the container type
 * @see AbstractAzureListenerEndpoint
 */
public abstract class AzureMessageListenerContainerFactoryAdapter<C extends AbstractMessageListenerContainer>
    implements MessageListenerContainerFactory<C> {

    protected AzureMessageConverter<?, ?> messageConverter;

    @Override
    public C createListenerContainer(AzureListenerEndpoint endpoint) {
        C instance = createContainerInstance(endpoint);
        initializeContainer(instance);
        endpoint.setupListenerContainer(instance, messageConverter);
        return instance;
    }

    /**
     * Create an empty container instance.
     *
     * @param endpoint the Azure listener endpoint
     * @return C instance
     */
    protected abstract C createContainerInstance(AzureListenerEndpoint endpoint);

    /**
     * Further initialize the specified container.
     * <p>Subclasses can inherit from this method to apply extra
     * configuration if necessary.
     *
     * @param instance instance
     */
    protected void initializeContainer(C instance) {
    }

}
