// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.core.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 *
 */
public class EventHubsBindingProperties implements BinderSpecificPropertiesProvider {

    private EventHubsConsumerProperties consumer = new EventHubsConsumerProperties();
    private EventHubsProducerProperties producer = new EventHubsProducerProperties();

    /**
     * Get the consumer properties.
     *
     * @return consumer the consumer properties
     * @see EventHubsConsumerProperties
     */
    public EventHubsConsumerProperties getConsumer() {
        return consumer;
    }

    /**
     * Set the consumer properties.
     *
     * @param consumer the consumer properties
     */
    public void setConsumer(EventHubsConsumerProperties consumer) {
        this.consumer = consumer;
    }

    /**
     * Get the producer properties.
     *
     * @return producer the producer properties
     * @see EventHubsProducerProperties
     */
    public EventHubsProducerProperties getProducer() {
        return producer;
    }

    /**
     * Set the producer properties.
     *
     * @param producer the producer properties
     */
    public void setProducer(EventHubsProducerProperties producer) {
        this.producer = producer;
    }
}
