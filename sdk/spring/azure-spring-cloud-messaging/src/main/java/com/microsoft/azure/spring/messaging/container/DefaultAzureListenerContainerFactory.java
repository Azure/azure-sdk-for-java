/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.messaging.container;

import com.microsoft.azure.spring.integration.core.api.SubscribeByGroupOperation;

/**
 * A {@link ListenerContainerFactory} implementation to build a
 * standard {@link DefaultMessageListenerContainer}.
 *
 * @author Warren Zhu
 */
public class DefaultAzureListenerContainerFactory
        extends AbstractAzureListenerContainerFactory<DefaultMessageListenerContainer> {

    public DefaultAzureListenerContainerFactory(SubscribeByGroupOperation subscribeOperation) {
        super(subscribeOperation);
    }

    @Override
    protected DefaultMessageListenerContainer createContainerInstance() {
        return new DefaultMessageListenerContainer(getSubscribeOperation());
    }

}
