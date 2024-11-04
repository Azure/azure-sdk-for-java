// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.core.implementation.provisioning;

import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusProducerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ServiceBusChannelProvisionerTests {

    private ServiceBusChannelProvisioner provisioner;
    private ServiceBusProducerProperties producerProperties;
    private ServiceBusConsumerProperties consumerProperties;

    @BeforeEach
    void beforeEach() {
        provisioner = spy(ServiceBusChannelProvisioner.class);
        producerProperties = new ServiceBusProducerProperties();
        consumerProperties = new ServiceBusConsumerProperties();
    }

    @SuppressWarnings("deprecation")
    @Test
    void provisionProducerDestination() {
        ExtendedProducerProperties<ServiceBusProducerProperties> extendedProperties =
            new ExtendedProducerProperties<>(producerProperties);

        provisioner.provisionProducerDestination("test", extendedProperties);
        verify(provisioner, times(1)).validateOrCreateForProducer("test", producerProperties.getEntityType());
    }

    @SuppressWarnings("deprecation")
    @Test
    void provisionConsumerDestination() {
        ExtendedConsumerProperties<ServiceBusConsumerProperties> extendedProperties =
            new ExtendedConsumerProperties<>(consumerProperties);

        provisioner.provisionConsumerDestination("test", "group", extendedProperties);
        verify(provisioner, times(1)).validateOrCreateForConsumer("test", "group", consumerProperties.getEntityType());
    }
}
