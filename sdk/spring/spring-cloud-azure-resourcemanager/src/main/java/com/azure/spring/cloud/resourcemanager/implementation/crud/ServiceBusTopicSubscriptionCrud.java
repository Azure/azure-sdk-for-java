// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscription;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.resourcemanager.implementation.provisioning.properties.ServiceBusTopicProperties;
import org.springframework.lang.Nullable;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * Resource manager for Service Bus topic subscription.
 */
public class ServiceBusTopicSubscriptionCrud extends AbstractResourceCrud<ServiceBusSubscription,
    Tuple3<String, String, String>, ServiceBusTopicProperties> {

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
    String getResourceName(Tuple3<String, String, String> key) {
        return key.getT3();
    }

    @Override
    String getResourceType() {
        return ServiceBusSubscription.class.getSimpleName();
    }

    @Override
    public ServiceBusSubscription internalGet(Tuple3<String, String, String> subscriptionCoordinate) {
        try {
            Topic topic = this.serviceBusTopicCrud
                .get(Tuples.of(subscriptionCoordinate.getT1(), subscriptionCoordinate.getT2()));
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

    @Override
    public ServiceBusSubscription internalCreate(Tuple3<String, String, String> subscriptionCoordinate) {
        return this.serviceBusTopicCrud
            .getOrCreate(Tuples.of(subscriptionCoordinate.getT1(), subscriptionCoordinate.getT2()))
            .subscriptions()
            .define(subscriptionCoordinate.getT3())
            .create();
    }

    @Override
    public ServiceBusSubscription internalCreate(Tuple3<String, String, String> subscriptionCoordinate,
                                                 @Nullable ServiceBusTopicProperties topicProperties) {
        return this.serviceBusTopicCrud
            .getOrCreate(Tuples.of(subscriptionCoordinate.getT1(), subscriptionCoordinate.getT2()), topicProperties)
            .subscriptions()
            .define(subscriptionCoordinate.getT3())
            .create();
    }
}
