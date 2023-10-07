// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusProducerProperties;
import org.springframework.util.Assert;
import reactor.util.function.Tuple3;

/**
 * Resource manager for Service Bus topic.
 */
public class ServiceBusTopicCrud extends AbstractResourceCrud<Topic, Tuple3<String, String, ServiceBusProducerProperties>> {

    public ServiceBusTopicCrud(AzureResourceManager azureResourceManager, AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceManager, azureResourceMetadata);
    }

    @Override
    String getResourceName(Tuple3<String, String, ServiceBusProducerProperties> key) {
        return key.getT2();
    }

    @Override
    String getResourceType() {
        return Topic.class.getSimpleName();
    }

    @Override
    public Topic internalGet(Tuple3<String, String, ServiceBusProducerProperties> creationTuple) {
        try {
            ServiceBusNamespace serviceBusNamespace = new ServiceBusNamespaceCrud(this.resourceManager,
                this.resourceMetadata)
                .get(creationTuple.getT1());
            Assert.notNull(serviceBusNamespace, "The Service Bus namespace should exist first.");
            return serviceBusNamespace
                .topics()
                .getByName(creationTuple.getT2());
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == RESOURCE_NOT_FOUND) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public Topic internalCreate(Tuple3<String, String, ServiceBusProducerProperties> creationTuple) {
        ServiceBusProducerProperties producerProperties = creationTuple.getT3();
        Topic.DefinitionStages.Blank blank = new ServiceBusNamespaceCrud(this.resourceManager, this.resourceMetadata)
            .getOrCreate(creationTuple.getT1())
            .topics()
            .define(creationTuple.getT2());
        if (producerProperties.getMaxSizeInMegabytes() != null) {
            blank.withSizeInMB(producerProperties.getMaxSizeInMegabytes());
        }
        if (producerProperties.getDefaultMessageTimeToLive() != null) {
            blank.withDefaultMessageTTL(producerProperties.getDefaultMessageTimeToLive());
        }
        return blank.create();
    }
}
