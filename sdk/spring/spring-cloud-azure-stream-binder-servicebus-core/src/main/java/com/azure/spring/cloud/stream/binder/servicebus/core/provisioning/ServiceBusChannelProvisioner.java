// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.core.provisioning;

import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusProducerProperties;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.cloud.stream.provisioning.ProvisioningProvider;

/**
 *
 */
public class ServiceBusChannelProvisioner implements
        ProvisioningProvider<ExtendedConsumerProperties<ServiceBusConsumerProperties>,
                ExtendedProducerProperties<ServiceBusProducerProperties>> {

    @Override
    public ProducerDestination provisionProducerDestination(String name,
            ExtendedProducerProperties<ServiceBusProducerProperties> properties) throws ProvisioningException {
        validateOrCreateForProducer(name, properties.getExtension().getEntityType());
        return new ServiceBusProducerDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group,
            ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) throws ProvisioningException {
        validateOrCreateForConsumer(name, group, properties.getExtension().getEntityType());
        return new ServiceBusConsumerDestination(name);
    }

    /**
     * Validate or create for consumer.
     *
     * @param name the name
     * @param group the group
     * @param type the type
     */
    protected void validateOrCreateForConsumer(String name, String group, ServiceBusEntityType type) {
        // no-op
    }


    /**
     * Validate or create for producer.
     *
     * @param name the name
     * @param type the type
     */
    protected void validateOrCreateForProducer(String name, ServiceBusEntityType type) {
        // no-op
    }
}
