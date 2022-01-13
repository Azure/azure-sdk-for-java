// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.processor;


import com.azure.spring.service.eventhubs.processor.consumer.EventHubsCloseContextConsumer;
import com.azure.spring.service.eventhubs.processor.consumer.EventHubsErrorContextConsumer;
import com.azure.spring.service.eventhubs.processor.consumer.EventHubsInitializationContextConsumer;

/**
 * A listener to process Event Hub events.
 */
public interface EventProcessingListener {

    /**
     * Return the initialization context consumer for event hubs by default.
     * @return the initialization context consumer.
     */
    default EventHubsInitializationContextConsumer getInitializationContextConsumer() {
        return initializationContextConsumer -> { };
    }

    /**
     * Return the default close context consumer by default.
     * @return the close context consumer.
     */
    default EventHubsCloseContextConsumer getCloseContextConsumer() {
        return closeContext -> { };
    }

    /**
     * Return the default error context consumer by default.
     * @return the error context consumer.
     */
    default EventHubsErrorContextConsumer getErrorContextConsumer() {
        return errorContext -> { };
    }

}
