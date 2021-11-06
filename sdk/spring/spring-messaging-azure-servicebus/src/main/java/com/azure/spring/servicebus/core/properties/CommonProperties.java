// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core.properties;

import com.azure.spring.core.connectionstring.implementation.ServiceBusConnectionString;
import com.azure.spring.core.properties.AzureSdkProperties;
import com.azure.spring.service.servicebus.properties.ServiceBusCommonDescriptor;

/**
 * Common properties shared by Service Bus namespace, a producer, and a consumer.
 */
public class CommonProperties extends AzureSdkProperties implements ServiceBusCommonDescriptor {

    private String domainName = "servicebus.windows.net";
    private String namespace;
    private String connectionString;

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

}
