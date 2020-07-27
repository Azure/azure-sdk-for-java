/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.impl;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.servicebus.ServiceBusSubscription;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;

public class ServiceBusTopicSubscriptionManager extends AzureManager<ServiceBusSubscription, Tuple<Topic, String>> {

    public ServiceBusTopicSubscriptionManager(Azure azure, AzureProperties azureProperties) {
        super(azure, azureProperties);
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
        } catch (NullPointerException ignore) {
            // azure management api has no way to determine whether an service bus topic subscription exists
            // Workaround for this is by catching NPE
            return null;
        }
    }

    @Override
    public ServiceBusSubscription internalCreate(Tuple<Topic, String> topicAndSubscriptionName) {
        return topicAndSubscriptionName.getFirst().subscriptions().define(topicAndSubscriptionName.getSecond())
                                       .create();
    }
}
