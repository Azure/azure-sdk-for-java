// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.properties;

import com.azure.spring.core.implementation.connectionstring.ServiceBusConnectionString;
import com.azure.spring.core.properties.AzureAmqpSdkProperties;
import com.azure.spring.service.servicebus.properties.ServiceBusCommonDescriptor;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;

/**
 * Common properties shared by Service Bus namespace, a producer, and a consumer.
 */
public class CommonProperties extends AzureAmqpSdkProperties implements ServiceBusCommonDescriptor {

    private String domainName = "servicebus.windows.net";
    private String namespace;
    private String connectionString;
    private String entityName;
    private ServiceBusEntityType entityType;

    private String extractFqdnFromConnectionString() {
        if (this.connectionString == null) {
            return null;
        }
        return new ServiceBusConnectionString(this.connectionString).getFullyQualifiedNamespace();
    }

    @Override
    public String getFQDN() {
        return this.namespace == null ? extractFqdnFromConnectionString() : (this.namespace + "." + domainName);
    }

    @Override
    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public ServiceBusEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(ServiceBusEntityType entityType) {
        this.entityType = entityType;
    }
}
