/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.context.core.impl;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.servicebus.ServiceBusNamespace;
import com.microsoft.azure.management.servicebus.Topic;
import com.microsoft.azure.spring.cloud.context.core.config.AzureProperties;
import com.microsoft.azure.spring.cloud.context.core.util.Tuple;

public class ServiceBusTopicManager extends AzureManager<Topic, Tuple<ServiceBusNamespace, String>> {

    public ServiceBusTopicManager(Azure azure, AzureProperties azureProperties) {
        super(azure, azureProperties);
    }

    @Override
    String getResourceName(Tuple<ServiceBusNamespace, String> key) {
        return key.getSecond();
    }

    @Override
    String getResourceType() {
        return Topic.class.getSimpleName();
    }

    @Override
    public Topic internalGet(Tuple<ServiceBusNamespace, String> namespaceAndName) {
        return namespaceAndName.getFirst().topics().getByName(namespaceAndName.getSecond());
    }

    @Override
    public Topic internalCreate(Tuple<ServiceBusNamespace, String> namespaceAndName) {
        return namespaceAndName.getFirst().topics().define(namespaceAndName.getSecond()).create();
    }
}
