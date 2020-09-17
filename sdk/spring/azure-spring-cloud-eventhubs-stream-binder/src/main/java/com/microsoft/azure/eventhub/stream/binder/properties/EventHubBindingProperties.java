// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhub.stream.binder.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 * @author Warren Zhu
 */
public class EventHubBindingProperties implements BinderSpecificPropertiesProvider {
    private EventHubConsumerProperties consumer = new EventHubConsumerProperties();
    private EventHubProducerProperties producer = new EventHubProducerProperties();

    public EventHubConsumerProperties getConsumer() {
        return consumer;
    }

    public void setConsumer(EventHubConsumerProperties consumer) {
        this.consumer = consumer;
    }

    public EventHubProducerProperties getProducer() {
        return producer;
    }

    public void setProducer(EventHubProducerProperties producer) {
        this.producer = producer;
    }
}
