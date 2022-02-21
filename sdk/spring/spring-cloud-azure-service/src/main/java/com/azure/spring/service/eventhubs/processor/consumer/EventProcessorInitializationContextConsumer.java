// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.processor.consumer;

import com.azure.messaging.eventhubs.models.InitializationContext;

import java.util.function.Consumer;

/**
 * InitializationContextConsumer is a functional interface for consuming {@link InitializationContext}.
 */
@FunctionalInterface
public interface EventProcessorInitializationContextConsumer extends Consumer<InitializationContext> {
}
