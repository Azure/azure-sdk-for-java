// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.provisioning;

import com.azure.spring.cloud.resourcemanager.provisioning.ServiceBusProvisioner;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusProducerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.cloud.stream.binder.servicebus.core.provisioning.ServiceBusConsumerDestination;
import com.azure.spring.cloud.stream.binder.servicebus.core.provisioning.ServiceBusProducerDestination;
import org.springframework.cloud.stream.binder.ExtendedConsumerProperties;
import org.springframework.cloud.stream.binder.ExtendedProducerProperties;
import org.springframework.cloud.stream.provisioning.ConsumerDestination;
import org.springframework.cloud.stream.provisioning.ProducerDestination;
import org.springframework.cloud.stream.provisioning.ProvisioningException;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

import static com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType.QUEUE;

/**
 *
 */
public class ServiceBusChannelResourceManagerProvisioner extends ServiceBusChannelProvisioner {

    private final String namespace;
    private final ServiceBusProvisioner serviceBusProvisioner;

    /**
     * Construct a {@link ServiceBusChannelResourceManagerProvisioner} with the specified namespace and {@link ServiceBusProvisioner}.
     *
     * @param namespace the namespace
     * @param serviceBusProvisioner the service Bus Provisioner
     */
    public ServiceBusChannelResourceManagerProvisioner(@NonNull String namespace,
                                                       @NonNull ServiceBusProvisioner serviceBusProvisioner) {
        Assert.hasText(namespace, "The namespace can't be null or empty");
        this.namespace = namespace;
        this.serviceBusProvisioner = serviceBusProvisioner;
    }

    @Override
    protected void validateOrCreateForConsumer(String name, String subscription, ServiceBusEntityType type) {
        if (QUEUE == type) {
            this.serviceBusProvisioner.provisionQueue(namespace, name);
        } else {
            this.serviceBusProvisioner.provisionSubscription(namespace, name, subscription);
        }
    }

    @Override
    protected void validateOrCreateForProducer(String name, ServiceBusEntityType type) {
        if (QUEUE == type) {
            this.serviceBusProvisioner.provisionQueue(namespace, name);
        } else {
            this.serviceBusProvisioner.provisionTopic(namespace, name);
        }
    }

    @Override
    public ProducerDestination provisionProducerDestination(String name,
                                                            ExtendedProducerProperties<ServiceBusProducerProperties> extendedProducerProperties) throws ProvisioningException {
        ServiceBusProducerProperties producerProperties = extendedProducerProperties.getExtension();
        if (QUEUE == producerProperties.getEntityType()) {
            this.serviceBusProvisioner.provisionQueue(namespace, name, producerProperties);
        } else {
            this.serviceBusProvisioner.provisionTopic(namespace, name, producerProperties);
        }
        return new ServiceBusProducerDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group,
                                                            ExtendedConsumerProperties<ServiceBusConsumerProperties> extendedConsumerProperties) throws ProvisioningException {
        ServiceBusConsumerProperties consumerProperties = extendedConsumerProperties.getExtension();
        if (QUEUE == consumerProperties.getEntityType()) {
            ServiceBusProducerProperties producerProperties = new ServiceBusProducerProperties();
            producerProperties.setEntityType(consumerProperties.getEntityType());
            producerProperties.setEntityName(consumerProperties.getEntityName());
            producerProperties.setDefaultMessageTimeToLive(consumerProperties.getDefaultMessageTimeToLive());
            producerProperties.setMaxSizeInMegabytes(consumerProperties.getMaxSizeInMegabytes());
            this.serviceBusProvisioner.provisionQueue(namespace, name, producerProperties);
        } else {
            this.serviceBusProvisioner.provisionSubscription(namespace, name, group, consumerProperties);
        }
        return new ServiceBusConsumerDestination(name);
    }

}
