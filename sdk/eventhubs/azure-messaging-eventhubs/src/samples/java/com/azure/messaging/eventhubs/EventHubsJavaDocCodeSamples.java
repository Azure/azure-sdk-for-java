// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryMode;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpTransportType;
import com.azure.core.credential.AzureNamedKeyCredential;
import com.azure.core.credential.AzureSasCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.IterableStream;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.EventHubConnectionStringProperties;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * This class contains all code snippets that are used in Event Hubs JavaDocs.
 */
public class EventHubsJavaDocCodeSamples {
    private final EventHubClientBuilder builder = new EventHubClientBuilder();
    private final EventHubConsumerAsyncClient consumer = builder
        .connectionString("fake-string")
        .consumerGroup("consumer-group-name")
        .buildAsyncConsumerClient();

    /**
     * Code sample for creating a synchronous Event Hub producer.
     */
    public void createProducer() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.construct
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubProducerClient producer = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                credential)
            .buildProducerClient();
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.construct
    }

    /**
     * Code sample for creating a synchronous Event Hub producer using a connection string.
     */
    public void createProducerWithConnectionString() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.connectionstring
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        EventHubProducerClient producer = new EventHubClientBuilder()
            .connectionString(connectionString)
            .buildProducerClient();
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.connectionstring
    }

    /**
     * Code sample for creating an async Event Hub producer.
     */
    public void createProducerAsync() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerasyncclient.construct
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                credential)
            .buildAsyncProducerClient();
        // END: com.azure.messaging.eventhubs.eventhubproducerasyncclient.construct
    }

    /**
     * Code sample for creating a synchronous Event Hub Consumer.
     */
    public void createConsumer() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerclient.construct
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubConsumerClient consumer = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                credential)
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildConsumerClient();
        // END: com.azure.messaging.eventhubs.eventhubconsumerclient.construct
    }

    /**
     * Code sample for creating an async Event Hub Consumer.
     */
    public void createConsumerAsync() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.construct
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                credential)
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.construct
    }

    /**
     * Code sample for creating a producer client with web sockets.
     */
    public void createProducerWebSockets() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.websockets.construct
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        AmqpRetryOptions customRetryOptions = new AmqpRetryOptions()
            .setMaxRetries(5)
            .setMode(AmqpRetryMode.FIXED)
            .setTryTimeout(Duration.ofSeconds(60));

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubProducerClient producer = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                credential)
            .transportType(AmqpTransportType.AMQP_WEB_SOCKETS)
            .buildProducerClient();
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.websockets.construct
    }

    /**
     * Creates an EventData using application properties.
     */
    public void createEventData() {
        // BEGIN: com.azure.messaging.eventhubs.eventdata.getProperties
        TelemetryEvent telemetry = new TelemetryEvent("temperature", "37");
        byte[] serializedTelemetryData = telemetry.toString().getBytes(UTF_8);

        EventData eventData = new EventData(serializedTelemetryData);
        eventData.getProperties().put("eventType", TelemetryEvent.class.getName());
        // END: com.azure.messaging.eventhubs.eventdata.getProperties
    }

    /**
     * Code snippet for {@link EventHubClientBuilder#shareConnection()}.
     */
    public void sharingConnection() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubclientbuilder.shareconnection.construct
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubClientBuilder builder = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                credential)
            .shareConnection();

        // Both the producer and consumer created share the same underlying connection.
        EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        EventHubConsumerAsyncClient consumer = builder
            .consumerGroup("my-consumer-group")
            .buildAsyncConsumerClient();
        // END: com.azure.messaging.eventhubs.eventhubclientbuilder.shareconnection.construct

        producer.close();
        consumer.close();
    }

    //region EventHubConsumerAsyncClient snippets

    /**
     * Receives event data from a single partition.
     */
    public void receiveAsync() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-eventposition
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                new DefaultAzureCredentialBuilder().build())
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildAsyncConsumerClient();

        // Obtain partitionId from EventHubConsumerAsyncClient.getPartitionIds()
        String partitionId = "0";
        EventPosition startingPosition = EventPosition.latest();

        // Keep a reference to `subscription`. When the program is finished receiving events, call
        // subscription.dispose(). This will stop fetching events from the Event Hub.
        //
        // NOTE: This is a non-blocking call and will move to the next line of code after setting up the async
        // operation.  If the program ends after this, or the class is immediately disposed, no events will be
        // received.
        Disposable subscription = consumer.receiveFromPartition(partitionId, startingPosition)
            .subscribe(partitionEvent -> {
                PartitionContext partitionContext = partitionEvent.getPartitionContext();
                EventData event = partitionEvent.getData();

                System.out.printf("Received event from partition '%s'%n", partitionContext.getPartitionId());
                System.out.printf("Contents of event as string: '%s'%n", event.getBodyAsString());
            }, error -> {
                // This is a terminal signal.  No more events will be received from the same Flux object.
                System.err.print("An error occurred:" + error);
            }, () -> {
                // This is a terminal signal.  No more events will be received from the same Flux object.
                System.out.print("Stream has ended.");
            });
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-eventposition

        // When program ends, or you're done receiving all events.
        subscription.dispose();
    }

    /**
     * Receives event data with back pressure.
     */
    public void receiveBackpressureAsync() {
        String partitionId = "0";

        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString("fake-string")
            .consumerGroup("consumer-group-name")
            .buildAsyncConsumerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-eventposition-basesubscriber
        consumer.receiveFromPartition(partitionId, EventPosition.latest()).subscribe(new BaseSubscriber<PartitionEvent>() {
            private static final int NUMBER_OF_EVENTS = 5;
            private final AtomicInteger currentNumberOfEvents = new AtomicInteger();

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                // Tell the Publisher we only want 5 events at a time.
                request(NUMBER_OF_EVENTS);
            }

            @Override
            protected void hookOnNext(PartitionEvent value) {
                // Process the EventData

                // If the number of events we have currently received is a multiple of 5, that means we have reached the
                // last event the Publisher will provide to us. Invoking request(long) here, tells the Publisher that
                // the subscriber is ready to get more events from upstream.
                if (currentNumberOfEvents.incrementAndGet() % 5 == 0) {
                    request(NUMBER_OF_EVENTS);
                }
            }
        });
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-eventposition-basesubscriber
    }

    /**
     * Receives from all partitions.
     */
    public void receiveAllAsync() {
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString("fake-string")
            .consumerGroup("consumer-group-name")
            .buildAsyncConsumerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#boolean
        // Keep a reference to `subscription`. When the program is finished receiving events, call
        // subscription.dispose(). This will stop fetching events from the Event Hub.
        Disposable subscription = consumer.receive(true)
            .subscribe(partitionEvent -> {
                PartitionContext context = partitionEvent.getPartitionContext();
                EventData event = partitionEvent.getData();

                System.out.printf("Event %s is from partition %s%n.", event.getSequenceNumber(),
                    context.getPartitionId());
            }, error -> {
                // This is a terminal signal.  No more events will be received from the same Flux object.
                System.err.print("An error occurred:" + error);
            }, () -> {
                // This is a terminal signal.  No more events will be received from the same Flux object.
                System.out.print("Stream has ended.");
            });
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#boolean
    }

    /**
     * Receives from all partitions with last enqueued information.
     */
    public void receiveLastEnqueuedInformationAsync() {
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString("event-hub-instance-connection-string")
            .consumerGroup("consumer-group-name")
            .buildAsyncConsumerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receiveFromPartition#string-eventposition-receiveoptions
        // Set `setTrackLastEnqueuedEventProperties` to true to get the last enqueued information from the partition for
        // each event that is received.
        ReceiveOptions receiveOptions = new ReceiveOptions()
            .setTrackLastEnqueuedEventProperties(true);
        EventPosition startingPosition = EventPosition.earliest();

        // Receives events from partition "0" starting at the beginning of the stream.
        // Keep a reference to `subscription`. When the program is finished receiving events, call
        // subscription.dispose(). This will stop fetching events from the Event Hub.
        Disposable subscription = consumer.receiveFromPartition("0", startingPosition, receiveOptions)
            .subscribe(partitionEvent -> {
                LastEnqueuedEventProperties properties = partitionEvent.getLastEnqueuedEventProperties();
                System.out.printf("Information received at %s. Last enqueued sequence number: %s%n",
                    properties.getRetrievalTime(),
                    properties.getSequenceNumber());
            });
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receiveFromPartition#string-eventposition-receiveoptions
    }
    //endregion

    /**
     * Receives event data from a single partition.
     */
    public void receiveFromSinglePartition() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerclient.receive#string-int-eventposition-duration
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubConsumerClient consumer = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                credential)
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .buildConsumerClient();

        Instant twelveHoursAgo = Instant.now().minus(Duration.ofHours(12));
        EventPosition startingPosition = EventPosition.fromEnqueuedTime(twelveHoursAgo);
        String partitionId = "0";

        // Reads events from partition '0' and returns the first 100 received or until the 30 seconds has elapsed.
        IterableStream<PartitionEvent> events = consumer.receiveFromPartition(partitionId, 100,
            startingPosition, Duration.ofSeconds(30));

        Long lastSequenceNumber = -1L;
        for (PartitionEvent partitionEvent : events) {
            // For each event, perform some sort of processing.
            System.out.print("Event received: " + partitionEvent.getData().getSequenceNumber());
            lastSequenceNumber = partitionEvent.getData().getSequenceNumber();
        }

        // Figure out what the next EventPosition to receive from is based on last event we processed in the stream.
        // If lastSequenceNumber is -1L, then we didn't see any events the first time we fetched events from the
        // partition.
        if (lastSequenceNumber != -1L) {
            EventPosition nextPosition = EventPosition.fromSequenceNumber(lastSequenceNumber, false);

            // Gets the next set of events from partition '0' to consume and process.
            IterableStream<PartitionEvent> nextEvents = consumer.receiveFromPartition(partitionId, 100,
                nextPosition, Duration.ofSeconds(30));
        }
        // END: com.azure.messaging.eventhubs.eventhubconsumerclient.receive#string-int-eventposition-duration
    }

    //region EventHubProducerAsyncClient snippets

    /**
     * Code snippet demonstrating how to send a batch that automatically routes events to any partition.
     */
    public void batchAutomaticRoutingAsync() {
        // The required parameter is a way to authenticate with Event Hubs using credentials.
        // The connectionString provides a way to authenticate with Event Hub.
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .connectionString(
                "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}",
                "event-hub-name")
            .buildAsyncProducerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch
        // Creating a batch without options set, will allow for automatic routing of events to any partition.
        producer.createBatch().flatMap(batch -> {
            batch.tryAdd(new EventData("test-event-1"));
            batch.tryAdd(new EventData("test-event-2"));
            return producer.send(batch);
        }).subscribe(unused -> {
        },
            error -> System.err.println("Error occurred while sending batch:" + error),
            () -> System.out.println("Send complete."));
        // END: com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch

        producer.close();
    }

    /**
     * Code snippet demonstrating how to create an EventDataBatch at routes events to a single partition.
     */
    public void batchPartitionIdAsync() {
        EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-partitionId
        CreateBatchOptions options = new CreateBatchOptions().setPartitionId("1");
        producer.createBatch(options).flatMap(batch -> {
            batch.tryAdd(new EventData("test-event-1"));
            batch.tryAdd(new EventData("test-event-2"));
            return producer.send(batch);
        }).subscribe(unused -> {
        },
            error -> System.err.println("Error occurred while sending batch to partition 1:" + error),
            () -> System.out.println("Send to partition 1 complete."));
        // END: com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-partitionId

        producer.close();
    }

    /**
     * Code snippet demonstrating how to send events with a partition key.
     */
    public void batchPartitionKeyAsync() {
        EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-partitionKey
        CreateBatchOptions options = new CreateBatchOptions().setPartitionKey("bread");

        producer.createBatch(options).flatMap(batch -> {
            batch.tryAdd(new EventData("sourdough"));
            batch.tryAdd(new EventData("rye"));
            return producer.send(batch);
        }).subscribe(unused -> {
        },
            error -> System.err.println("Error occurred while sending batch:" + error),
            () -> System.out.println("Send complete."));
        // END: com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-partitionKey
    }

    /**
     * Code snippet demonstrating how to create a size-limited {@link EventDataBatch} and send it.
     */
    public void batchSizeLimitedAsync() {
        final EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        final EventData firstEvent = new EventData("92".getBytes(UTF_8));
        firstEvent.getProperties().put("telemetry", "latency");
        final EventData secondEvent = new EventData("98".getBytes(UTF_8));
        secondEvent.getProperties().put("telemetry", "cpu-temperature");

        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-int
        Flux<EventData> telemetryEvents = Flux.just(firstEvent, secondEvent);

        // Setting `setMaximumSizeInBytes` when creating a batch, limits the size of that batch.
        // In this case, all the batches created with these options are limited to 256 bytes.
        CreateBatchOptions options = new CreateBatchOptions().setMaximumSizeInBytes(256);
        AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>(
            producer.createBatch(options).block());

        // The sample Flux contains two events, but it could be an infinite stream of telemetry events.
        Disposable publishingOperation = telemetryEvents.flatMap(event -> {
            EventDataBatch batch = currentBatch.get();

            if (batch.tryAdd(event)) {
                return Mono.empty();
            }

            // Send the current batch then create another size-limited EventDataBatch and try to fit the event into
            // this new batch.
            return producer.send(batch).then(
                producer.createBatch(options).map(newBatch -> {
                    currentBatch.set(newBatch);

                    // Add the event that did not fit in the previous batch.
                    if (!newBatch.tryAdd(event)) {
                        return Mono.error(new IllegalArgumentException(
                            "Event was too large to fit in an empty batch. Max size: "
                                + newBatch.getMaxSizeInBytes()));
                    }

                    return Mono.empty();
                }));
        }).subscribe(unused -> {
        }, error -> {
            System.out.println("Error occurred publishing events: " + error);
        }, () -> {
            System.out.println("Completed publishing operation.");
        });
        // END: com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-int
    }

    /**
     * Code snippet to demonstrate how to send a list of events using
     * {@link EventHubProducerAsyncClient#send(Iterable)}.
     */
    public void sendIterableSampleAsync() {
        final EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncproducerclient.send#Iterable
        List<EventData> events = Arrays.asList(new EventData("maple"), new EventData("aspen"),
            new EventData("oak"));

        producer.send(events)
            .subscribe(unused -> {
            },
                error -> System.err.println("Error occurred while sending events:" + error),
                () -> System.out.println("Send complete."));
        // END: com.azure.messaging.eventhubs.eventhubasyncproducerclient.send#Iterable
    }

    /**
     * Code snippet to demonstrate how to send a list of events using
     * {@link EventHubProducerAsyncClient#send(Iterable, SendOptions)}.
     */
    public void sendIterableWithPartitionKeySampleAsync() {
        final EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncproducerclient.send#Iterable-SendOptions
        List<EventData> events = Arrays.asList(new EventData("Melbourne"), new EventData("London"),
            new EventData("New York"));

        SendOptions sendOptions = new SendOptions().setPartitionKey("cities");
        producer.send(events, sendOptions)
            .subscribe(unused -> {
            },
                error -> System.err.println("Error occurred while sending events:" + error),
                () -> System.out.println("Send complete."));
        // END: com.azure.messaging.eventhubs.eventhubasyncproducerclient.send#Iterable-SendOptions
    }

    //endregion

    //region EventHubProducerClient snippets

    /**
     * Code snippet demonstrating how to send a batch that automatically routes events to any partition.
     *
     * @throws IllegalArgumentException if an event is too large for an empty batch.
     */
    public void batchAutomaticRouting() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.createBatch
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        EventHubProducerClient producer = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                credential)
            .buildProducerClient();

        List<EventData> allEvents = Arrays.asList(new EventData("Foo"), new EventData("Bar"));
        EventDataBatch eventDataBatch = producer.createBatch();

        for (EventData eventData : allEvents) {
            if (!eventDataBatch.tryAdd(eventData)) {
                producer.send(eventDataBatch);
                eventDataBatch = producer.createBatch();

                // Try to add that event that couldn't fit before.
                if (!eventDataBatch.tryAdd(eventData)) {
                    throw new IllegalArgumentException("Event is too large for an empty batch. Max size: "
                        + eventDataBatch.getMaxSizeInBytes());
                }
            }
        }

        // send the last batch of remaining events
        if (eventDataBatch.getCount() > 0) {
            producer.send(eventDataBatch);
        }

        // Clients are expected to be long-lived objects.
        // Dispose of the producer to close any underlying resources when we are finished with it.
        producer.close();
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.createBatch
    }

    /**
     * Code snippet demonstrating how to create an EventDataBatch at routes events to a single partition.
     */
    public void batchPartitionId() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-partitionId
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        EventHubProducerClient producer = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                credential)
            .buildProducerClient();

        // Creating a batch with partitionId set will route all events in that batch to partition `0`.
        CreateBatchOptions options = new CreateBatchOptions().setPartitionId("0");
        EventDataBatch batch = producer.createBatch(options);

        // Add events to batch and when you want to send the batch, send it using the producer.
        producer.send(batch);
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-partitionId
    }

    /**
     * Code snippet demonstrating how to send events with a partition key.
     */
    public void batchPartitionKey() {
        final EventHubProducerClient producer = builder.buildProducerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-partitionKey
        List<EventData> events = Arrays.asList(new EventData("sourdough"), new EventData("rye"),
            new EventData("wheat"));

        // Creating a batch with partitionKey set will tell the service to hash the partitionKey and decide which
        // partition to send the events to. Events with the same partitionKey are always routed to the same partition.
        CreateBatchOptions options = new CreateBatchOptions().setPartitionKey("bread");
        EventDataBatch batch = producer.createBatch(options);

        events.forEach(event -> batch.tryAdd(event));
        producer.send(batch);
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-partitionKey
    }

    /**
     * Code snippet demonstrating how to create a size-limited {@link EventDataBatch} and send it.
     *
     * @throws IllegalArgumentException if an event is too large for an empty batch.
     */
    public void batchSizeLimited() {
        final EventHubProducerClient producer = builder.buildProducerClient();
        final EventData firstEvent = new EventData("92".getBytes(UTF_8));
        firstEvent.getProperties().put("telemetry", "latency");
        final EventData secondEvent = new EventData("98".getBytes(UTF_8));
        secondEvent.getProperties().put("telemetry", "cpu-temperature");
        final EventData thirdEvent = new EventData("120".getBytes(UTF_8));
        thirdEvent.getProperties().put("telemetry", "fps");

        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-int
        List<EventData> telemetryEvents = Arrays.asList(firstEvent, secondEvent, thirdEvent);

        // Setting `setMaximumSizeInBytes` when creating a batch, limits the size of that batch.
        // In this case, all the batches created with these options are limited to 256 bytes.
        CreateBatchOptions options = new CreateBatchOptions().setMaximumSizeInBytes(256);

        EventDataBatch currentBatch = producer.createBatch(options);

        // For each telemetry event, we try to add it to the current batch.
        // When the batch is full, send it then create another batch to add more events to.
        for (EventData event : telemetryEvents) {
            if (!currentBatch.tryAdd(event)) {
                producer.send(currentBatch);
                currentBatch = producer.createBatch(options);

                // Add the event we couldn't before.
                if (!currentBatch.tryAdd(event)) {
                    throw new IllegalArgumentException("Event is too large for an empty batch.");
                }
            }
        }
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-int
    }

    /**
     * Code snippet to demonstrate how to send a list of events using {@link EventHubProducerClient#send(Iterable)}.
     */
    public void sendIterableSample() {
        final EventHubProducerClient producer = builder.buildProducerClient();
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.send#Iterable
        List<EventData> events = Arrays.asList(new EventData("maple"), new EventData("aspen"),
            new EventData("oak"));
        producer.send(events);
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.send#Iterable
    }

    /**
     * Code snippet to demonstrate how to send a list of events using
     * {@link EventHubProducerClient#send(Iterable, SendOptions)}.
     */
    public void sendIterableWithPartitionKeySample() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.send#Iterable-SendOptions
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        EventHubProducerClient producer = new EventHubClientBuilder()
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                credential)
            .buildProducerClient();

        List<EventData> events = Arrays.asList(new EventData("Melbourne"), new EventData("London"),
            new EventData("New York"));

        SendOptions sendOptions = new SendOptions().setPartitionKey("cities");
        producer.send(events, sendOptions);
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.send#Iterable-SendOptions
    }

    //endregion

    //region EventProcessorClient snippets

    /**
     * Code snippet for showing how to create a new instance of {@link EventProcessorClient}.
     */
    public void createEventProcessor() {
        // BEGIN: com.azure.messaging.eventhubs.eventprocessorclientbuilder.construct
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .consumerGroup("<< CONSUMER GROUP NAME >>")
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                credential)
            .checkpointStore(new SampleCheckpointStore())
            .processEvent(eventContext -> {
                System.out.printf("Partition id = %s and sequence number of event = %s%n",
                    eventContext.getPartitionContext().getPartitionId(),
                    eventContext.getEventData().getSequenceNumber());
            })
            .processError(errorContext -> {
                System.out.printf("Error occurred in partition processor for partition %s, %s%n",
                    errorContext.getPartitionContext().getPartitionId(),
                    errorContext.getThrowable());
            })
            .buildEventProcessorClient();
        // END: com.azure.messaging.eventhubs.eventprocessorclientbuilder.construct
    }

    /**
     * Code snippet to show creation of an event processor that receives events in batches.
     */
    public void receiveBatchSample() {
        // BEGIN: com.azure.messaging.eventhubs.eventprocessorclientbuilder.batchreceive
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                credential)
            .checkpointStore(new SampleCheckpointStore())
            .processEventBatch(eventBatchContext -> {
                eventBatchContext.getEvents().forEach(eventData -> {
                    System.out.printf("Partition id = %s and sequence number of event = %s%n",
                        eventBatchContext.getPartitionContext().getPartitionId(),
                        eventData.getSequenceNumber());
                });
            }, 50, Duration.ofSeconds(30))
            .processError(errorContext -> {
                System.out.printf("Error occurred in partition processor for partition %s, %s%n",
                    errorContext.getPartitionContext().getPartitionId(),
                    errorContext.getThrowable());
            })
            .buildEventProcessorClient();
        // END: com.azure.messaging.eventhubs.eventprocessorclientbuilder.batchreceive
    }

    /**
     * Code snippet for showing how to start and stop an {@link EventProcessorClient}.
     */
    public void startStopSample() {
        // BEGIN: com.azure.messaging.eventhubs.eventprocessorclient.startstop
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .consumerGroup(EventHubClientBuilder.DEFAULT_CONSUMER_GROUP_NAME)
            .credential("<<fully-qualified-namespace>>", "<<event-hub-name>>",
                credential)
            .processEvent(eventContext -> {
                System.out.printf("Partition id = %s and sequence number of event = %s%n",
                    eventContext.getPartitionContext().getPartitionId(),
                    eventContext.getEventData().getSequenceNumber());
            })
            .processError(errorContext -> {
                System.out.printf("Error occurred in partition processor for partition %s, %s%n",
                    errorContext.getPartitionContext().getPartitionId(),
                    errorContext.getThrowable());
            })
            .checkpointStore(new SampleCheckpointStore())
            .buildEventProcessorClient();

        eventProcessorClient.start();

        // Continue to perform other tasks while the processor is running in the background.
        //
        // Finally, stop the processor client when application is finished.
        eventProcessorClient.stop();
        // END: com.azure.messaging.eventhubs.eventprocessorclient.startstop
    }

    //endregion

    //region EventHubBufferedProducerAsyncClient snippets

    public void createBufferedProducerAsync() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubbufferedproducerasyncclient.construct
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubBufferedProducerAsyncClient client = new EventHubBufferedProducerClientBuilder()
            .credential("fully-qualified-namespace", "event-hub-name", credential)
            .onSendBatchSucceeded(succeededContext -> {
                System.out.println("Successfully published events to: " + succeededContext.getPartitionId());
            })
            .onSendBatchFailed(failedContext -> {
                System.out.printf("Failed to published events to %s. Error: %s%n",
                    failedContext.getPartitionId(), failedContext.getThrowable());
            })
            .maxWaitTime(Duration.ofSeconds(60))
            .maxEventBufferLengthPerPartition(1500)
            .buildAsyncClient();
        // END: com.azure.messaging.eventhubs.eventhubbufferedproducerasyncclient.construct
    }

    public void enqueueBufferedMessagesAsync() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubbufferedproducerasyncclient.enqueueEvents-iterable
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubBufferedProducerAsyncClient client = new EventHubBufferedProducerClientBuilder()
            .credential("fully-qualified-namespace", "event-hub-name", credential)
            .onSendBatchSucceeded(succeededContext -> {
                System.out.println("Successfully published events to: " + succeededContext.getPartitionId());
            })
            .onSendBatchFailed(failedContext -> {
                System.out.printf("Failed to published events to %s. Error: %s%n",
                    failedContext.getPartitionId(), failedContext.getThrowable());
            })
            .buildAsyncClient();

        List<EventData> events = Arrays.asList(new EventData("maple"), new EventData("aspen"),
            new EventData("oak"));

        // Enqueues the events to be published.
        client.enqueueEvents(events).subscribe(numberOfEvents -> {
            System.out.printf("There are currently: %d events in buffer.%n", numberOfEvents);
        }, error -> {
                System.err.println("Error occurred enqueueing events: " + error);
            },
            () -> {
                System.out.println("Events successfully enqueued.");
            });

        // Seconds later, enqueue another event.
        client.enqueueEvent(new EventData("bonsai")).subscribe(numberOfEvents -> {
            System.out.printf("There are %d events in the buffer.%n", numberOfEvents);
        }, error -> {
                System.err.println("Error occurred enqueueing events: " + error);
            },
            () -> {
                System.out.println("Event successfully enqueued.");
            });

        // Causes any buffered events to be flushed before closing underlying connection.
        client.close();
        // END: com.azure.messaging.eventhubs.eventhubbufferedproducerasyncclient.enqueueEvents-iterable
    }

    //endregion

    //region EventHubBufferedProducerClient snippets

    public void createBufferedProducer() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubbufferedproducerclient.construct
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubBufferedProducerClient client = new EventHubBufferedProducerClientBuilder()
            .credential("fully-qualified-namespace", "event-hub-name", credential)
            .onSendBatchSucceeded(succeededContext -> {
                System.out.println("Successfully published events to: " + succeededContext.getPartitionId());
            })
            .onSendBatchFailed(failedContext -> {
                System.out.printf("Failed to published events to %s. Error: %s%n",
                    failedContext.getPartitionId(), failedContext.getThrowable());
            })
            .buildClient();
        // END: com.azure.messaging.eventhubs.eventhubbufferedproducerclient.construct
    }

    public void enqueueBufferedMessages() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubbufferedproducerclient.enqueueEvents-iterable
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        EventHubBufferedProducerClient client = new EventHubBufferedProducerClientBuilder()
            .credential("fully-qualified-namespace", "event-hub-name", credential)
            .onSendBatchSucceeded(succeededContext -> {
                System.out.println("Successfully published events to: " + succeededContext.getPartitionId());
            })
            .onSendBatchFailed(failedContext -> {
                System.out.printf("Failed to published events to %s. Error: %s%n",
                    failedContext.getPartitionId(), failedContext.getThrowable());
            })
            .buildClient();

        List<EventData> events = Arrays.asList(new EventData("maple"), new EventData("aspen"),
            new EventData("oak"));

        // Enqueues the events to be published.
        client.enqueueEvents(events);

        // Seconds later, enqueue another event.
        client.enqueueEvent(new EventData("bonsai"));

        // Causes any buffered events to be flushed before closing underlying connection.
        client.close();
        // END: com.azure.messaging.eventhubs.eventhubbufferedproducerclient.enqueueEvents-iterable
    }

    //endregion

    /**
     * Code snippet showing how to use {@link EventHubConnectionStringProperties}.
     */
    public void eventHubConnectionStringProperties() {
        // BEGIN: com.azure.messaging.eventhubs.models.eventhubconnectionstringproperties.construct
        String connectionString = "Endpoint=sb://demo-hub.servicebus.windows.net/;SharedAccessKeyName=TestAccessKey;"
            + "SharedAccessKey=TestAccessKeyValue;EntityPath=MyEventHub";

        EventHubConnectionStringProperties properties = EventHubConnectionStringProperties.parse(connectionString);
        AzureNamedKeyCredential credential = new AzureNamedKeyCredential(properties.getSharedAccessKeyName(),
            properties.getSharedAccessKey());

        EventHubProducerClient producer = new EventHubClientBuilder()
            .credential(properties.getFullyQualifiedNamespace(), properties.getEntityPath(), credential)
            .buildProducerClient();
        // END: com.azure.messaging.eventhubs.models.eventhubconnectionstringproperties.construct
    }

    /**
     * Code snippet showing how to use {@link EventHubConnectionStringProperties}.
     */
    public void eventHubConnectionStringPropertiesNamespace() {
        // BEGIN: com.azure.messaging.eventhubs.models.eventhubconnectionstringproperties.construct.namespace
        String connectionString = "Endpoint=sb://demo-hub.servicebus.windows.net/;"
            + "SharedAccessKeyName=NamespaceAccessKey;SharedAccessKey=NamespaceAccessKeyValue";

        String eventHubName = "my-event-hub";

        EventHubConnectionStringProperties properties = EventHubConnectionStringProperties.parse(connectionString);
        AzureNamedKeyCredential credential = new AzureNamedKeyCredential(properties.getSharedAccessKeyName(),
            properties.getSharedAccessKey());

        EventHubProducerClient producer = new EventHubClientBuilder()
            .credential(properties.getFullyQualifiedNamespace(), eventHubName, credential)
            .buildProducerClient();
        // END: com.azure.messaging.eventhubs.models.eventhubconnectionstringproperties.construct.namespace
    }

    /**
     * Code snippet showing how to use {@link EventHubConnectionStringProperties}.
     */
    public void eventHubConnectionStringPropertiesSas() {
        // BEGIN: com.azure.messaging.eventhubs.models.eventhubconnectionstringproperties.construct.sas
        // "sr" is the URI of the resource being accessed.
        // "se" is the expiration date of the signature.
        // "skn" is name of the authorization policy used to create the SAS
        String connectionString = "Endpoint={endpoint};EntityPath={entityPath};SharedAccessSignature="
            + "SharedAccessSignature sr={fullyQualifiedNamespace}&sig={signature}&se={expiry}&skn={policyName}";

        EventHubConnectionStringProperties properties = EventHubConnectionStringProperties.parse(connectionString);
        AzureSasCredential credential = new AzureSasCredential(connectionString);

        EventHubConsumerClient consumer = new EventHubClientBuilder()
            .credential(properties.getFullyQualifiedNamespace(), properties.getEntityPath(), credential)
            .buildConsumerClient();
        // END: com.azure.messaging.eventhubs.models.eventhubconnectionstringproperties.construct.sas
    }

    private static final class TelemetryEvent {
        private final String name;
        private final String value;

        private TelemetryEvent(String name, String value) {
            this.name = name;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("[name]=%s;[value]=%s", name, value);
        }
    }
}
