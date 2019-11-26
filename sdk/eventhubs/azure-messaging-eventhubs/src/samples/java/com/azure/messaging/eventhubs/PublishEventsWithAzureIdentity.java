// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to send an {@link EventDataBatch} to an Azure Event Hub using Azure Identity.
 */
public class PublishEventsWithAzureIdentity {
    /**
     * Main method to invoke this demo on how to send an {@link EventDataBatch} to an Azure Event Hub.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        Flux<EventData> telemetryEvents = Flux.just(
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
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .credential(
                "<<fully-qualified-namespace>>",
                "<<event-hub-name>>",
                credential)
            .buildAsyncProducerClient();

        final AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>(
            producer.createBatch().block());

        // The sample Flux contains three events, but it could be an infinite stream of telemetry events.
        telemetryEvents.subscribe(event -> {
            final EventDataBatch batch = currentBatch.get();
            if (!batch.tryAdd(event)) {
                producer.createBatch().map(newBatch -> {
                    currentBatch.set(newBatch);

                    // Adding that event we couldn't add before.
                    newBatch.tryAdd(event);
                    return producer.send(batch);
                }).block();
            }
        }, error -> System.err.println("Error received:" + error),
            () -> {
                final EventDataBatch batch = currentBatch.getAndSet(null);
                if (batch != null) {
                    producer.send(batch).block();
                }
            });

        // Sleeping this thread because we want to wait for all the events to send before ending the program.
        // `.subscribe` is not a blocking call. It coordinates the callbacks and starts the send operation, but does not
        // wait for it to complete.
        // Customers can chain together Reactor operators like .then() and `.doFinally` if they want an operation to run
        // after all the events have been transmitted. .block() can also be used to turn the call into a synchronous
        // operation.
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // Disposing of our producer.
            producer.close();
        }
    }
}
