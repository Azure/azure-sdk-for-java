// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Contains code snippets when generating javadocs through doclets for {@link EventHubProducerAsyncClient}.
 */
public class EventHubProducerAsyncClientJavaDocCodeSamples {
    private final EventHubClientBuilder builder = new EventHubClientBuilder();

    /**
     * Code snippet demonstrating how to create an {@link EventHubProducerAsyncClient}.
     */
    public void instantiate() {
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
    public void batchAutomaticRouting() {
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
    public void batchPartitionId() {
        EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-partitionId
        // Creating a batch with partitionId set will route all events in that batch to partition `foo`.
        CreateBatchOptions options = new CreateBatchOptions().setPartitionId("foo");
        producer.createBatch(options).flatMap(batch -> {
            batch.tryAdd(new EventData("test-event-1"));
            batch.tryAdd(new EventData("test-event-2"));
            return producer.send(batch);
        }).subscribe(unused -> {
        },
            error -> System.err.println("Error occurred while sending batch:" + error),
            () -> System.out.println("Send complete."));
        // END: com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-partitionId

        producer.close();
    }

    /**
     * Code snippet demonstrating how to send events with a partition key.
     */
    public void batchPartitionKey() {
        EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-partitionKey
        // Creating a batch with partitionKey set will tell the service to hash the partitionKey and decide which
        // partition to send the events to. Events with the same partitionKey are always routed to the same partition.
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
    public void batchSizeLimited() {
        final EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        final EventData firstEvent = new EventData("92".getBytes(UTF_8));
        firstEvent.getProperties().put("telemetry", "latency");
        final EventData secondEvent = new EventData("98".getBytes(UTF_8));
        secondEvent.getProperties().put("telemetry", "cpu-temperature");

        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-int
        final Flux<EventData> telemetryEvents = Flux.just(firstEvent, secondEvent);

        // Setting `setMaximumSizeInBytes` when creating a batch, limits the size of that batch.
        // In this case, all the batches created with these options are limited to 256 bytes.
        final CreateBatchOptions options = new CreateBatchOptions()
            .setMaximumSizeInBytes(256);
        final AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>(
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
}
