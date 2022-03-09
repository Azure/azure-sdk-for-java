// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.container;

import com.azure.spring.integration.core.api.SubscribeByGroupOperation;

/**
 * A {@link ListenerContainerFactory} implementation to build a
 * standard {@link DefaultMessageListenerContainer}.
 *
 * @author Warren Zhu
 */
public class DefaultAzureListenerContainerFactory
        extends AbstractAzureListenerContainerFactory<DefaultMessageListenerContainer> {

    /**
     * Construct the listener container factory with the {@link SubscribeByGroupOperation}.
     * @param subscribeOperation the {@link SubscribeByGroupOperation}.
     */
    public DefaultAzureListenerContainerFactory(SubscribeByGroupOperation subscribeOperation) {
        super(subscribeOperation);
    }

    @Override
    protected DefaultMessageListenerContainer createContainerInstance() {
        return new DefaultMessageListenerContainer(getSubscribeOperation());
    }

}
