// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.spring.cloud.context.core.config.AzureProperties;
import com.azure.spring.cloud.context.core.util.Tuple;
import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.servicebus.Queue;
import com.microsoft.azure.management.servicebus.ServiceBusNamespace;

public class ServiceBusQueueManager extends AzureManager<Queue, Tuple<ServiceBusNamespace, String>> {

    public ServiceBusQueueManager(Azure azure, AzureProperties azureProperties) {
        super(azure, azureProperties);
    }

    @Override
    String getResourceName(Tuple<ServiceBusNamespace, String> key) {
        return key.getSecond();
    }

    @Override
    String getResourceType() {
        return Queue.class.getSimpleName();
    }

    @Override
    public Queue internalGet(Tuple<ServiceBusNamespace, String> namespaceAndName) {
        return namespaceAndName.getFirst().queues().getByName(namespaceAndName.getSecond());
    }

    @Override
    public Queue internalCreate(Tuple<ServiceBusNamespace, String> namespaceAndName) {
        return namespaceAndName.getFirst().queues().define(namespaceAndName.getSecond()).create();
    }
}
