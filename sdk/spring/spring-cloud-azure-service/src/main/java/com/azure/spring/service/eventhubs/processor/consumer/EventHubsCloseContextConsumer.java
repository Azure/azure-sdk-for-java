// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.processor.consumer;

import com.azure.messaging.eventhubs.models.CloseContext;

import java.util.function.Consumer;

/**
 * CloseContextConsumer is a functional interface for consuming {@link CloseContext}.
 */
@FunctionalInterface
public interface EventHubsCloseContextConsumer extends Consumer<CloseContext> {
}
