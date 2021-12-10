// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 *
 */
public class EventHubsBindingProperties implements BinderSpecificPropertiesProvider {

    private EventHubsConsumerProperties consumer = new EventHubsConsumerProperties();
    private EventHubsProducerProperties producer = new EventHubsProducerProperties();

    /**
     * Get consumer
     *
     * @return consumer the consumer
     */
    public EventHubsConsumerProperties getConsumer() {
        return consumer;
    }

    /**
     * Set consumer
     *
     * @param consumer the consumer
     */
    public void setConsumer(EventHubsConsumerProperties consumer) {
        this.consumer = consumer;
    }

    /**
     * Get producer
     *
     * @return producer the producer
     */
    public EventHubsProducerProperties getProducer() {
        return producer;
    }

    /**
     * Set producer
     *
     * @param producer the producer
     */
    public void setProducer(EventHubsProducerProperties producer) {
        this.producer = producer;
    }
}
