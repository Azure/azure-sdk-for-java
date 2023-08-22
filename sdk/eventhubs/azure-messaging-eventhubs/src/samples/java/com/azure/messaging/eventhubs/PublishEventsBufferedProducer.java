// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.models.SendBatchFailedContext;
import com.azure.messaging.eventhubs.models.SendBatchSucceededContext;
import com.azure.messaging.eventhubs.models.SendOptions;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * Sample demonstrates how to publish messages using {@link EventHubBufferedProducerClient}.
 */
public class PublishEventsBufferedProducer {
    /**
     * Main method to invoke this demo.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // The default azure credential checks multiple locations for credentials and determines the best one to use.
        // For the purpose of this sample, create a service principal and set the following environment variables.
        // See https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal for
        // information on how to create a service principal.
        System.setProperty("AZURE_CLIENT_ID", "<<insert-service-principal-client-id>>");
        System.setProperty("AZURE_CLIENT_SECRET", "<<insert-service-principal-client-application-secret>>");
        System.setProperty("AZURE_TENANT_ID", "<<insert-service-principal-tenant-id>>");

        // DefaultAzureCredentialBuilder exists inside the azure-identity package.
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .build();

        // Create a producer.
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubBufferedProducerClient producer = new EventHubBufferedProducerClientBuilder()
            .credential(
                "<<fully-qualified-namespace>>",
                "<<event-hub-name>>",
                credential)
            .onSendBatchSucceeded(succeededContext -> onSuccess(succeededContext))
            .onSendBatchFailed(failedContext -> onFailed(failedContext))
            .buildClient();

        // Querying the partition identifiers for the Event Hub. Then calling client.getPartitionProperties with the
        // identifier to get information about each partition.
        final EventHubProperties properties = producer.getEventHubProperties();
        System.out.printf("Event Hub Information: %s; Created: %s; PartitionIds: [%s]%n",
            properties.getName(),
            properties.getCreatedAt(),
            String.join(", ", properties.getPartitionIds()));

        // Sending a set of events to be distributed to partition 0.
        final List<EventData> events = IntStream.range(0, 10).mapToObj(index -> {
            return new EventData("Event # " + index);
        }).collect(Collectors.toList());

        final SendOptions sendOptions = new SendOptions()
            .setPartitionId("0");

        System.out.printf("Enqueuing events to partition: %s%n", sendOptions.getPartitionId());
        producer.enqueueEvents(events, sendOptions);

        System.out.println("Disposing of producer");
        producer.close();
    }

    /**
     * Method invoked when a batch is successfully published to an Event Hub.
     *
     * @param succeededContext The context.
     */
    private static void onSuccess(SendBatchSucceededContext succeededContext) {
        final List<EventData> events = StreamSupport.stream(succeededContext.getEvents().spliterator(), false)
            .collect(Collectors.toList());

        System.out.printf("Batch published to partition '%s'. # of Events: %d%n",
            succeededContext.getPartitionId(), events.size());
    }

    /**
     * Method invoked when a batch could not be published to an Event Hub.
     *
     * @param failedContext The context around the failure.
     */
    private static void onFailed(SendBatchFailedContext failedContext) {
        final List<EventData> events = StreamSupport.stream(failedContext.getEvents().spliterator(), false)
            .collect(Collectors.toList());

        System.out.printf("Failed to publish events to partition '%s'. # of Events: %d.  Error: %s%n",
            failedContext.getPartitionId(), events.size(), failedContext.getThrowable());
    }
}
