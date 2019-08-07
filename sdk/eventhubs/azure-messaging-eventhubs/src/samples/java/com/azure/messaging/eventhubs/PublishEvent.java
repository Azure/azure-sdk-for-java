// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpException;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to send a message to an Azure Event Hub.
 */
public class PublishEvent {
    /**
     * Main method to invoke this demo on how to send a message to an Azure Event Hub.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        // Instantiate a client that will be used to call the service.
        EventHubAsyncClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .buildAsyncClient();

        // Create a producer. This overload of `createProducer` does not accept any arguments. Consequently, events
        // sent from this producer are load balanced between all available partitions in the Event Hub instance.
        EventHubProducer producer = client.createProducer();

        // Create an event to send.
        EventData data = new EventData("Hello world!".getBytes(UTF_8));

        // Send that event. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Event Hub. It completes with an error if an exception occurred while sending
        // the event.
        producer.send(data).subscribe(
            (ignored) -> System.out.println("Event sent."),
            error -> {
                System.err.println("There was an error sending the event: " + error.toString());

                if (error instanceof AmqpException) {
                    AmqpException amqpException = (AmqpException) error;

                    System.err.println(String.format("Is send operation retriable? %s. Error condition: %s",
                        amqpException.isTransient(), amqpException.getErrorCondition()));
                }
            }, () -> {
                // Disposing of our producer and client.
                try {
                    producer.close();
                } catch (IOException e) {
                    System.err.println("Error encountered while closing producer: " + e.toString());
                }

                client.close();
            });
    }
}
