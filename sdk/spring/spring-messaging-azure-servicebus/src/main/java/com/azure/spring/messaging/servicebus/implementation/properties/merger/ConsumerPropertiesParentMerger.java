// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.implementation.properties.merger;

import com.azure.spring.cloud.core.implementation.properties.PropertyMapper;
import com.azure.spring.cloud.core.implementation.util.AzurePropertiesUtils;
import com.azure.spring.cloud.service.implementation.core.PropertiesMerger;
import com.azure.spring.messaging.servicebus.core.properties.ConsumerProperties;
import com.azure.spring.messaging.servicebus.core.properties.NamespaceProperties;

/**
 * A merger used to merge a {@link ConsumerProperties} with its parent {@link NamespaceProperties}. When a property is
 * set in the child, it will be kept. For those properties not set in the child, it will use the value in the parent.
 * @since 5.22.0
 */
public class ConsumerPropertiesParentMerger implements PropertiesMerger<ConsumerProperties, NamespaceProperties> {

    @Override
    public ConsumerProperties merge(ConsumerProperties child, NamespaceProperties parent) {
        ConsumerProperties properties = new ConsumerProperties();
        if (child == null && parent == null) {
            return properties;
        }

        if (child == null) {
            child = new ConsumerProperties();
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
        propertyMapper.from(parent.getCustomEndpointAddress()).to(properties::setCustomEndpointAddress);

        // If a same property appears in both two objects, the value from the child will take precedence.
        copyConsumerPropertiesIfNotNull(child, properties);

        return properties;
    }

    public static void copyConsumerPropertiesIfNotNull(ConsumerProperties source, ConsumerProperties target) {
        PropertyMapper propertyMapper = new PropertyMapper();

        propertyMapper.from(source.getDomainName()).to(target::setDomainName);
        propertyMapper.from(source.getNamespace()).to(target::setNamespace);
        propertyMapper.from(source.getConnectionString()).to(target::setConnectionString);
        propertyMapper.from(source.getEntityName()).to(target::setEntityName);
        propertyMapper.from(source.getEntityType()).to(target::setEntityType);
        propertyMapper.from(source.getCustomEndpointAddress()).to(target::setCustomEndpointAddress);

        propertyMapper.from(source.getSessionEnabled()).to(target::setSessionEnabled);
        propertyMapper.from(source.getAutoComplete()).to(target::setAutoComplete);
        propertyMapper.from(source.getPrefetchCount()).to(target::setPrefetchCount);
        propertyMapper.from(source.getSubQueue()).to(target::setSubQueue);
        propertyMapper.from(source.getReceiveMode()).to(target::setReceiveMode);
        propertyMapper.from(source.getSubscriptionName()).to(target::setSubscriptionName);
        propertyMapper.from(source.getMaxAutoLockRenewDuration()).to(target::setMaxAutoLockRenewDuration);
    }
}
