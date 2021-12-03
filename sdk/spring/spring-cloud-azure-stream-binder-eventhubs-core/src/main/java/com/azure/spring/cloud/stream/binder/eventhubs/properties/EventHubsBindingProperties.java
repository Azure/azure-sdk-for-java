// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 *
 */
public final class EventHubsBindingProperties implements BinderSpecificPropertiesProvider {

    private EventHubsConsumerProperties consumer = new EventHubsConsumerProperties();
    private EventHubsProducerProperties producer = new EventHubsProducerProperties();

    public EventHubsConsumerProperties getConsumer() {
        return consumer;
    }

    public void setConsumer(EventHubsConsumerProperties consumer) {
        this.consumer = consumer;
    }

    public EventHubsProducerProperties getProducer() {
        return producer;
    }

    public void setProducer(EventHubsProducerProperties producer) {
        this.producer = producer;
    }
}
