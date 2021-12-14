// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus.properties;

import com.azure.spring.cloud.autoconfigure.properties.core.AbstractAzureAmqpCP;
import com.azure.spring.core.implementation.connectionstring.ServiceBusConnectionString;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;

/**
 *
 */
public abstract class AzureServiceBusCommonProperties extends AbstractAzureAmqpCP {

    // https://help.boomi.com/bundle/connectors/page/r-atm-Microsoft_Azure_Service_Bus_connection.html
    // https://docs.microsoft.com/en-us/rest/api/servicebus/addressing-and-protocol
    protected String domainName = "servicebus.windows.net";

    protected String namespace;

    protected String connectionString;

    private String entityName;

    private ServiceBusEntityType entityType;

    private String extractFqdnFromConnectionString() {
        if (this.connectionString == null) {
            return null;
        }
        return new ServiceBusConnectionString(this.connectionString).getFullyQualifiedNamespace();
    }

    public String getFullyQualifiedNamespace() {
        return this.namespace == null ? extractFqdnFromConnectionString() : (this.namespace + "." + domainName);
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String name) {
        this.entityName = name;
    }

    public ServiceBusEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(ServiceBusEntityType type) {
        this.entityType = type;
    }

}
