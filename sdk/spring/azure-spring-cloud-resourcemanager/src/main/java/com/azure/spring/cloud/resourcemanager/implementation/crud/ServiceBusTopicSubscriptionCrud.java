// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscription;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * Resource manager for Service Bus topic subscription.
 */
public class ServiceBusTopicSubscriptionCrud extends AbstractResourceCrud<ServiceBusSubscription,
    Tuple3<String, String, String>> {


    public ServiceBusTopicSubscriptionCrud(AzureResourceManager azureResourceManager,
                                           AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceManager, azureResourceMetadata);
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
            return new ServiceBusTopicCrud(this.resourceManager, this.resourceMetadata)
                .get(Tuples.of(subscriptionCoordinate.getT1(), subscriptionCoordinate.getT2()))
                .subscriptions()
                .getByName(subscriptionCoordinate.getT2());
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public ServiceBusSubscription internalCreate(Tuple3<String, String, String> subscriptionCoordinate) {
        return new ServiceBusTopicCrud(this.resourceManager, this.resourceMetadata)
            .getOrCreate(Tuples.of(subscriptionCoordinate.getT1(), subscriptionCoordinate.getT2()))
            .subscriptions()
            .define(subscriptionCoordinate.getT3())
            .create();
    }
}
