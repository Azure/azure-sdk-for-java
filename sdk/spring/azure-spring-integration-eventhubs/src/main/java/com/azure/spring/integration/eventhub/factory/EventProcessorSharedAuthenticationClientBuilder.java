// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.factory;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;

/**
 * An Event Processor client builder which shares authentication across different event hubs.
 */
public class EventProcessorSharedAuthenticationClientBuilder extends EventProcessorClientBuilder {

    private String eventHubName;


    public EventProcessorSharedAuthenticationClientBuilder eventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
        return this;
    }

    public EventProcessorSharedAuthenticationClientBuilder connectionString(String connectionString) {
        super.connectionString(connectionString, this.eventHubName);
        return this;
    }

    public EventProcessorSharedAuthenticationClientBuilder credential(String fullyQualifiedNamespace,
                                                                      TokenCredential credential) {
        super.credential(fullyQualifiedNamespace, this.eventHubName, credential);
        return this;
    }

    public EventProcessorSharedAuthenticationClientBuilder credential(String fullyQualifiedNamespace,
                                                                      AzureSasCredential credential) {
        super.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }

    public EventProcessorSharedAuthenticationClientBuilder credential(String fullyQualifiedNamespace,
                                                                      AzureNamedKeyCredential credential) {
        super.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }
}
