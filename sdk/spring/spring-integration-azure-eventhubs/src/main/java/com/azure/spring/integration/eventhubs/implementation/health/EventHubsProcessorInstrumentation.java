// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhubs.implementation.health;

import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.spring.integration.core.implementation.instrumentation.AbstractProcessorInstrumentation;

import java.time.Duration;

/**
 * EventHubs health details entity class.
 */
public class EventHubsProcessorInstrumentation extends AbstractProcessorInstrumentation<ErrorContext> {

    /**
     * Construct a {@link EventHubsProcessorInstrumentation} with the specified name, {@link Type} and the period of a none error window.
     *
     * @param name the name
     * @param type the type
     * @param noneErrorWindow the period of a none error window
     */
    public EventHubsProcessorInstrumentation(String name, Type type, Duration noneErrorWindow) {
        super(name, type, noneErrorWindow);
    }

    @Override
    public Throwable getException() {
        return getErrorContext() == null ? null : getErrorContext().getThrowable();
    }
}
