// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.properties;

import com.azure.messaging.eventhubs.models.CloseContext;
import com.azure.messaging.eventhubs.models.InitializationContext;
import com.azure.spring.service.eventhubs.consumer.EventHubsErrorHandler;
import com.azure.spring.service.eventhubs.consumer.EventHubsMessageListener;

import java.util.function.Consumer;

/**
 * The properties to describe an Event Hubs listener container.
 */
public class EventHubsContainerProperties extends ProcessorProperties {

    private EventHubsMessageListener messageListener;

    private EventHubsErrorHandler errorContextConsumer;

    private Consumer<InitializationContext> initializationContextConsumer;

    private Consumer<CloseContext> closeContextConsumer;

    public EventHubsMessageListener getMessageListener() {
        return messageListener;
    }

    public void setMessageListener(EventHubsMessageListener messageListener) {
        this.messageListener = messageListener;
    }

    public EventHubsErrorHandler getErrorContextConsumer() {
        return errorContextConsumer;
    }

    public void setErrorContextConsumer(EventHubsErrorHandler errorContextConsumer) {
        this.errorContextConsumer = errorContextConsumer;
    }

    public Consumer<InitializationContext> getInitializationContextConsumer() {
        return initializationContextConsumer;
    }

    public void setInitializationContextConsumer(Consumer<InitializationContext> initializationContextConsumer) {
        this.initializationContextConsumer = initializationContextConsumer;
    }

    public Consumer<CloseContext> getCloseContextConsumer() {
        return closeContextConsumer;
    }

    public void setCloseContextConsumer(Consumer<CloseContext> closeContextConsumer) {
        this.closeContextConsumer = closeContextConsumer;
    }

}
