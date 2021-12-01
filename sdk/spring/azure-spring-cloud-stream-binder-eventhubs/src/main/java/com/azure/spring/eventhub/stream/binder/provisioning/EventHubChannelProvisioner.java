// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder.provisioning;

import com.azure.spring.eventhub.stream.binder.properties.EventHubProducerProperties;
import com.azure.spring.eventhub.stream.binder.properties.EventHubConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;

/**
 * @author Warren Zhu
 */
public class EventHubChannelProvisioner implements
        ProvisioningProvider<ExtendedConsumerProperties<EventHubConsumerProperties>,
                ExtendedProducerProperties<EventHubProducerProperties>> {

    /**
     *
     * @param name The name.
     * @param properties The ExtendedProducerProperties.
     * @return The ProducerDestination
     * @throws ProvisioningException The ProvisioningException.
     */
    @Override
    public ProducerDestination provisionProducerDestination(String name,
            ExtendedProducerProperties<EventHubProducerProperties> properties) throws ProvisioningException {
        validateOrCreateForProducer(name);
        return new EventHubProducerDestination(name);
    }

    /**
     *
     * @param name The name.
     * @param group The group.
     * @param properties The ExtendedConsumerProperties.
     * @return ConsumerDestination
     * @throws ProvisioningException The ProvisioningException.
     */
    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group,
            ExtendedConsumerProperties<EventHubConsumerProperties> properties) throws ProvisioningException {
        validateOrCreateForConsumer(name, group);
        return new EventHubConsumerDestination(name);
    }

    /**
     *
     * @param name The name.
     * @param group The group.
     */
    protected void validateOrCreateForConsumer(String name, String group) {
        // no-op
    }

    /**
     *
     * @param name The name.
     */
    protected void validateOrCreateForProducer(String name) {
        // no-op
    }
}
