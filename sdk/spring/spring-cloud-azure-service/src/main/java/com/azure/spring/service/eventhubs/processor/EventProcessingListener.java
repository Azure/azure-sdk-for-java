// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.eventhubs.processor;


import com.azure.spring.service.eventhubs.processor.consumer.CloseContextConsumer;
import com.azure.spring.service.eventhubs.processor.consumer.ErrorContextConsumer;
import com.azure.spring.service.eventhubs.processor.consumer.InitializationContextConsumer;

/**
 * A listener to process Event Hub events.
 */
public interface EventProcessingListener {

    default InitializationContextConsumer getInitializationContextConsumer() {
        return initializationContextConsumer -> { };
    }

    default CloseContextConsumer getCloseContextConsumer() {
        return closeContext -> { };
    }

    default ErrorContextConsumer getErrorContextConsumer() {
        return errorContext -> { };
    }

}
