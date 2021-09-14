// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core;

import java.util.function.Consumer;

/**
 *
 */
public interface ServiceBusMessageProcessor<M, E> {

    Consumer<E> processError();

    Consumer<M> processMessage();
}
