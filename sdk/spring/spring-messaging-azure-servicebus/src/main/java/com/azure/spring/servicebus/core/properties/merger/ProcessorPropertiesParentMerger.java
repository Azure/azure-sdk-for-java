// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.properties.merger;

import com.azure.spring.core.util.AzurePropertiesUtils;
import com.azure.spring.core.properties.PropertyMapper;
import com.azure.spring.service.implementation.core.ParentMerger;
import com.azure.spring.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.servicebus.core.properties.ProcessorProperties;

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
        propertyMapper.from(parent.getConnectionString()).to(properties::setConnectionString);
        propertyMapper.from(parent.getEntityName()).to(properties::setEntityName);
        propertyMapper.from(parent.getEntityType()).to(properties::setEntityType);

        propertyMapper.from(child.getDomainName()).to(properties::setDomainName);
        propertyMapper.from(child.getNamespace()).to(properties::setNamespace);
        propertyMapper.from(child.getConnectionString()).to(properties::setConnectionString);
        propertyMapper.from(child.getEntityName()).to(properties::setEntityName);
        propertyMapper.from(child.getEntityType()).to(properties::setEntityType);
        propertyMapper.from(child.getMaxConcurrentSessions()).to(properties::setMaxConcurrentSessions);
        propertyMapper.from(child.getMaxConcurrentCalls()).to(properties::setMaxConcurrentCalls);

        propertyMapper.from(child.getSessionEnabled()).to(properties::setSessionAware);
        propertyMapper.from(child.getAutoComplete()).to(properties::setAutoComplete);
        propertyMapper.from(child.getPrefetchCount()).to(properties::setPrefetchCount);
        propertyMapper.from(child.getSubQueue()).to(properties::setSubQueue);
        propertyMapper.from(child.getReceiveMode()).to(properties::setReceiveMode);
        propertyMapper.from(child.getSubscriptionName()).to(properties::setSubscriptionName);
        propertyMapper.from(child.getMaxAutoLockRenewDuration()).to(properties::setMaxAutoLockRenewDuration);

        return properties;
    }
}
