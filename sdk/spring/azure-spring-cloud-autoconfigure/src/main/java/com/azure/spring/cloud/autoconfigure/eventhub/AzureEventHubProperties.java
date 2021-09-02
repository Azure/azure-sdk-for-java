// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.eventhub;

import com.azure.spring.core.properties.AzureProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Pattern;

/**
 * @author Warren Zhu
 */
@Validated
@ConfigurationProperties("spring.cloud.azure.eventhub")
public class AzureEventHubProperties extends AzureProperties {

    private String domainName = "servicebus.windows.net";
    private String namespace;
    private String eventHubName;
    private String connectionString;
    private boolean isSharedConnection;
    private String customEndpointAddress;
    private String consumerGroup;
    private Integer prefetchCount;

    /**
     * TODO(xiada): Do we still need to define this in event properties
     */
    @Pattern(regexp = "^[a-z0-9]{3,24}$",
        message = "must be between 3 and 24 characters in length and use numbers and lower-case letters only")
    private String checkpointStorageAccount;

    private String checkpointAccessKey;

    private String checkpointContainer;

    // FQDN = the FQDN of the EventHubs namespace you created (it includes the EventHubs namespace name followed by
    // servicebus.windows.net)
    // Endpoint=sb://<FQDN>/;SharedAccessKeyName=<KeyName>;SharedAccessKey=<KeyValue>
    // https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-get-connection-string
    public String getFQDN() {
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
        return eventHubName;
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

    public boolean isSharedConnection() {
        return isSharedConnection;
    }

    public void setSharedConnection(boolean sharedConnection) {
        isSharedConnection = sharedConnection;
    }

    public String getCustomEndpointAddress() {
        return customEndpointAddress;
    }

    public void setCustomEndpointAddress(String customEndpointAddress) {
        this.customEndpointAddress = customEndpointAddress;
    }

    public String getConsumerGroup() {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup) {
        this.consumerGroup = consumerGroup;
    }

    public Integer getPrefetchCount() {
        return prefetchCount;
    }

    public void setPrefetchCount(Integer prefetchCount) {
        this.prefetchCount = prefetchCount;
    }

    public String getCheckpointStorageAccount() {
        return checkpointStorageAccount;
    }

    public void setCheckpointStorageAccount(String checkpointStorageAccount) {
        this.checkpointStorageAccount = checkpointStorageAccount;
    }

    public String getCheckpointAccessKey() {
        return checkpointAccessKey;
    }

    public void setCheckpointAccessKey(String checkpointAccessKey) {
        this.checkpointAccessKey = checkpointAccessKey;
    }

    public String getCheckpointContainer() {
        return checkpointContainer;
    }

    public void setCheckpointContainer(String checkpointContainer) {
        this.checkpointContainer = checkpointContainer;
    }
}
