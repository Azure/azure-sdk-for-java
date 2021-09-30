// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhubs.properties;

import com.azure.core.amqp.implementation.ConnectionStringProperties;
import com.azure.spring.cloud.autoconfigure.properties.AbstractAzureAmqpConfigurationProperties;

/**
 * Azure Event Hub related properties.
 */
public abstract class AzureEventHubCommonProperties extends AbstractAzureAmqpConfigurationProperties {

    protected String domainName = "servicebus.windows.net";
    protected String namespace;
    protected String eventHubName;
    protected String connectionString;
    protected String customEndpointAddress;
    protected Integer prefetchCount;

    protected String extractFQDNFromConnectionString() {
        if (this.connectionString == null) {
            return null;
        }
        return new ConnectionStringProperties(this.connectionString).getEndpoint().getHost();
    }

    protected String extractEventHubNameFromConnectionString() {
        if (this.connectionString == null) {
            return null;
        }
        return new ConnectionStringProperties(this.connectionString).getEntityPath();
    }


    // FQDN = the FQDN of the EventHubs namespace you created (it includes the EventHubs namespace name followed by
    // servicebus.windows.net)
    // Endpoint=sb://<FQDN>/;SharedAccessKeyName=<KeyName>;SharedAccessKey=<KeyValue>
    // https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-get-connection-string
    public String getFQDN() {
        return this.namespace == null ? extractFQDNFromConnectionString() : (this.namespace + "." + domainName);
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

    public String getEventHubName() {
        return eventHubName == null ? extractEventHubNameFromConnectionString() : eventHubName;
    }

    public void setEventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public void setConnectionString(String connectionString) {
        this.connectionString = connectionString;
    }

    public String getCustomEndpointAddress() {
        return customEndpointAddress;
    }

    public void setCustomEndpointAddress(String customEndpointAddress) {
        this.customEndpointAddress = customEndpointAddress;
    }

    public Integer getPrefetchCount() {
        return prefetchCount;
    }

    public void setPrefetchCount(Integer prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

}
