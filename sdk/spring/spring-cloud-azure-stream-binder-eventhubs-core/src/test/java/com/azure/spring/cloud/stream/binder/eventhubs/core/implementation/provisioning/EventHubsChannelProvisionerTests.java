// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.core.implementation.provisioning;

import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsConsumerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.core.properties.EventHubsProducerProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class EventHubsChannelProvisionerTests {

    private EventHubsChannelProvisioner provisioner;
    private EventHubsProducerProperties producerProperties;
    private EventHubsConsumerProperties consumerProperties;

    @BeforeEach
    void beforeEach() {
        provisioner = spy(EventHubsChannelProvisioner.class);
        producerProperties = new EventHubsProducerProperties();
        consumerProperties = new EventHubsConsumerProperties();
    }

    @Test
    void provisionProducerDestination() {
        ExtendedProducerProperties<EventHubsProducerProperties> extendedProperties =
            new ExtendedProducerProperties<>(producerProperties);

        provisioner.provisionProducerDestination("test", extendedProperties);
        verify(provisioner, times(1)).validateOrCreateForProducer("test");
    }

    @Test
    void provisionConsumerDestination() {
        ExtendedConsumerProperties<EventHubsConsumerProperties> extendedProperties =
            new ExtendedConsumerProperties<>(consumerProperties);

        provisioner.provisionConsumerDestination("test", "group", extendedProperties);
        verify(provisioner, times(1)).validateOrCreateForConsumer("test", "group");
    }
}
