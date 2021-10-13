// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;

/**
 * An Event Processor client builder which shares authentication across different event hubs.
 */
public class EventProcessorSharedAuthenticationClientBuilder extends EventProcessorClientBuilder {

    private String eventHubName;
    private String fullyQualifiedNamespace;
    private AzureNamedKeyCredential namedKeyCredential;
    private AzureSasCredential sasCredential;
    private TokenCredential tokenCredential;
    private String connectionString;


    public EventProcessorSharedAuthenticationClientBuilder eventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
        return this;
    }

    public EventProcessorSharedAuthenticationClientBuilder connectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    public EventProcessorSharedAuthenticationClientBuilder credential(String fullyQualifiedNamespace,
                                                                      TokenCredential credential) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.tokenCredential = credential;
        return this;
    }

    public EventProcessorSharedAuthenticationClientBuilder credential(String fullyQualifiedNamespace,
                                                                      AzureSasCredential credential) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.sasCredential = credential;
        return this;
    }

    public EventProcessorSharedAuthenticationClientBuilder credential(String fullyQualifiedNamespace,
                                                                      AzureNamedKeyCredential credential) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.namedKeyCredential = credential;
        return this;
    }

    @Override
    public EventProcessorClient buildEventProcessorClient() {
        if (this.tokenCredential != null) {
            super.credential(this.fullyQualifiedNamespace, this.eventHubName, this.tokenCredential);
        }
        if (this.sasCredential != null) {
            super.credential(this.fullyQualifiedNamespace, this.eventHubName, this.sasCredential);
        }
        if (this.namedKeyCredential != null) {
            super.credential(this.fullyQualifiedNamespace, this.eventHubName, this.namedKeyCredential);
        }
        if (this.connectionString != null) {
            super.connectionString(this.connectionString, this.eventHubName);
        }

        return super.buildEventProcessorClient();
    }
}
