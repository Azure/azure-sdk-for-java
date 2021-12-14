// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus.processor.consumer;

import com.azure.messaging.servicebus.ServiceBusErrorContext;

import java.util.function.Consumer;

/**
 * ErrorContextConsumer is a functional interface for consuming {@link ServiceBusErrorContext}.
 */
@FunctionalInterface
public interface ServiceBusErrorContextConsumer extends Consumer<ServiceBusErrorContext> {
}
