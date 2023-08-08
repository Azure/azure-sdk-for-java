// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.eventhubs.consumer;

import com.azure.messaging.eventhubs.models.ErrorContext;

import java.util.function.Consumer;

/**
 * The error handler to handle errors when listening to Event Hubs.
 */
@FunctionalInterface
public interface EventHubsErrorHandler extends Consumer<ErrorContext> {

}
