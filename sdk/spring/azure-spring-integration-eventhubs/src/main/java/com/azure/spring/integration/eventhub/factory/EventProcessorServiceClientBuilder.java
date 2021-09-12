package com.azure.spring.integration.eventhub.factory;

import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;

/**
 * @author Xiaolu Dai, 2021/9/9.
 */
public class EventProcessorServiceClientBuilder extends EventProcessorClientBuilder {

    private String eventHubName;


    public EventProcessorServiceClientBuilder eventHubName(String eventHubName) {
        this.eventHubName = eventHubName;
        return this;
    }

    public EventProcessorServiceClientBuilder connectionString(String connectionString) {
        super.connectionString(connectionString, this.eventHubName);
        return this;
    }

    public EventProcessorServiceClientBuilder credential(String fullyQualifiedNamespace,
                                            TokenCredential credential) {
        super.credential(fullyQualifiedNamespace, this.eventHubName, credential);
        return this;
    }

    public EventProcessorServiceClientBuilder credential(String fullyQualifiedNamespace,
                                            AzureSasCredential credential) {
        super.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }

    public EventProcessorServiceClientBuilder credential(String fullyQualifiedNamespace,
                                            AzureNamedKeyCredential credential) {
        super.credential(fullyQualifiedNamespace, eventHubName, credential);
        return this;
    }
}
