// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.eventhubs.models.SendOptions;
import reactor.core.publisher.Flux;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Send a list of events with send option configured
 */
public class PublishEventsWithPartitionKey {

    /**
     * Main method to invoke this demo about how to send a list of events with partition ID configured in send option
     * to an Azure Event Hub instance.
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

        // Create a producer. This overload of `createProducer` does not accept any arguments
        EventHubProducer producer = client.createProducer();

        // We will publish three events based on simple sentences.
        Flux<EventData> data = Flux.just(
            new EventData("Ball".getBytes(UTF_8)),
            new EventData("Net".getBytes(UTF_8)),
            new EventData("Players".getBytes(UTF_8)));

        // When an Event Hub producer is not associated with any specific partition, it may be desirable to request that
        // the Event Hubs service keep different events or batches of events together on the same partition. This can be
        // accomplished by setting a partition key when publishing the events.
        //
        // The partition key is NOT the identifier of a specific partition. Rather, it is an arbitrary piece of string data
        // that Event Hubs uses as the basis to compute a hash value. Event Hubs will associate the hash value with a specific
        // partition, ensuring that any events published with the same partition key are rerouted to the same partition.
        //
        // All of event data send to the same partition of the partition key 'basketball' associate with.
        //
        // Note that there is no means of accurately predicting which partition will be associated with a given partition key;
        // we can only be assured that it will be a consistent choice of partition. If you have a need to understand which
        // exact partition an event is published to, you will need to use an Event Hub producer associated with that partition.
        SendOptions sendOptions = new SendOptions().partitionKey("basketball");

        // Send that event. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Event Hub. It completes with an error if an exception occurred while sending
        // the event.
        producer.send(data, sendOptions).subscribe(
            (ignored) -> System.out.println("Sending a list of events to a partition that the partition key maps to..."),
            error -> {
                System.err.println("There was an error sending the event batch: " + error.toString());

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
