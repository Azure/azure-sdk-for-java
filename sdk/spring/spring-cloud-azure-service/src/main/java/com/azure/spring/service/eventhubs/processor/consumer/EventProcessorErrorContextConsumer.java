// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.processor.consumer;

import com.azure.messaging.eventhubs.models.ErrorContext;

import java.util.function.Consumer;

/**
 * ErrorContextConsumer is a functional interface for consuming {@link ErrorContext}.
 */
@FunctionalInterface
public interface EventProcessorErrorContextConsumer extends Consumer<ErrorContext> {
}
