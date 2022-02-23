// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.implementation.merger;

import com.azure.spring.core.properties.PropertyMapper;
import com.azure.spring.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.eventhubs.core.properties.NamespaceProperties;
import com.azure.spring.eventhubs.core.properties.ProcessorProperties;
import com.azure.spring.service.implementation.core.ParentMerger;

/**
 * A merger used to merge a {@link ProcessorProperties} with its parent {@link NamespaceProperties}. When a property is
 * set in the child, it will be kept. For those properties not set in the child, it will use the value in the parent.
 */
public class ProcessorPropertiesParentMerger implements ParentMerger<ProcessorProperties, NamespaceProperties> {

    @Override
    public ProcessorProperties mergeParent(ProcessorProperties child, NamespaceProperties parent) {
        ProcessorProperties properties = new ProcessorProperties();
        if (child == null && parent == null) {
            return properties;
        }
        if (child == null) {
            child = new ProcessorProperties();
        }
        if (parent == null) {
            parent = new NamespaceProperties();
        }

        PropertyMapper propertyMapper = new PropertyMapper();

        AzurePropertiesUtils.mergeAzureCommonProperties(parent, child, properties);

        propertyMapper.from(parent.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(parent.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(parent.getEventHubName()).to(properties::setEventHubName);
        propertyMapper.from(parent.getConnectionString()).to(properties::setConnectionString);
        propertyMapper.from(parent.getCustomEndpointAddress()).to(properties::setCustomEndpointAddress);

        propertyMapper.from(child.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(child.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(child.getEventHubName()).to(properties::setEventHubName);
        propertyMapper.from(child.getConnectionString()).to(properties::setConnectionString);
        propertyMapper.from(child.getCustomEndpointAddress()).to(properties::setCustomEndpointAddress);
        propertyMapper.from(child.getPrefetchCount()).to(properties::setPrefetchCount);
        propertyMapper.from(child.getConsumerGroup()).to(properties::setConsumerGroup);

        propertyMapper.from(child.getTrackLastEnqueuedEventProperties()).to(properties::setTrackLastEnqueuedEventProperties);
        propertyMapper.from(child.getInitialPartitionEventPosition()).to(m -> properties.getInitialPartitionEventPosition().putAll(m));
        propertyMapper.from(child.getBatch().getMaxSize()).to(properties.getBatch()::setMaxSize);
        propertyMapper.from(child.getBatch().getMaxWaitTime()).to(properties.getBatch()::setMaxWaitTime);
        propertyMapper.from(child.getLoadBalancing().getPartitionOwnershipExpirationInterval()).to(properties.getLoadBalancing()::setPartitionOwnershipExpirationInterval);
        propertyMapper.from(child.getLoadBalancing().getStrategy()).to(properties.getLoadBalancing()::setStrategy);
        propertyMapper.from(child.getLoadBalancing().getUpdateInterval()).to(properties.getLoadBalancing()::setUpdateInterval);

        return properties;
    }
}
