// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.container;

import com.azure.spring.messaging.endpoint.AbstractAzureListenerEndpoint;
import com.azure.spring.messaging.endpoint.AzureListenerEndpoint;
import com.azure.spring.integration.core.api.SubscribeByGroupOperation;
import org.springframework.lang.Nullable;
import org.springframework.util.ErrorHandler;

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

    @Nullable
    private ErrorHandler errorHandler;

    protected AbstractAzureListenerContainerFactory(SubscribeByGroupOperation subscribeOperation) {
        this.subscribeOperation = subscribeOperation;
    }

    @Override
    public C createListenerContainer(AzureListenerEndpoint endpoint) {
        C instance = createContainerInstance();
        if (this.errorHandler != null) {
            instance.setErrorHandler(this.errorHandler);
        }

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

    public void setErrorHandler(@Nullable ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
}
