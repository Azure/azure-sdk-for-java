// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.properties.merger;

import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.service.implementation.core.PropertiesMerger;
import com.azure.spring.messaging.servicebus.core.properties.ProcessorProperties;

/**
 * A merger used to merge a {@link ProcessorProperties} with another properties of the same type. When a property is
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
        propertyMapper.from(source.getConnectionString()).to(target::setConnectionString);
        propertyMapper.from(source.getEntityName()).to(target::setEntityName);
        propertyMapper.from(source.getEntityType()).to(target::setEntityType);
        propertyMapper.from(source.getMaxConcurrentSessions()).to(target::setMaxConcurrentSessions);
        propertyMapper.from(source.getMaxConcurrentCalls()).to(target::setMaxConcurrentCalls);

        propertyMapper.from(source.getSessionEnabled()).to(target::setSessionEnabled);
        propertyMapper.from(source.getAutoComplete()).to(target::setAutoComplete);
        propertyMapper.from(source.getPrefetchCount()).to(target::setPrefetchCount);
        propertyMapper.from(source.getSubQueue()).to(target::setSubQueue);
        propertyMapper.from(source.getReceiveMode()).to(target::setReceiveMode);
        propertyMapper.from(source.getSubscriptionName()).to(target::setSubscriptionName);
        propertyMapper.from(source.getMaxAutoLockRenewDuration()).to(target::setMaxAutoLockRenewDuration);
    }
}
