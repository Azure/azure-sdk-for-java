// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.amqp.exception.AmqpException;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventHubClient;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducer;
import com.azure.messaging.eventhubs.EventHubProducerOptions;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Sample demonstrates how to sent events to specific event hub by define partition ID in producer option only.
 */
public class PublishEventsToSpecificPartition {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Main method to invoke this demo about how to send a list of events with partition ID configured in producer option
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

        // To create a consumer, we need to know what partition to connect to. We take the first partition id.
        // .blockFirst() here is used to synchronously block until the first partition id is emitted. The maximum wait
        // time is set by passing in the OPERATION_TIMEOUT value. If no item is emitted before the timeout elapses, a
        // TimeoutException is thrown.
        String firstPartition = client.getPartitionIds().blockFirst(OPERATION_TIMEOUT);

        // When an Event Hub producer is associated with any specific partition, it can publish events only to that partition.
        // The producer has no ability to ask for the service to route events, including by using a partition key.
        //
        // If you attempt to use a partition key with an Event Hub producer that is associated with a partition, an exception
        // will occur. Otherwise, publishing to a specific partition is exactly the same as other publishing scenarios.
        EventHubProducerOptions producerOptions = new EventHubProducerOptions().partitionId(firstPartition);

        // Create a producer. Consequently, events sent from this producer will deliver to the specific partition ID Event Hub instance.
        EventHubProducer producer = client.createProducer(producerOptions);

        // Create an event list
        List<EventData> dataList = new ArrayList<>();
        dataList.add(new EventData("EventData Sample 1".getBytes(UTF_8)));
        dataList.add(new EventData("EventData Sample 2 ".getBytes(UTF_8)));
        dataList.add(new EventData("EventData Sample 3".getBytes(UTF_8)));

        // Send that event. This call returns a Mono<Void>, which we subscribe to. It completes successfully when the
        // event has been delivered to the Event Hub. It completes with an error if an exception occurred while sending
        // the event.
        producer.send(dataList).subscribe(
            (ignored) -> System.out.println("A list of Event sent."),
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
