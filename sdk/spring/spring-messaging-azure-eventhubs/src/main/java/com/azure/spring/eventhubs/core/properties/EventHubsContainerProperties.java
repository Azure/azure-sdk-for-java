// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.properties;

import com.azure.spring.service.eventhubs.processor.EventHubsMessageListener;
import com.azure.spring.service.eventhubs.processor.consumer.EventProcessorCloseContextConsumer;
import com.azure.spring.service.eventhubs.processor.consumer.EventProcessorErrorContextConsumer;
import com.azure.spring.service.eventhubs.processor.consumer.EventProcessorInitializationContextConsumer;

/**
 * The properties to describe an Event Hubs listener container.
 */
public class EventHubsContainerProperties extends ProcessorProperties {

    private EventHubsMessageListener messageListener;

    private EventProcessorInitializationContextConsumer initializationContextConsumer;

    private EventProcessorCloseContextConsumer closeContextConsumer;

    private EventProcessorErrorContextConsumer errorContextConsumer;


    public EventHubsMessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(EventHubsMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public EventProcessorInitializationContextConsumer getInitializationContextConsumer() {
        return initializationContextConsumer;
    }

    public void setInitializationContextConsumer(EventProcessorInitializationContextConsumer initializationContextConsumer) {
        this.initializationContextConsumer = initializationContextConsumer;
    }

    public EventProcessorCloseContextConsumer getCloseContextConsumer() {
        return closeContextConsumer;
    }

    public void setCloseContextConsumer(EventProcessorCloseContextConsumer closeContextConsumer) {
        this.closeContextConsumer = closeContextConsumer;
    }

    public EventProcessorErrorContextConsumer getErrorContextConsumer() {
        return errorContextConsumer;
    }

    public void setErrorContextConsumer(EventProcessorErrorContextConsumer errorContextConsumer) {
        this.errorContextConsumer = errorContextConsumer;
    }
}
