// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.processor;


import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.InitializationContext;
import com.azure.spring.service.eventhubs.processor.consumer.EventHubsCloseContextConsumer;
import com.azure.spring.service.eventhubs.processor.consumer.EventHubsErrorContextConsumer;
import com.azure.spring.service.eventhubs.processor.consumer.EventHubsInitializationContextConsumer;

import java.util.function.Consumer;

/**
 * A listener to process Event Hub events.
 */
public interface EventProcessingListener {

    /**
     * Return the {@link Consumer} of {@link InitializationContext} for Event Hubs by default.
     * @return the initialization context consumer.
     */
    default EventHubsInitializationContextConsumer getInitializationContextConsumer() {
        return initializationContextConsumer -> { };
    }

    /**
     * Return the {@link Consumer} of {@link CloseContext} for Event Hubs by default.
     * @return the close context consumer.
     */
    default EventHubsCloseContextConsumer getCloseContextConsumer() {
        return closeContext -> { };
    }

    /**
     * Return the {@link Consumer} of {@link ErrorContext} for Event Hubs by default.
     * @return the error context consumer.
     */
    default EventHubsErrorContextConsumer getErrorContextConsumer() {
        return errorContext -> { };
    }

}
