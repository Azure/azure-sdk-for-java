// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.eventhubs.provisioning;

import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsProducerProperties;
import com.azure.spring.cloud.stream.binder.eventhubs.properties.EventHubsConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;

/**
 *
 */
public class EventHubsChannelProvisioner implements
        ProvisioningProvider<ExtendedConsumerProperties<EventHubsConsumerProperties>,
                ExtendedProducerProperties<EventHubsProducerProperties>> {

    @Override
    public ProducerDestination provisionProducerDestination(String name,
            ExtendedProducerProperties<EventHubsProducerProperties> properties) throws ProvisioningException {
        validateOrCreateForProducer(name);
        return new EventHubsProducerDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group,
            ExtendedConsumerProperties<EventHubsConsumerProperties> properties) throws ProvisioningException {
        validateOrCreateForConsumer(name, group);
        return new EventHubsConsumerDestination(name);
    }

    protected void validateOrCreateForConsumer(String name, String group) {
        // no-op
    }

    protected void validateOrCreateForProducer(String name) {
        // no-op
    }
}
