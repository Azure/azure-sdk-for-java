// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.servicebus;

import com.azure.spring.core.aware.authentication.NamedKeyAware;
import com.azure.spring.core.aware.authentication.SasTokenAware;
import com.azure.spring.core.implementation.connectionstring.ServiceBusConnectionString;
import com.azure.spring.core.properties.authentication.NamedKeyProperties;
import com.azure.spring.service.core.properties.AbstractAmqpProperties;
import com.azure.spring.service.servicebus.properties.ServiceBusClientCommonProperties;
import com.azure.spring.service.servicebus.properties.ServiceBusEntityType;


public class TestServiceBusClientCommonProperties extends AbstractAmqpProperties
    implements ServiceBusClientCommonProperties, SasTokenAware, NamedKeyAware {

    private String domainName = "servicebus.windows.net";
    private String namespace;
    private String connectionString;
    private String entityName;
    private ServiceBusEntityType entityType;
    private String sasToken;
    private NamedKeyProperties namedKey;

    @Override
    public NamedKeyProperties getNamedKey() {
        return namedKey;
    }

    public void setNamedKey(NamedKeyProperties namedKey) {
        this.namedKey = namedKey;
    }

    public void setSasToken(String sasToken) {
        this.sasToken = sasToken;
    }

    @Override
    public String getSasToken() {
        return sasToken;
    }

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

    @Override
    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    @Override
    public ServiceBusEntityType getEntityType() {
        return entityType;
    }

    public void setEntityType(ServiceBusEntityType entityType) {
        this.entityType = entityType;
    }
}
