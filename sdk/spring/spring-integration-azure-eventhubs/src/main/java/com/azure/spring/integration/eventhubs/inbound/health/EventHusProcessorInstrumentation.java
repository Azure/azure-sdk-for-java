// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhubs.inbound.health;

import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.spring.integration.instrumentation.AbstractProcessorInstrumentation;

import java.time.Duration;

/**
 * EventHus health details entity class.
 */
public class EventHusProcessorInstrumentation extends AbstractProcessorInstrumentation<ErrorContext> {

    /**
     * Construct a {@link EventHusProcessorInstrumentation} with the specified name, {@link Type} and the period of a none error window.
     *
     * @param name the name
     * @param type the type
     * @param noneErrorWindow the period of a none error window
     */
    public EventHusProcessorInstrumentation(String name, Type type, Duration noneErrorWindow) {
        super(name, type, noneErrorWindow);
    }

    @Override
    public Throwable getException() {
        return getErrorContext() == null ? null : getErrorContext().getThrowable();
    }
}
