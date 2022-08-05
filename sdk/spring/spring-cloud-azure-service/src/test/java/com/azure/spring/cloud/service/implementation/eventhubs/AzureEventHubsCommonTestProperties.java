// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.eventhubs;

import com.azure.spring.cloud.core.implementation.connectionstring.EventHubsConnectionString;
import com.azure.spring.cloud.core.implementation.properties.AzureAmqpSdkProperties;
import com.azure.spring.cloud.service.implementation.eventhubs.properties.EventHubClientCommonProperties;

/**
 * Azure Event Hubs related properties.
 */
abstract class AzureEventHubsCommonTestProperties extends AzureAmqpSdkProperties
    implements EventHubClientCommonProperties {

    protected String domainName = "servicebus.windows.net";
    protected String namespace;
    protected String eventHubName;
    protected String connectionString;
    protected String customEndpointAddress;

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
    public String getEventHubName() {
        return eventHubName == null ? extractEventHubNameFromConnectionString() : eventHubName;
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
