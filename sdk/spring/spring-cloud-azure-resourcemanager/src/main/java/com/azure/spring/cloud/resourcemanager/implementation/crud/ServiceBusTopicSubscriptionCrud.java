// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscription;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusProducerProperties;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

/**
 * Resource manager for Service Bus topic subscription.
 */
public class ServiceBusTopicSubscriptionCrud extends AbstractResourceCrud<ServiceBusSubscription,
    Tuple4<String, String, String, ServiceBusConsumerProperties>> {

    private ServiceBusTopicCrud serviceBusTopicCrud;
    public ServiceBusTopicSubscriptionCrud(AzureResourceManager azureResourceManager,
                                           AzureResourceMetadata azureResourceMetadata) {
        this(azureResourceManager, azureResourceMetadata,
            new ServiceBusTopicCrud(azureResourceManager, azureResourceMetadata));
    }

    ServiceBusTopicSubscriptionCrud(AzureResourceManager azureResourceManager,
                                    AzureResourceMetadata azureResourceMetadata,
                                    ServiceBusTopicCrud serviceBusTopicCrud) {
        super(azureResourceManager, azureResourceMetadata);
        this.serviceBusTopicCrud = serviceBusTopicCrud;
    }

    @Override
    String getResourceName(Tuple4<String, String, String, ServiceBusConsumerProperties> key) {
        return key.getT3();
    }

    @Override
    String getResourceType() {
        return ServiceBusSubscription.class.getSimpleName();
    }

    @Override
    public ServiceBusSubscription internalGet(Tuple4<String, String, String, ServiceBusConsumerProperties> subscriptionCoordinate) {
        try {
            // todo: Check if this is correct
            ServiceBusProducerProperties producerProperties = getServiceBusProducerProperties(subscriptionCoordinate.getT4());
            Topic topic = this.serviceBusTopicCrud
                .get(Tuples.of(subscriptionCoordinate.getT1(), subscriptionCoordinate.getT2(), producerProperties));
            return topic == null ? null : topic
                .subscriptions()
                .getByName(subscriptionCoordinate.getT3());
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == RESOURCE_NOT_FOUND) {
                return null;
            } else {
                throw e;
            }
        }
    }

    private ServiceBusProducerProperties getServiceBusProducerProperties(ServiceBusConsumerProperties consumerProperties) {
        ServiceBusProducerProperties producerProperties = new ServiceBusProducerProperties();
        producerProperties.setEntityType(consumerProperties.getEntityType());
        producerProperties.setEntityName(consumerProperties.getEntityName());
        producerProperties.setMaxSizeInMegabytes(consumerProperties.getMaxSizeInMegabytes());
        producerProperties.setDefaultMessageTimeToLive(consumerProperties.getDefaultMessageTimeToLive());
        return producerProperties;
    }

    @Override
    public ServiceBusSubscription internalCreate(Tuple4<String, String, String, ServiceBusConsumerProperties> subscriptionCoordinate) {
        ServiceBusProducerProperties producerProperties = getServiceBusProducerProperties(subscriptionCoordinate.getT4());
        return this.serviceBusTopicCrud
            .getOrCreate(Tuples.of(subscriptionCoordinate.getT1(), subscriptionCoordinate.getT2(), producerProperties))
            .subscriptions()
            .define(subscriptionCoordinate.getT3())
            .create();
    }
}
