// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to sent events to a specific event hub by defining partition ID in producer option only.
 */
public class PublishEventsWithCustomMetadata {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Main method to invoke this demo about how to send a custom event list to an Azure Event Hub instance.
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

        // Because an event consists mainly of an opaque set of bytes, it may be difficult for consumers of those events to
        // make informed decisions about how to process them.
        //
        // In order to allow event publishers to offer better context for consumers, event data may also contain custom metadata,
        // in the form of a set of key/value pairs. This metadata is not used by, or in any way meaningful to, the Event Hubs
        // service; it exists only for coordination between event publishers and consumers.
        //
        // One common scenario for the inclusion of metadata is to provide a hint about the type of data contained by an event,
        // so that consumers understand its format and can deserialize it appropriately.
        //
        // We will publish two events based on simple sentences, but will attach some custom metadata with
        // pretend type names and other hints. Note that the set of metadata is unique to an event; there is no need for every
        // event in a batch to have the same metadata properties available nor the same data type for those properties.
        EventData firstEvent = new EventData("EventData Sample 1".getBytes(UTF_8));
        firstEvent.properties().put("EventType", "com.microsoft.samples.hello-event");
        firstEvent.properties().put("priority", 1);
        firstEvent.properties().put("score", 9.0);

        EventData secEvent = new EventData("EventData Sample 2".getBytes(UTF_8));
        secEvent.properties().put("EventType", "com.microsoft.samples.goodbye-event");
        secEvent.properties().put("priority", "17");
        secEvent.properties().put("blob", 10);

        final Flux<EventData> data = Flux.just(firstEvent, secEvent);

        // To create a consumer, we need to know what partition to connect to. We take the first partition id.
        // .blockFirst() here is used to synchronously block until the first partition id is emitted. The maximum wait
        // time is set by passing in the OPERATION_TIMEOUT value. If no item is emitted before the timeout elapses, a
        // TimeoutException is thrown.
        String firstPartition = client.getPartitionIds().blockFirst(OPERATION_TIMEOUT);

        // Send that event. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Event Hub. It completes with an error if an exception occurred while sending
        // the event.
        producer.send(data).subscribe(
            (ignored) -> System.out.println("Event sent to specific partition, ID = " + firstPartition),
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
