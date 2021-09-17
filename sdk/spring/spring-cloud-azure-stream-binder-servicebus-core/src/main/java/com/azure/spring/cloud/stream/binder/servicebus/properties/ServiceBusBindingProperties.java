// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.properties;

import org.springframework.cloud.stream.binder.BinderSpecificPropertiesProvider;

/**
 * @author Warren Zhu
 */
public class ServiceBusBindingProperties implements BinderSpecificPropertiesProvider {
    private ServiceBusConsumerProperties consumer = new ServiceBusConsumerProperties();
    private ServiceBusProducerProperties producer = new ServiceBusProducerProperties();

    public ServiceBusConsumerProperties getConsumer() {
        return consumer;
    }

    public void setConsumer(ServiceBusConsumerProperties consumer) {
        this.consumer = consumer;
    }

    public ServiceBusProducerProperties getProducer() {
        return producer;
    }

    public void setProducer(ServiceBusProducerProperties producer) {
        this.producer = producer;
    }
}
