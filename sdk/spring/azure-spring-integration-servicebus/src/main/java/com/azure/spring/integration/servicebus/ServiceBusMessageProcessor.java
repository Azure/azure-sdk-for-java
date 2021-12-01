// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus;

import java.util.function.Consumer;

/**
 * Service bus message processor.
 */
public interface ServiceBusMessageProcessor<M, E> {

    /**
     *
     * @return The process error consumer.
     */
    Consumer<E> processError();

    /**
     *
     * @return The process message consumer.
     */
    Consumer<M> processMessage();
}
