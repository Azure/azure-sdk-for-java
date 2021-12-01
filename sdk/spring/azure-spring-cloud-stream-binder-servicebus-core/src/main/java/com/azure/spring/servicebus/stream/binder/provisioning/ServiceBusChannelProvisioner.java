// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.stream.binder.provisioning;

import com.azure.spring.servicebus.stream.binder.properties.ServiceBusConsumerProperties;
import com.azure.spring.servicebus.stream.binder.properties.ServiceBusProducerProperties;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;

/**
 * @author Warren Zhu
 */
public class ServiceBusChannelProvisioner implements
        ProvisioningProvider<ExtendedConsumerProperties<ServiceBusConsumerProperties>,
                ExtendedProducerProperties<ServiceBusProducerProperties>> {

    /**
     *
     * @param name The name.
     * @param properties The ExtendedProducerProperties.
     * @return The ProducerDestination.
     * @throws ProvisioningException The ProvisioningException.
     */
    @Override
    public ProducerDestination provisionProducerDestination(String name,
            ExtendedProducerProperties<ServiceBusProducerProperties> properties) throws ProvisioningException {
        validateOrCreateForProducer(name);
        return new ServiceBusProducerDestination(name);
    }

    /**
     *
     * @param name The name.
     * @param group The group.
     * @param properties The ExtendedConsumerProperties.
     * @return The ConsumerDestination.
     * @throws ProvisioningException The ProvisioningException.
     */
    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group,
            ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) throws ProvisioningException {
        validateOrCreateForConsumer(name, group);
        return new ServiceBusConsumerDestination(name);
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
