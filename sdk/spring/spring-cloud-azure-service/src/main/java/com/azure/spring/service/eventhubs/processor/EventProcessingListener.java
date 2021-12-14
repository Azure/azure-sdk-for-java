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

    default EventHubsInitializationContextConsumer getInitializationContextConsumer() {
        return initializationContextConsumer -> { };
    }

    default EventHubsCloseContextConsumer getCloseContextConsumer() {
        return closeContext -> { };
    }

    default EventHubsErrorContextConsumer getErrorContextConsumer() {
        return errorContext -> { };
    }

}
