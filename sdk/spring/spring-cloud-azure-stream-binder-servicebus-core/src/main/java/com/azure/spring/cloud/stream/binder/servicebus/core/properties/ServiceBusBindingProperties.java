// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.core.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 *  Service Bus binding properties.
 */
public class ServiceBusBindingProperties implements BinderSpecificPropertiesProvider {

    /**
     * Creates an instance of {@link ServiceBusBindingProperties}.
     */
    public ServiceBusBindingProperties() {
    }

    private ServiceBusConsumerProperties consumer = new ServiceBusConsumerProperties();
    private ServiceBusProducerProperties producer = new ServiceBusProducerProperties();

    /**
     * Get the consumer properties.
     *
     * @return consumer the consumer properties
     */
    public ServiceBusConsumerProperties getConsumer() {
        return consumer;
    }

    /**
     * Set the consumer properties
     *
     * @param consumer the consumer properties
     */
    public void setConsumer(ServiceBusConsumerProperties consumer) {
        this.consumer = consumer;
    }

    /**
     * Get the producer properties.
     *
     * @return producer the producer properties
     */
    public ServiceBusProducerProperties getProducer() {
        return producer;
    }

    /**
     * Set the producer properties.
     *
     * @param producer the producer properties
     */
    public void setProducer(ServiceBusProducerProperties producer) {
        this.producer = producer;
    }
}
