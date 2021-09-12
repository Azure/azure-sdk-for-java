package com.azure.spring.integration.eventhub.factory;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.EventHubClientBuilder;

/**
 * @author Xiaolu Dai, 2021/9/9.
 */
public class EventHubServiceClientBuilder extends EventHubClientBuilder {

    private String eventHubName;


    public EventHubServiceClientBuilder eventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
        return this;
    }

    public EventHubServiceClientBuilder connectionString(String connectionString) {
        super.connectionString(connectionString, this.eventHubName);
        return this;
    }

    public EventHubServiceClientBuilder credential(String fullyQualifiedNamespace,
                                            TokenCredential credential) {
        super.credential(fullyQualifiedNamespace, this.eventHubName, credential);
        return this;
    }

    public EventHubServiceClientBuilder credential(String fullyQualifiedNamespace,
                                            AzureSasCredential credential) {
        super.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }

    public EventHubServiceClientBuilder credential(String fullyQualifiedNamespace,
                                            AzureNamedKeyCredential credential) {
        super.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }
}
