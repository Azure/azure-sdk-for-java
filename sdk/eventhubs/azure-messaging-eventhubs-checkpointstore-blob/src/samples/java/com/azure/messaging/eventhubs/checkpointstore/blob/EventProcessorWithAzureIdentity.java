// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs.checkpointstore.blob;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.EventProcessorClient;
import com.azure.messaging.eventhubs.EventProcessorClientBuilder;
import com.azure.messaging.eventhubs.models.ErrorContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Demonstrates how create an EventProcessorClient using Azure Identity to authenticate with Azure Storage Blobs and
 * Azure Event Hubs.
 *
 * @see <a href="https://github.com/Azure/azure-sdk-for-java/wiki/Identity-and-Authentication">Identity and
 *     Authentication</a>
 */
public class EventProcessorWithAzureIdentity {
    private static final String FULLY_QUALIFIED_NAMESPACE = "my-event-hubs-namespace.servicebus.windows.net";
    private static final String EVENT_HUB_NAME = "my-event-hub";

    public static final Consumer<EventContext> PARTITION_PROCESSOR = eventContext -> {
        System.out.printf("Processing event from partition %s with sequence number %d %n",
            eventContext.getPartitionContext().getPartitionId(), eventContext.getEventData().getSequenceNumber());
        if (eventContext.getEventData().getSequenceNumber() % 10 == 0) {
            eventContext.updateCheckpoint();
        }
    };

    public static final Consumer<ErrorContext> ERROR_HANDLER = errorContext -> {
        System.out.printf("Error occurred in partition processor for partition %s, %s.%n",
            errorContext.getPartitionContext().getPartitionId(),
            errorContext.getThrowable());
    };

    /**
     * The main method to run the sample.
     *
     * @param args Unused arguments to the sample.
     * @throws Exception if there are any errors while running the sample program.
     */
    public static void main(String[] args) throws Exception {
        // The DefaultAzureCredential is an aggregate credential that chooses the best credentials to use based
        // on the running environment. See https://github.com/Azure/azure-sdk-for-java/wiki/Identity-and-Authentication
        // for more information.
        // Ensure the credential chosen has permissions to access both the Storage Blob container and the Event Hub.
        // For example, if you are using a service principal, it has permissions to both resources. Or, if this is
        // deployed to a service that supports managed identity, that identity can access the resources.
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .build();

        BlobContainerAsyncClient blobContainerAsyncClient = new BlobContainerClientBuilder()
            .credential(credential)
            .endpoint("<< STORAGE ENDPOINT >>")
            .containerName("<< CONTAINER NAME >>")
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.HEADERS))
            .buildAsyncClient();

        EventProcessorClientBuilder eventProcessorClientBuilder = new EventProcessorClientBuilder()
            .credential(FULLY_QUALIFIED_NAMESPACE, EVENT_HUB_NAME, credential)
            .consumerGroup("<< CONSUMER GROUP NAME >>")
            .processEvent(PARTITION_PROCESSOR)
            .processError(ERROR_HANDLER)
            .checkpointStore(new BlobCheckpointStore(blobContainerAsyncClient));

        EventProcessorClient eventProcessorClient = eventProcessorClientBuilder.buildEventProcessorClient();
        // Starts the event processor
        eventProcessorClient.start();

        // Perform other tasks while the event processor is processing events in the background.
        TimeUnit.MINUTES.sleep(5);

        // Stops the event processor
        eventProcessorClient.stop();
    }
}
