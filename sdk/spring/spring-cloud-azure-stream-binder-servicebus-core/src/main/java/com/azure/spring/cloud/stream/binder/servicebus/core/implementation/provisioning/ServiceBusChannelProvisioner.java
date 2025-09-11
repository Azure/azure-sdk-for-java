// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.core.implementation.provisioning;

import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusProducerProperties;
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
        return new ServiceBusProducerDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group,
            ExtendedConsumerProperties<ServiceBusConsumerProperties> properties) throws ProvisioningException {
        return new ServiceBusConsumerDestination(name);
    }


}
