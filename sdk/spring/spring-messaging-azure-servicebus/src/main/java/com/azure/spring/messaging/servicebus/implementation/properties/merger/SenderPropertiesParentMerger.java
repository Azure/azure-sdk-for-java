// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.properties.merger;

import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.service.implementation.core.PropertiesMerger;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;
import com.azure.spring.messaging.servicebus.core.properties.ProducerProperties;

/**
 * A merger used to merge a {@link ProducerProperties} with its parent {@link NamespaceProperties}. When a property is
 *  * set in the child, it will be kept. For those properties not set in the child, it will use the value in the
 *  parent.
 */
public class SenderPropertiesParentMerger implements PropertiesMerger<ProducerProperties, NamespaceProperties> {

    @Override
    public ProducerProperties merge(ProducerProperties child, NamespaceProperties parent) {
        ProducerProperties properties = new ProducerProperties();
        if (child == null && parent == null) {
            return properties;
        }
        if (child == null) {
            child = new ProducerProperties();
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

        return properties;

    }
}
