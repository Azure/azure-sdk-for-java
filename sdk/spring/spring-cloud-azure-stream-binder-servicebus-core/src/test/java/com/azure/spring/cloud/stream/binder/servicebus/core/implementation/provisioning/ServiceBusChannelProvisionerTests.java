// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.core.implementation.provisioning;

import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusProducerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.spy;

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

    @Test
    void provisionProducerDestination() {
        ExtendedProducerProperties<ServiceBusProducerProperties> extendedProperties =
            new ExtendedProducerProperties<>(producerProperties);

        String destinationName = "test-producer-destination";

        ProducerDestination destination =
            provisioner.provisionProducerDestination(destinationName, extendedProperties);

        assertNotNull(destination);
        assertEquals(destinationName, destination.getName());
    }

    @Test
    void provisionConsumerDestination() {
        ExtendedConsumerProperties<ServiceBusConsumerProperties> extendedProperties =
            new ExtendedConsumerProperties<>(consumerProperties);

        String destinationName = "test-consumer-destination";
        String group = "test-group";

        ConsumerDestination destination =
            provisioner.provisionConsumerDestination(destinationName, group, extendedProperties);

        assertNotNull(destination);
        assertEquals(destinationName, destination.getName());
    }
}
