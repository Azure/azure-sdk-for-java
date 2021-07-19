// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.servicebus.models.ServiceBusSubscription;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.spring.cloud.context.core.api.AzureResourceMetadata;
import com.azure.spring.core.util.Tuple;

/**
 * Resource manager for Service Bus topic subscription.
 */
public class ServiceBusTopicSubscriptionManager extends AzureManager<ServiceBusSubscription, Tuple<Topic, String>> {


    public ServiceBusTopicSubscriptionManager(AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceMetadata);
    }

    @Override
    String getResourceName(Tuple<Topic, String> key) {
        return key.getSecond();
    }

    @Override
    String getResourceType() {
        return ServiceBusSubscription.class.getSimpleName();
    }

    @Override
    public ServiceBusSubscription internalGet(Tuple<Topic, String> topicAndSubscriptionName) {
        try {
            return topicAndSubscriptionName.getFirst().subscriptions().getByName(topicAndSubscriptionName.getSecond());
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public ServiceBusSubscription internalCreate(Tuple<Topic, String> topicAndSubscriptionName) {
        return topicAndSubscriptionName.getFirst()
                                       .subscriptions()
                                       .define(topicAndSubscriptionName.getSecond())
                                       .create();
    }
}
