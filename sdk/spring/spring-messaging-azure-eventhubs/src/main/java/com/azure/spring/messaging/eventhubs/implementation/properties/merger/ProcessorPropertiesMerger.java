// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.implementation.properties.merger;

import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.messaging.eventhubs.core.properties.ProcessorProperties;
import com.azure.spring.cloud.service.implementation.core.PropertiesMerger;

/**
 * A merger used to merge a {@link ProcessorProperties} with another properties of same type. When a property is
 * set in the first, it will be kept. For those properties not set in the first, it will use the value in the second.
 */
public class ProcessorPropertiesMerger implements PropertiesMerger<ProcessorProperties, ProcessorProperties> {

    @Override
    public ProcessorProperties merge(ProcessorProperties first, ProcessorProperties second) {
        ProcessorProperties properties = new ProcessorProperties();
        if (first == null && second == null) {
            return properties;
        }
        if (first == null) {
            first = new ProcessorProperties();
        }
        if (second == null) {
            second = new ProcessorProperties();
        }


        AzurePropertiesUtils.mergeAzureCommonProperties(second, first, properties);

        copyProcessorPropertiesIfNotNull(second, properties);
        // If a same property appears in both two objects, the value from the first will take precedence.
        copyProcessorPropertiesIfNotNull(first, properties);

        return properties;
    }

    public static void copyProcessorPropertiesIfNotNull(ProcessorProperties source, ProcessorProperties target) {
        PropertyMapper propertyMapper = new PropertyMapper();

        propertyMapper.from(source.getDomainName()).to(target::setDomainName);
        propertyMapper.from(source.getNamespace()).to(target::setNamespace);
        propertyMapper.from(source.getEventHubName()).to(target::setEventHubName);
        propertyMapper.from(source.getConnectionString()).to(target::setConnectionString);
        propertyMapper.from(source.getCustomEndpointAddress()).to(target::setCustomEndpointAddress);
        propertyMapper.from(source.getPrefetchCount()).to(target::setPrefetchCount);
        propertyMapper.from(source.getConsumerGroup()).to(target::setConsumerGroup);

        propertyMapper.from(source.getTrackLastEnqueuedEventProperties()).to(target::setTrackLastEnqueuedEventProperties);
        propertyMapper.from(source.getInitialPartitionEventPosition()).to(m -> target.getInitialPartitionEventPosition().putAll(m));
        propertyMapper.from(source.getBatch().getMaxSize()).to(target.getBatch()::setMaxSize);
        propertyMapper.from(source.getBatch().getMaxWaitTime()).to(target.getBatch()::setMaxWaitTime);
        propertyMapper.from(source.getLoadBalancing().getPartitionOwnershipExpirationInterval()).to(target.getLoadBalancing()::setPartitionOwnershipExpirationInterval);
        propertyMapper.from(source.getLoadBalancing().getStrategy()).to(target.getLoadBalancing()::setStrategy);
        propertyMapper.from(source.getLoadBalancing().getUpdateInterval()).to(target.getLoadBalancing()::setUpdateInterval);
    }
}
