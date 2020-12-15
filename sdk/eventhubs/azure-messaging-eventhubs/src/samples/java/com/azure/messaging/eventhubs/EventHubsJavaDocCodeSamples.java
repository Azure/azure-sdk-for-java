// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.IterableStream;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.EventPosition;
import com.azure.messaging.eventhubs.models.LastEnqueuedEventProperties;
import com.azure.messaging.eventhubs.models.PartitionContext;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import com.azure.messaging.eventhubs.models.ReceiveOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.reactivestreams.Subscription;
import reactor.core.Disposable;
import reactor.core.Exceptions;
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
        // BEGIN: com.azure.messaging.eventhubs.eventhubclientbuilder.instantiation
        // Toggling `shareConnection` instructs the builder to use the same underlying connection
        // for each consumer or producer created using the same builder instance.
        EventHubClientBuilder builder = new EventHubClientBuilder()
            .connectionString("event-hubs-instance-connection-string")
            .shareConnection();

        // Both the producer and consumer created share the same underlying connection.
        EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        EventHubConsumerAsyncClient consumer = builder
            .consumerGroup("my-consumer-group")
            .buildAsyncConsumerClient();
        // END: com.azure.messaging.eventhubs.eventhubclientbuilder.instantiation

        producer.close();
        consumer.close();
    }

    public void instantiateConsumerAsyncClient() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.instantiation
        // The required parameters are `consumerGroup` and a way to authenticate with Event Hubs using credentials.
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString("Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
                + "SharedAccessKey={key};EntityPath={eh-name}")
            .consumerGroup("consumer-group-name")
            .buildAsyncConsumerClient();
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.instantiation

        consumer.close();
    }

    /**
     * Receives event data from a single partition.
     */
    public void receive() {

        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-eventposition
        // Obtain partitionId from EventHubConsumerAsyncClient.getPartitionIds()
        String partitionId = "0";
        EventPosition startingPosition = EventPosition.latest();

        // Keep a reference to `subscription`. When the program is finished receiving events, call
        // subscription.dispose(). This will stop fetching events from the Event Hub.
        Disposable subscription = consumer.receiveFromPartition(partitionId, startingPosition)
            .subscribe(partitionEvent -> {
                PartitionContext partitionContext = partitionEvent.getPartitionContext();
                EventData event = partitionEvent.getData();

                System.out.printf("Received event from partition '%s'%n", partitionContext.getPartitionId());
                System.out.printf("Contents of event as string: '%s'%n", event.getBodyAsString());
            }, error -> System.err.print(error.toString()));
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#string-eventposition

        // When program ends, or you're done receiving all events.
        subscription.dispose();
    }

    /**
     * Receives event data with back pressure.
     */
    public void receiveBackpressure() {
        // Obtain partitionId from EventHubAsyncClient.getPartitionIds()
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
    public void receiveAll() {
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString("fake-string")
            .consumerGroup("consumer-group-name")
            .buildAsyncConsumerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#boolean
        // Receives events from all partitions from the beginning of each partition.
        consumer.receive(true).subscribe(partitionEvent -> {
            PartitionContext context = partitionEvent.getPartitionContext();
            EventData event = partitionEvent.getData();
            System.out.printf("Event %s is from partition %s%n.", event.getSequenceNumber(), context.getPartitionId());
        });
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receive#boolean
    }

    /**
     * Receives from all partitions with last enqueued information.
     */
    public void receiveLastEnqueuedInformation() {
        EventHubConsumerAsyncClient consumer = new EventHubClientBuilder()
            .connectionString("event-hub-instance-connection-string")
            .consumerGroup("consumer-group-name")
            .buildAsyncConsumerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receiveFromPartition#string-eventposition-receiveoptions
        // Set `setTrackLastEnqueuedEventProperties` to true to get the last enqueued information from the partition for
        // each event that is received.
        ReceiveOptions receiveOptions = new ReceiveOptions()
            .setTrackLastEnqueuedEventProperties(true);

        // Receives events from partition "0" as they come in.
        consumer.receiveFromPartition("0", EventPosition.earliest(), receiveOptions)
            .subscribe(partitionEvent -> {
                LastEnqueuedEventProperties properties = partitionEvent.getLastEnqueuedEventProperties();
                System.out.printf("Information received at %s. Last enqueued sequence number: %s%n",
                    properties.getRetrievalTime(),
                    properties.getSequenceNumber());
            });
        // END: com.azure.messaging.eventhubs.eventhubconsumerasyncclient.receiveFromPartition#string-eventposition-receiveoptions
    }

    /**
     * Code snippet for creating an EventHubConsumer
     */
    public void instantiateConsumerClient() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerclient.instantiation
        // The required parameters are `consumerGroup`, and a way to authenticate with Event Hubs using credentials.
        EventHubConsumerClient consumer = new EventHubClientBuilder()
            .connectionString("Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};"
                + "SharedAccessKey={key};Entity-Path={hub-name}")
            .consumerGroup("$DEFAULT")
            .buildConsumerClient();
        // END: com.azure.messaging.eventhubs.eventhubconsumerclient.instantiation

        consumer.close();
    }

    /**
     * Receives event data from a single partition.
     */
    public void receiveFromSinglePartition() {
        EventHubConsumerClient consumer = new EventHubClientBuilder()
            .connectionString("event-hub-instance-connection-string")
            .consumerGroup("consumer-group-name")
            .buildConsumerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubconsumerclient.receive#string-int-eventposition-duration
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

    /**
     * Code snippet demonstrating how to create an {@link EventHubProducerAsyncClient}.
     */
    public void instantiateProducerAsyncClient() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncproducerclient.instantiation
        // The required parameter is a way to authenticate with Event Hubs using credentials.
        // The connectionString provides a way to authenticate with Event Hub.
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .connectionString(
                "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}",
                "event-hub-name")
            .buildAsyncProducerClient();
        // END: com.azure.messaging.eventhubs.eventhubasyncproducerclient.instantiation

        producer.close();
    }

    /**
     * Code snippet demonstrating how to send a batch that automatically routes events to any partition.
     */
    public void batchAutomaticRoutingAsync() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch
        // The required parameter is a way to authenticate with Event Hubs using credentials.
        // The connectionString provides a way to authenticate with Event Hub.
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .connectionString(
                "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}",
                "event-hub-name")
            .buildAsyncProducerClient();

        // Creating a batch without options set, will allow for automatic routing of events to any partition.
        producer.createBatch().flatMap(batch -> {
            batch.tryAdd(new EventData("test-event-1"));
            batch.tryAdd(new EventData("test-event-2"));
            return producer.send(batch);
        }).subscribe(unused -> { },
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
        // Creating a batch with partitionId set will route all events in that batch to partition `foo`.
        CreateBatchOptions options = new CreateBatchOptions().setPartitionId("foo");
        producer.createBatch(options).flatMap(batch -> {
            batch.tryAdd(new EventData("test-event-1"));
            batch.tryAdd(new EventData("test-event-2"));
            return producer.send(batch);
        }).subscribe(unused -> { },
            error -> System.err.println("Error occurred while sending batch:" + error),
            () -> System.out.println("Send complete."));
        // END: com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-partitionId

        producer.close();
    }

    /**
     * Code snippet demonstrating how to send events with a partition key.
     */
    public void batchPartitionKeyAsync() {
        EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-partitionKey
        // Creating a batch with partitionKey set will tell the service to hash the partitionKey and decide which
        // partition to send the events to. Events with the same partitionKey are always routed to the same partition.
        CreateBatchOptions options = new CreateBatchOptions().setPartitionKey("bread");
        producer.createBatch(options).flatMap(batch -> {
            batch.tryAdd(new EventData("sourdough"));
            batch.tryAdd(new EventData("rye"));
            return producer.send(batch);
        }).subscribe(unused -> { },
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
        telemetryEvents.flatMap(event -> {
            final EventDataBatch batch = currentBatch.get();
            if (batch.tryAdd(event)) {
                return Mono.empty();
            }

            return Mono.when(
                producer.send(batch),
                producer.createBatch(options).map(newBatch -> {
                    currentBatch.set(newBatch);

                    // Add the event that did not fit in the previous batch.
                    if (!newBatch.tryAdd(event)) {
                        throw Exceptions.propagate(new IllegalArgumentException(
                            "Event was too large to fit in an empty batch. Max size: " + newBatch.getMaxSizeInBytes()));
                    }

                    return newBatch;
                }));
        }).then()
            .doFinally(signal -> {
                final EventDataBatch batch = currentBatch.getAndSet(null);
                if (batch != null && batch.getCount() > 0) {
                    producer.send(batch).block();
                }
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
        producer
            .send(events)
            .subscribe(unused -> { },
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
        producer
            .send(events, sendOptions)
            .subscribe(unused -> { },
                error -> System.err.println("Error occurred while sending events:" + error),
                () -> System.out.println("Send complete."));
        // END: com.azure.messaging.eventhubs.eventhubasyncproducerclient.send#Iterable-SendOptions
    }

    /**
     * Code snippet demonstrating how to create an {@link EventHubProducerClient}.
     */
    public void instantiateProducerClient() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.instantiation
        // The required parameter is a way to authenticate with Event Hubs using credentials.
        // The connectionString provides a way to authenticate with Event Hub.
        EventHubProducerClient producer = new EventHubClientBuilder()
            .connectionString(
                "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}",
                "event-hub-name")
            .buildProducerClient();
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.instantiation

        producer.close();
    }

    /**
     * Code snippet demonstrating how to send a batch that automatically routes events to any partition.
     *
     * @throws IllegalArgumentException if an event is too large for an empty batch.
     */
    public void batchAutomaticRouting() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.createBatch
        // The required parameter is a way to authenticate with Event Hubs using credentials.
        // The connectionString provides a way to authenticate with Event Hub.
        EventHubProducerClient producer = new EventHubClientBuilder()
            .connectionString(
                "Endpoint={fully-qualified-namespace};SharedAccessKeyName={policy-name};SharedAccessKey={key}",
                "event-hub-name")
            .buildProducerClient();
        List<EventData> events = Arrays.asList(new EventData("test-event-1"), new EventData("test-event-2"));

        // Creating a batch without options set, will allow for automatic routing of events to any partition.
        EventDataBatch batch = producer.createBatch();
        for (EventData event : events) {
            if (batch.tryAdd(event)) {
                continue;
            }

            producer.send(batch);
            batch = producer.createBatch();
            if (!batch.tryAdd(event)) {
                throw new IllegalArgumentException("Event is too large for an empty batch.");
            }
        }
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.createBatch

        producer.close();
    }

    /**
     * Code snippet demonstrating how to create an EventDataBatch at routes events to a single partition.
     */
    public void batchPartitionId() {
        final EventHubProducerClient producer = builder.buildProducerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.createBatch#CreateBatchOptions-partitionId
        // Creating a batch with partitionId set will route all events in that batch to partition `foo`.
        CreateBatchOptions options = new CreateBatchOptions().setPartitionId("foo");

        EventDataBatch batch = producer.createBatch(options);
        batch.tryAdd(new EventData("data-to-partition-foo"));
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
        final EventHubProducerClient producer = builder.buildProducerClient();
        // BEGIN: com.azure.messaging.eventhubs.eventhubproducerclient.send#Iterable-SendOptions
        List<EventData> events = Arrays.asList(new EventData("Melbourne"), new EventData("London"),
            new EventData("New York"));
        SendOptions sendOptions = new SendOptions().setPartitionKey("cities");
        producer.send(events, sendOptions);
        // END: com.azure.messaging.eventhubs.eventhubproducerclient.send#Iterable-SendOptions
    }

    /**
     * Code snippet for showing how to create a new instance of {@link EventProcessorClient}.
     *
     * @return A new instance of {@link EventProcessorClient}
     */
    // BEGIN: com.azure.messaging.eventhubs.eventprocessorclientbuilder.instantiation
    public EventProcessorClient createEventProcessor() {
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .consumerGroup("consumer-group")
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
            .connectionString(connectionString)
            .buildEventProcessorClient();
        return eventProcessorClient;
    }
    // END: com.azure.messaging.eventhubs.eventprocessorclientbuilder.instantiation

    /**
     * Code snippet to show creation of an event processor that receives events in batches.
     */
    public void receiveBatchSample() {
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        // BEGIN: com.azure.messaging.eventhubs.eventprocessorclientbuilder.batchreceive
        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .consumerGroup("consumer-group")
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
            .connectionString(connectionString)
            .buildEventProcessorClient();
        // END: com.azure.messaging.eventhubs.eventprocessorclientbuilder.batchreceive
    }

    /**
     * Code snippet for showing how to start and stop an {@link EventProcessorClient}.
     */
    public void startStopSample() {
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};"
            + "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";
        EventProcessorClient eventProcessorClient = new EventProcessorClientBuilder()
            .connectionString(connectionString)
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
            .consumerGroup("consumer-group")
            .buildEventProcessorClient();

        // BEGIN: com.azure.messaging.eventhubs.eventprocessorclient.startstop
        eventProcessorClient.start();
        // Continue to perform other tasks while the processor is running in the background.
        eventProcessorClient.stop();
        // END: com.azure.messaging.eventhubs.eventprocessorclient.startstop
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
