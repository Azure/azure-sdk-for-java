// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscription;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.core.util.Triple;
import com.azure.spring.core.util.Tuple;

/**
 * Resource manager for Service Bus topic subscription.
 */
public class ServiceBusTopicSubscriptionCrud extends AbstractResourceCrud<ServiceBusSubscription,
                                                                             Triple<String, String, String>> {


    public ServiceBusTopicSubscriptionCrud(AzureResourceManager azureResourceManager,
                                           AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceManager, azureResourceMetadata);
    }
    @Override
    String getResourceName(Triple<String, String, String> key) {
        return key.getThird();
    }

    @Override
    String getResourceType() {
        return ServiceBusSubscription.class.getSimpleName();
    }

    @Override
    public ServiceBusSubscription internalGet(Triple<String, String, String> subscriptionCoordinate) {
        try {
            return new ServiceBusTopicCrud(this.resourceManager, this.resourceMetadata)
                .get(Tuple.of(subscriptionCoordinate.getFirst(), subscriptionCoordinate.getSecond()))
                .subscriptions()
                .getByName(subscriptionCoordinate.getSecond());
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public ServiceBusSubscription internalCreate(Triple<String, String, String> subscriptionCoordinate) {
        return new ServiceBusTopicCrud(this.resourceManager, this.resourceMetadata)
            .getOrCreate(Tuple.of(subscriptionCoordinate.getFirst(), subscriptionCoordinate.getSecond()))
            .subscriptions()
            .define(subscriptionCoordinate.getThird())
            .create();
    }
}
