// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.messaging.container;

import com.microsoft.azure.spring.integration.core.api.SubscribeByGroupOperation;
import com.microsoft.azure.spring.messaging.endpoint.AbstractAzureListenerEndpoint;
import com.microsoft.azure.spring.messaging.endpoint.AzureListenerEndpoint;

/**
 * Base {@link ListenerContainerFactory} for Spring's base container implementation.
 *
 * @param <C> the container type
 * @author Warren Zhu
 * @see AbstractAzureListenerEndpoint
 */
abstract class AbstractAzureListenerContainerFactory<C extends AbstractListenerContainer>
        implements ListenerContainerFactory<C> {

    private final SubscribeByGroupOperation subscribeOperation;

    protected AbstractAzureListenerContainerFactory(SubscribeByGroupOperation subscribeOperation) {
        this.subscribeOperation = subscribeOperation;
    }

    @Override
    public C createListenerContainer(AzureListenerEndpoint endpoint) {
        C instance = createContainerInstance();
        initializeContainer(instance);
        endpoint.setupListenerContainer(instance);
        return instance;
    }

    /**
     * Create an empty container instance.
     *
     * @return C instance
     */
    protected abstract C createContainerInstance();

    /**
     * Further initialize the specified container.
     * <p>Subclasses can inherit from this method to apply extra
     * configuration if necessary.
     * @param instance instance
     */
    protected void initializeContainer(C instance) {
    }

    public SubscribeByGroupOperation getSubscribeOperation() {
        return subscribeOperation;
    }

}
