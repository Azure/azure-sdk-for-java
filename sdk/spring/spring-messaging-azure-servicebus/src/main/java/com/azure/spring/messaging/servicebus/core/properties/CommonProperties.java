// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core.properties;

import com.azure.spring.cloud.core.implementation.connectionstring.ServiceBusConnectionString;
import com.azure.spring.cloud.core.implementation.properties.AzureAmqpSdkProperties;
import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusClientCommonProperties;
import com.azure.spring.cloud.service.servicebus.properties.ServiceBusEntityType;

/**
 * Common properties shared by Service Bus namespace, a producer, and a consumer.
 */
public class CommonProperties extends AzureAmqpSdkProperties implements ServiceBusClientCommonProperties {

    private String domainName = "servicebus.windows.net";

    /**
     * The namespace of a service bus, which is the prefix of the FQDN. A FQDN should be composed of &lt;NamespaceName&gt;.&lt;DomainName&gt;
     */
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
    public String getFullyQualifiedNamespace() {
        return this.namespace == null ? extractFqdnFromConnectionString() : (this.namespace + "." + domainName);
    }

    @Override
    public String getDomainName() {
        return domainName;
    }

    /**
     * Set the domain name.
     * @param domainName the domain name.
     */
    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    /**
     * Set the namespace.
     * @param namespace the namespace.
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    @Override
    public String getConnectionString() {
        return connectionString;
    }

    /**
     * Set the connection string.
     * @param connectionString the connection string.
     */
    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    @Override
    public String getEntityName() {
        return entityName;
    }

    /**
     * Set the entity name.
     * @param entityName the entity name.
     */
    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    @Override
    public ServiceBusEntityType getEntityType() {
        return entityType;
    }

    /**
     * Set the entity type.
     * @param entityType the entity type.
     */
    public void setEntityType(ServiceBusEntityType entityType) {
        this.entityType = entityType;
    }
}
