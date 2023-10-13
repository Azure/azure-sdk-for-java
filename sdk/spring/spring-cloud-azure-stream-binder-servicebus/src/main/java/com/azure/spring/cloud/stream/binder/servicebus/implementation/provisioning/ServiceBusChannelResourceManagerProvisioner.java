// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.stream.binder.servicebus.implementation.provisioning;

import com.azure.spring.cloud.resourcemanager.implementation.provisioning.ServiceBusProvisioner;
import com.azure.spring.cloud.resourcemanager.implementation.provisioning.properties.ServiceBusEntityProperties;
import com.azure.spring.cloud.resourcemanager.implementation.provisioning.properties.ServiceBusQueueProperties;
import com.azure.spring.cloud.resourcemanager.implementation.provisioning.properties.ServiceBusTopicProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.implementation.provisioning.ServiceBusChannelProvisioner;
import com.azure.spring.cloud.stream.binder.servicebus.core.implementation.provisioning.ServiceBusConsumerDestination;
import com.azure.spring.cloud.stream.binder.servicebus.core.implementation.provisioning.ServiceBusProducerDestination;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusEntityOptionsProvider;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusProducerProperties;
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
    public ProducerDestination provisionProducerDestination(String name,
                                                            ExtendedProducerProperties<ServiceBusProducerProperties> extendedProducerProperties) throws ProvisioningException {
        ServiceBusProducerProperties producerProperties = extendedProducerProperties.getExtension();
        Assert.notNull(producerProperties.getEntityType(), "The EntityType of the producer can't be null.");

        if (QUEUE == producerProperties.getEntityType()) {
            this.serviceBusProvisioner.provisionQueue(namespace, name, buildEntityProperties(producerProperties, ServiceBusQueueProperties.class));
        } else {
            this.serviceBusProvisioner.provisionTopic(namespace, name, buildEntityProperties(producerProperties, ServiceBusTopicProperties.class));
        }

        return new ServiceBusProducerDestination(name);
    }

    @Override
    public ConsumerDestination provisionConsumerDestination(String name, String group,
                                                            ExtendedConsumerProperties<ServiceBusConsumerProperties> extendedConsumerProperties) throws ProvisioningException {
        ServiceBusConsumerProperties consumerProperties = extendedConsumerProperties.getExtension();
        Assert.notNull(consumerProperties.getEntityType(), "The EntityType of the consumer can't be null.");

        if (QUEUE == consumerProperties.getEntityType()) {
            this.serviceBusProvisioner.provisionQueue(namespace, name, buildEntityProperties(consumerProperties, ServiceBusQueueProperties.class));
        } else {
            this.serviceBusProvisioner.provisionSubscription(namespace, name, group, buildEntityProperties(consumerProperties, ServiceBusTopicProperties.class));
        }

        return new ServiceBusConsumerDestination(name);
    }

    private <T extends ServiceBusEntityProperties> T buildEntityProperties(
        ServiceBusEntityOptionsProvider entityOptionsProvider,
        Class<T> clazz) {
        T entityProperties = null;
        try {
            entityProperties = clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        entityProperties.setDefaultMessageTimeToLive(entityOptionsProvider.getDefaultMessageTimeToLive());
        entityProperties.setMaxSizeInMegabytes(entityOptionsProvider.getMaxSizeInMegabytes());
        return entityProperties;
    }
}
