// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Contains code snippets when generating javadocs through doclets for {@link EventHubProducerAsyncClient}.
 */
public class EventHubProducerAsyncClientJavaDocCodeSamples {
    private final EventHubClientBuilder builder = new EventHubClientBuilder();

    /**
     * Code snippet demonstrating how to create an {@link EventHubProducerAsyncClient} that automatically routes events to any
     * partition.
     *
     */
    public void instantiate() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncproducerclient.instantiation
        // The required parameter is a way to authenticate with Event Hubs using credentials.
        // The connectionString provides a way to authenticate with Event Hub.
        EventHubProducerAsyncClient producer = new EventHubClientBuilder()
            .connectionString(
                "Endpoint={fully-qualified-namespace}/;SharedAccessKeyName={policy-name};SharedAccessKey={key}",
                "event-hub-name")
            .buildAsyncProducerClient();
        // END: com.azure.messaging.eventhubs.eventhubasyncproducerclient.instantiation

        producer.close();
    }

    /**
     * Code snippet demonstrating how to create an {@link EventHubProducerAsyncClient} that routes events to a single
     * partition.
     *
     */
    public void instantiatePartitionProducer() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncproducerclient.instantiation#partitionId
        EventData eventData = new EventData("data-to-partition-foo");
        SendOptions options = new SendOptions()
            .setPartitionId("foo");

        EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        producer.send(eventData, options);
        // END: com.azure.messaging.eventhubs.eventhubasyncproducerclient.instantiation#partitionId

        producer.close();
    }

    /**
     * Code snippet demonstrating how to send events with a partition key.
     */
    public void sendEventsFluxSendOptions() {
        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncproducerclient.send#publisher-sendOptions
        Flux<EventData> events = Flux.just(
            new EventData("sourdough".getBytes(UTF_8)),
            new EventData("rye".getBytes(UTF_8)),
            new EventData("wheat".getBytes(UTF_8))
        );

        EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();
        SendOptions options = new SendOptions()
            .setPartitionKey("bread");

        producer.send(events, options).subscribe(ignored -> System.out.println("sent"),
            error -> System.err.println("Error received:" + error),
            () -> System.out.println("Send complete."));
        // END: com.azure.messaging.eventhubs.eventhubasyncproducerclient.send#publisher-sendOptions
    }

    /**
     * Code snippet demonstrating how to create an {@link EventDataBatch} and send it.
     */
    public void sendEventDataBatch() {
        final EventHubProducerAsyncClient producer = builder.buildAsyncProducerClient();

        // BEGIN: com.azure.messaging.eventhubs.eventhubasyncproducerclient.send#eventDataBatch
        final EventData firstEvent = new EventData("92".getBytes(UTF_8));
        firstEvent.getProperties().put("telemetry", "latency");

        final EventData secondEvent = new EventData("98".getBytes(UTF_8));
        secondEvent.getProperties().put("telemetry", "cpu-temperature");

        final EventData thirdEvent = new EventData("120".getBytes(UTF_8));
        thirdEvent.getProperties().put("telemetry", "fps");

        final Flux<EventData> telemetryEvents = Flux.just(firstEvent, secondEvent, thirdEvent);

        final CreateBatchOptions options = new CreateBatchOptions()
            .setPartitionKey("telemetry")
            .setMaximumSizeInBytes(256);
        final AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>(
            producer.createBatch(options).block());

        // The sample Flux contains three events, but it could be an infinite stream of telemetry events.
        telemetryEvents.subscribe(event -> {
            final EventDataBatch batch = currentBatch.get();
            if (!batch.tryAdd(event)) {
                producer.createBatch(options).map(newBatch -> {
                    currentBatch.set(newBatch);
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
        // END: com.azure.messaging.eventhubs.eventhubasyncproducerclient.send#eventDataBatch
    }
}
