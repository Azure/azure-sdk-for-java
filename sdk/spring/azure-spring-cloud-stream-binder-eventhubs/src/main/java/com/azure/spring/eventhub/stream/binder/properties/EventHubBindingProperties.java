// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 * @author Warren Zhu
 */
public class EventHubBindingProperties implements BinderSpecificPropertiesProvider {
    private EventHubConsumerProperties consumer = new EventHubConsumerProperties();
    private EventHubProducerProperties producer = new EventHubProducerProperties();

    /**
     *
     * @return The consumer.
     */
    public EventHubConsumerProperties getConsumer() {
        return consumer;
    }

    /**
     *
     * @param consumer The consumer.
     */
    public void setConsumer(EventHubConsumerProperties consumer) {
        this.consumer = consumer;
    }

    /**
     *
     * @return The producer.
     */
    public EventHubProducerProperties getProducer() {
        return producer;
    }

    /**
     *
     * @param producer The producer.
     */
    public void setProducer(EventHubProducerProperties producer) {
        this.producer = producer;
    }
}
