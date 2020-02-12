// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to send an {@link EventDataBatch} to an Azure Event Hub using Azure Identity.
 */
public class PublishEventsWithAzureIdentity {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Main method to invoke this demo on how to send an {@link EventDataBatch} to an Azure Event Hub.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        List<EventData> telemetryEvents = Arrays.asList(
            new EventData("Roast beef".getBytes(UTF_8)),
            new EventData("Cheese".getBytes(UTF_8)),
            new EventData("Tofu".getBytes(UTF_8)),
            new EventData("Turkey".getBytes(UTF_8)));

        // The default azure credential checks multiple locations for credentials and determines the best one to use.
        // For the purpose of this sample, create a service principal and set the following environment variables.
        // See https://docs.microsoft.com/azure/active-directory/develop/howto-create-service-principal-portal for
        // information on how to create a service principal.
        System.setProperty("AZURE_CLIENT_ID", "<<insert-service-principal-client-id>>");
        System.setProperty("AZURE_CLIENT_ID", "<<insert-service-principal-client-application-secret>>");
        System.setProperty("AZURE_TENANT_ID", "<<insert-service-principal-tenant-id>>");

        // DefaultAzureCredentialBuilder exists inside the azure-identity package.
        TokenCredential credential = new DefaultAzureCredentialBuilder()
            .build();

        // Create a producer.
        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubProducerClient producer = new EventHubClientBuilder()
            .credential(
                "<<fully-qualified-namespace>>",
                "<<event-hub-name>>",
                credential)
            .buildProducerClient();

        // Creates an EventDataBatch where the Event Hubs service will automatically load balance the events between all
        // available partitions.
        EventDataBatch currentBatch = producer.createBatch();

        // We try to add as many events as a batch can fit based on the event size and send to Event Hub when
        // the batch can hold no more events. Create a new batch for next set of events and repeat until all events
        // are sent.
        for (EventData event : telemetryEvents) {
            if (currentBatch.tryAdd(event)) {
                continue;
            }

            // The batch is full, so we create a new batch and send the batch.
            producer.send(currentBatch);
            currentBatch = producer.createBatch();

            // Add that event that we couldn't before.
            if (!currentBatch.tryAdd(event)) {
                System.err.printf("Event is too large for an empty batch. Skipping. Max size: %s. Event: %s%n",
                    currentBatch.getMaxSizeInBytes(), event.getBodyAsString());
            }
        }

        producer.send(currentBatch);
    }
}
