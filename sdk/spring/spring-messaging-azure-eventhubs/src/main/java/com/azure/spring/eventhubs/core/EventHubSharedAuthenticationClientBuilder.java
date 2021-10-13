// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.core;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubConsumerAsyncClient;
import com.azure.messaging.eventhubs.EventHubConsumerClient;
import com.azure.messaging.eventhubs.EventHubProducerAsyncClient;
import com.azure.messaging.eventhubs.EventHubProducerClient;

/**
 * An Event Hub client builder which shares authentication across different event hubs.
 */
public class EventHubSharedAuthenticationClientBuilder extends EventHubClientBuilder {

    private String eventHubName;
    private String fullyQualifiedNamespace;
    private AzureNamedKeyCredential namedKeyCredential;
    private AzureSasCredential sasCredential;
    private TokenCredential tokenCredential;
    private String connectionString;

    public EventHubSharedAuthenticationClientBuilder eventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
        return this;
    }

    public EventHubSharedAuthenticationClientBuilder connectionString(String connectionString) {
        this.connectionString = connectionString;
        return this;
    }

    public EventHubSharedAuthenticationClientBuilder credential(String fullyQualifiedNamespace,
                                                                TokenCredential credential) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.tokenCredential = credential;
        return this;
    }

    public EventHubSharedAuthenticationClientBuilder credential(String fullyQualifiedNamespace,
                                                                AzureSasCredential credential) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.sasCredential = credential;
        return this;
    }

    public EventHubSharedAuthenticationClientBuilder credential(String fullyQualifiedNamespace,
                                                                AzureNamedKeyCredential credential) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.namedKeyCredential = credential;
        return this;
    }

    @Override
    public EventHubConsumerAsyncClient buildAsyncConsumerClient() {
        setShareAuthentication();
        return super.buildAsyncConsumerClient();
    }

    @Override
    public EventHubConsumerClient buildConsumerClient() {
        setShareAuthentication();
        return super.buildConsumerClient();
    }

    @Override
    public EventHubProducerAsyncClient buildAsyncProducerClient() {
        setShareAuthentication();
        return super.buildAsyncProducerClient();
    }

    @Override
    public EventHubProducerClient buildProducerClient() {
        setShareAuthentication();
        return super.buildProducerClient();
    }

    private void setShareAuthentication() {
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
    }
}
