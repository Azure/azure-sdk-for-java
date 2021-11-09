// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core.properties;

import com.azure.spring.core.aware.authentication.ConnectionStringAware;
import com.azure.spring.core.connectionstring.implementation.EventHubConnectionString;
import com.azure.spring.core.properties.AbstractAzureSdkProperties;
import com.azure.spring.service.eventhubs.properties.EventHubCommonDescriptor;

/**
 * Common properties shared by event hub namespace, a producer, and a consumer.
 */
abstract class CommonProperties extends AbstractAzureSdkProperties implements EventHubCommonDescriptor, ConnectionStringAware {


    private String domainName = "servicebus.windows.net";
    private String namespace;
    private String eventHubName;
    private String connectionString;
    private String customEndpointAddress;

    private String extractFqdnFromConnectionString() {
        if (this.connectionString == null) {
            return null;
        }
        return new EventHubConnectionString(this.connectionString).getFullyQualifiedNamespace();
    }

    private String extractEventHubNameFromConnectionString() {
        if (this.connectionString == null) {
            return null;
        }
        return new EventHubConnectionString(this.connectionString).getEntityPath();
    }


    // FQDN = the FQDN of the EventHubs namespace you created (it includes the EventHubs namespace name followed by
    // servicebus.windows.net)
    // Endpoint=sb://<FQDN>/;SharedAccessKeyName=<KeyName>;SharedAccessKey=<KeyValue>
    // https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-get-connection-string
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
    public String getEventHubName() {
        return eventHubName == null ? extractEventHubNameFromConnectionString() : this.eventHubName;
    }

    public void setEventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
    }

    @Override
    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    @Override
    public String getCustomEndpointAddress() {
        return customEndpointAddress;
    }

    public void setCustomEndpointAddress(String customEndpointAddress) {
        this.customEndpointAddress = customEndpointAddress;
    }
}
