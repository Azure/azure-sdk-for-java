// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.processor;


import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.InitializationContext;

import java.util.function.Consumer;

/**
 * A listener to process Event Hub events.
 */
public interface EventProcessingListener {

    /**
     * Return the {@link Consumer} of {@link InitializationContext} for Event Hubs by default.
     * @return the initialization context consumer.
     */
    default Consumer<InitializationContext> getInitializationContextConsumer() {
        return initializationContextConsumer -> { };
    }

    /**
     * Return the {@link Consumer} of {@link CloseContext} for Event Hubs by default.
     * @return the close context consumer.
     */
    default Consumer<CloseContext> getCloseContextConsumer() {
        return closeContext -> { };
    }

    /**
     * Return the {@link Consumer} of {@link ErrorContext} for Event Hubs by default.
     * @return the error context consumer.
     */
    default Consumer<ErrorContext> getErrorContextConsumer() {
        return errorContext -> { };
    }

}
