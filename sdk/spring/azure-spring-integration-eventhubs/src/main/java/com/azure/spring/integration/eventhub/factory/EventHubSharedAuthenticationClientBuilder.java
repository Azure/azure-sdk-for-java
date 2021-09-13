// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.eventhub.factory;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.EventHubClientBuilder;

/**
 * An Event Hub client builder which shares authentication across different event hubs.
 */
public class EventHubSharedAuthenticationClientBuilder extends EventHubClientBuilder {

    private String eventHubName;


    public EventHubSharedAuthenticationClientBuilder eventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
        return this;
    }

    public EventHubSharedAuthenticationClientBuilder connectionString(String connectionString) {
        super.connectionString(connectionString, this.eventHubName);
        return this;
    }

    public EventHubSharedAuthenticationClientBuilder credential(String fullyQualifiedNamespace,
                                                                TokenCredential credential) {
        super.credential(fullyQualifiedNamespace, this.eventHubName, credential);
        return this;
    }

    public EventHubSharedAuthenticationClientBuilder credential(String fullyQualifiedNamespace,
                                                                AzureSasCredential credential) {
        super.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }

    public EventHubSharedAuthenticationClientBuilder credential(String fullyQualifiedNamespace,
                                                                AzureNamedKeyCredential credential) {
        super.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }
}
