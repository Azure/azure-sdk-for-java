// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClient;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducer;
import com.azure.messaging.eventhubs.SendOptions;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Send a list of events with send option configured
 */
public class PublishEventsWithPartitionKey {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);

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
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};SharedAccessKey={sharedAccessKey};EntityPath={eventHubPath}";

        // Instantiate a client that will be used to call the service.
        EventHubClient client = new EventHubClientBuilder()
            .connectionString(connectionString)
            .build();

        // Create a producer. This overload of `createProducer` does not accept any arguments
        EventHubProducer producer = client.createProducer();

        // We will publish a small batch of events based on simple sentences.
        List<EventData> dataList = new ArrayList<>();
        dataList.add(new EventData("EventData Sample 1".getBytes(UTF_8)));
        dataList.add(new EventData("EventData Sample 2 ".getBytes(UTF_8)));
        dataList.add(new EventData("EventData Sample 3".getBytes(UTF_8)));

        String firstPartition = client.getPartitionIds().blockFirst(OPERATION_TIMEOUT);

        // When an Event Hub producer is not associated with any specific partition, it may be desirable to request that
        // the Event Hubs service keep different events or batches of events together on the same partition. This can be
        // accomplished by setting a partition key when publishing the events.
        //
        // The partition key is NOT the identifier of a specific partition. Rather, it is an arbitrary piece of string data
        // that Event Hubs uses as the basis to compute a hash value. Event Hubs will associate the hash value with a specific
        // partition, ensuring that any events published with the same partition key are rerouted to the same partition.
        //
        // Note that there is no means of accurately predicting which partition will be associated with a given partition key;
        // we can only be assured that it will be a consistent choice of partition. If you have a need to understand which
        // exact partition an event is published to, you will need to use an Event Hub producer associated with that partition.
        SendOptions sendOptions = new SendOptions().partitionKey(firstPartition);

        // Send that event. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Event Hub. It completes with an error if an exception occurred while sending
        // the event.
        producer.send(dataList, sendOptions).subscribe(
            (ignored) -> System.out.println("A list of event sent to specific partition, partition ID = " + firstPartition),
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
