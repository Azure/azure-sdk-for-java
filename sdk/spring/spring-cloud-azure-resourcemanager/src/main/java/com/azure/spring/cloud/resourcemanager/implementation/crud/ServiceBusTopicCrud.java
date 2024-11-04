// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.crud;

import com.azure.core.management.exception.ManagementException;
import com.azure.resourcemanager.AzureResourceManager;
import com.azure.resourcemanager.servicebus.models.ServiceBusNamespace;
import com.azure.resourcemanager.servicebus.models.Topic;
import com.azure.spring.cloud.core.properties.resource.AzureResourceMetadata;
import com.azure.spring.cloud.resourcemanager.implementation.provisioning.properties.ServiceBusTopicProperties;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import reactor.util.function.Tuple2;

/**
 * Resource manager for Service Bus topic.
 */
public class ServiceBusTopicCrud extends AbstractResourceCrud<Topic, Tuple2<String, String>, ServiceBusTopicProperties> {

    public ServiceBusTopicCrud(AzureResourceManager azureResourceManager, AzureResourceMetadata azureResourceMetadata) {
        super(azureResourceManager, azureResourceMetadata);
    }

    @Override
    String getResourceName(Tuple2<String, String> key) {
        return key.getT2();
    }

    @Override
    String getResourceType() {
        return Topic.class.getSimpleName();
    }

    @Override
    public Topic internalGet(Tuple2<String, String> namespaceAndName) {
        try {
            ServiceBusNamespace serviceBusNamespace = new ServiceBusNamespaceCrud(this.resourceManager,
                this.resourceMetadata)
                .get(namespaceAndName.getT1());
            Assert.notNull(serviceBusNamespace, "The Service Bus namespace should exist first.");
            return serviceBusNamespace
                .topics()
                .getByName(namespaceAndName.getT2());
        } catch (ManagementException e) {
            if (e.getResponse().getStatusCode() == RESOURCE_NOT_FOUND) {
                return null;
            } else {
                throw e;
            }
        }
    }

    @Override
    public Topic internalCreate(Tuple2<String, String> namespaceAndName) {
        return new ServiceBusNamespaceCrud(this.resourceManager, this.resourceMetadata)
            .getOrCreate(namespaceAndName.getT1())
            .topics()
            .define(namespaceAndName.getT2())
            .create();
    }

    @Override
    public Topic internalCreate(Tuple2<String, String> namespaceAndName, @Nullable ServiceBusTopicProperties topicProperties) {
        Topic.DefinitionStages.Blank blank = new ServiceBusNamespaceCrud(this.resourceManager, this.resourceMetadata)
            .getOrCreate(namespaceAndName.getT1())
            .topics()
            .define(namespaceAndName.getT2());
        if (topicProperties != null) {
            if (topicProperties.getMaxSizeInMegabytes() != null) {
                blank.withSizeInMB(topicProperties.getMaxSizeInMegabytes());
            }
            if (topicProperties.getDefaultMessageTimeToLive() != null) {
                blank.withDefaultMessageTTL(topicProperties.getDefaultMessageTimeToLive());
            }
        }
        return blank.create();
    }
}
