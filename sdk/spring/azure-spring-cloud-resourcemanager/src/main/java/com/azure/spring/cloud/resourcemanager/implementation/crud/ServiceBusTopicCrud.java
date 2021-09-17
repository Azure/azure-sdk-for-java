// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.spring.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.core.util.Tuple;

/**
 * Resource manager for Service Bus topic.
 */
public class ServiceBusTopicCrud extends AbstractResourceCrud<Topic, Tuple<String, String>> {

    public ServiceBusTopicCrud(AzureResourceManager azureResourceManager, AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceManager, azureResourceMetadata);
    }

    @Override
    String getResourceName(Tuple<String, String> key) {
        return key.getSecond();
    }

    @Override
    String getResourceType() {
        return Topic.class.getSimpleName();
    }

    @Override
    public Topic internalGet(Tuple<String, String> namespaceAndName) {
        try {
            return new ServiceBusNamespaceCrud(this.resourceManager, this.resourceMetadata)
                .get(namespaceAndName.getFirst())
                .topics()
                .getByName(namespaceAndName.getSecond());
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == 404) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public Topic internalCreate(Tuple<String, String> namespaceAndName) {
        return new ServiceBusNamespaceCrud(this.resourceManager, this.resourceMetadata)
            .getOrCreate(namespaceAndName.getFirst())
            .topics()
            .define(namespaceAndName.getSecond())
            .create();
    }
}
