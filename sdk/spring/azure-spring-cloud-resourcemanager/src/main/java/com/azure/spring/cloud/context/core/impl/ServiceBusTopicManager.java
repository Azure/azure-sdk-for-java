// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.spring.cloud.context.core.api.AzureResourceMetadata;
import com.azure.spring.core.util.Tuple;

/**
 * Resource manager for Service Bus topic.
 */
public class ServiceBusTopicManager extends AzureManager<Topic, Tuple<ServiceBusNamespace, String>> {

    public ServiceBusTopicManager(AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceMetadata);
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
        try {
            return namespaceAndName.getFirst().topics().getByName(namespaceAndName.getSecond());
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public Topic internalCreate(Tuple<ServiceBusNamespace, String> namespaceAndName) {
        return namespaceAndName.getFirst().topics().define(namespaceAndName.getSecond()).create();
    }
}
