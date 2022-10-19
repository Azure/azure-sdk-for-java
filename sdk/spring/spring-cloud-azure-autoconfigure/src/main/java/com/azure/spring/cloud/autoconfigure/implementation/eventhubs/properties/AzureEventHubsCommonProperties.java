// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties;

import com.azure.spring.cloud.autoconfigure.implementation.properties.core.AbstractAzureAmqpConfigurationProperties;
import com.azure.spring.cloud.core.implementation.connectionstring.EventHubsConnectionString;

/**
 * Azure Event Hubs related properties.
 */
public abstract class AzureEventHubsCommonProperties extends AbstractAzureAmqpConfigurationProperties {

    /**
     * The domain name of an Event Hub namespace.
     */
    private String domainName;
    /**
     * The namespace of an event hub, which is the prefix of the FQDN. A FQDN should be composed of &lt;NamespaceName&gt;.&lt;DomainName&gt;
     */
    private String namespace;
    /**
     * The name of an event hub.
     */
    private String eventHubName;
    /**
     * Connection string to connect to an event hub.
     */
    private String connectionString;
    /**
     * A custom endpoint address when connecting to the Event Hubs service. This can be useful when your network does
     * not allow connecting to the standard Azure Event Hubs endpoint address, but does allow connecting through an
     * intermediary. For example: https://my.custom.endpoint.com:55300.
     */
    private String customEndpointAddress;

    protected String extractFqdnFromConnectionString() {
        if (this.connectionString == null) {
            return null;
        }
        return new EventHubsConnectionString(this.connectionString).getFullyQualifiedNamespace();
    }

    protected String extractEventHubNameFromConnectionString() {
        if (this.connectionString == null) {
            return null;
        }
        return new EventHubsConnectionString(this.connectionString).getEntityPath();
    }


    // FQDN = the FQDN of the EventHubs namespace you created (it includes the EventHubs namespace name followed by
    // servicebus.windows.net)
    // Endpoint=sb://<FQDN>/;SharedAccessKeyName=<KeyName>;SharedAccessKey=<KeyValue>
    // https://docs.microsoft.com/azure/event-hubs/event-hubs-get-connection-string
    public String getFullyQualifiedNamespace() {
        return this.namespace == null ? extractFqdnFromConnectionString() : buildFqdnFromNamespace();
    }

    private String buildFqdnFromNamespace() {
        if (namespace == null || domainName == null) {
            return null;
        }
        return this.namespace + "." + domainName;
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

}
