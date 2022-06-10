// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.eventhubs.core.properties;

import com.azure.spring.cloud.core.provider.connectionstring.ConnectionStringProvider;
import com.azure.spring.cloud.core.implementation.connectionstring.EventHubsConnectionString;
import com.azure.spring.cloud.core.implementation.properties.AzureAmqpSdkProperties;
import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventHubClientCommonProperties;

/**
 * Common properties shared by event hub namespace, a producer, and a consumer.
 */
public abstract class CommonProperties extends AzureAmqpSdkProperties implements EventHubClientCommonProperties, ConnectionStringProvider {


    private String domainName = "servicebus.windows.net";

    /**
     * The namespace of a event hub, which is the prefix of the FQDN. A FQDN should be composed of &lt;NamespaceName&gt;.&lt;DomainName&gt;
     */
    private String namespace;
    private String eventHubName;
    private String connectionString;
    private String customEndpointAddress;

    private String extractFqdnFromConnectionString() {
        if (this.connectionString == null) {
            return null;
        }
        return new EventHubsConnectionString(this.connectionString).getFullyQualifiedNamespace();
    }

    private String extractEventHubNameFromConnectionString() {
        if (this.connectionString == null) {
            return null;
        }
        return new EventHubsConnectionString(this.connectionString).getEntityPath();
    }


    // FQDN = the FQDN of the EventHubs namespace you created (it includes the EventHubs namespace name followed by
    // servicebus.windows.net)
    // Endpoint=sb://<FQDN>/;SharedAccessKeyName=<KeyName>;SharedAccessKey=<KeyValue>
    // https://docs.microsoft.com/azure/event-hubs/event-hubs-get-connection-string
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
    public String getEventHubName() {
        return eventHubName == null ? extractEventHubNameFromConnectionString() : this.eventHubName;
    }

    /**
     * Set the event hub name.
     * @param eventHubName the event hub name.
     */
    public void setEventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
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
    public String getCustomEndpointAddress() {
        return customEndpointAddress;
    }

    /**
     * Set the custom endpoint address.
     * @param customEndpointAddress the custom endpoint address.
     */
    public void setCustomEndpointAddress(String customEndpointAddress) {
        this.customEndpointAddress = customEndpointAddress;
    }
}
