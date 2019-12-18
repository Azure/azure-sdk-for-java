// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import reactor.core.Exceptions;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Demonstrates how to continuously publish events using {@link EventHubProducerAsyncClient}.
 */
public class PublishStreamOfEvents {
    private static final Duration OPERATION_TIMEOUT = Duration.ofSeconds(30);

    /**
     * Main method to invoke this demo about how continuously publish a stream of events to Event Hubs.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // The connection string value can be obtained by:
        // 1. Going to your Event Hubs namespace in Azure Portal.
        // 2. Creating an Event Hub instance.
        // 3. Creating a "Shared access policy" for your Event Hub instance.
        // 4. Copying the connection string from the policy's properties.
        String connectionString = "Endpoint={endpoint};SharedAccessKeyName={sharedAccessKeyName};" +
            "SharedAccessKey={sharedAccessKey};EntityPath={eventHubName}";

        MyPublisher publisher = new MyPublisher(connectionString);

        // We will publish three events based on simple sentences.
        Flux<EventData> events = Flux.just(
            new EventData("This is the first event.".getBytes(UTF_8)),
            new EventData("This is the second event.".getBytes(UTF_8)),
            new EventData("This is the third event.".getBytes(UTF_8)));

        publisher.publish(events);

        // We try to add as many events as a batch can fit based on the event size and send to Event Hub when
        // the batch can hold no more events. Create a new batch for next set of events and repeat until all events
        // are sent.
        events.flatMap(event -> {
            final EventDataBatch batch = currentBatch.get();
            if (batch.tryAdd(event)) {
                return Mono.empty();
            }

            // The batch is full, so we create a new batch and send the batch. Mono.when completes when both operations
            // have completed.
            return Mono.when(
                producer.send(batch),
                producer.createBatch(options).map(newBatch -> {
                    currentBatch.set(newBatch);

                    // Add that event that we couldn't before.
                    if (!newBatch.tryAdd(event)) {
                        throw Exceptions.propagate(new IllegalArgumentException(String.format(
                            "Event is too large for an empty batch. Max size: %s. Event: %s",
                            newBatch.getMaxSizeInBytes(), event.getBodyAsString())));
                    }

                    return newBatch;
                }));
        }).then()
            .doFinally(signal -> {
                final EventDataBatch batch = currentBatch.getAndSet(null);
                if (batch != null) {
                    producer.send(batch).block(OPERATION_TIMEOUT);
                }
            })
            .subscribe(unused -> System.out.println("Complete"),
                error -> System.out.println("Error sending events: " + error),
                () -> System.out.println("Completed sending events."));

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete. Using .block() instead of
        // .subscribe() will turn this into a synchronous call.
        try {
            TimeUnit.SECONDS.sleep(5);
        } catch (InterruptedException ignored) {
        } finally {
            // Disposing of our producer.
            producer.close();
        }
    }

    static class MyPublisher implements AutoCloseable {
        private final EventHubProducerAsyncClient producer;
        private final AtomicBoolean isDisposed = new AtomicBoolean();
        private final Object lock = new Object();
        private final AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>();

        MyPublisher(String connectionString) {
            producer = new EventHubClientBuilder()
                .connectionString(connectionString)
                .buildAsyncProducerClient();
        }

        /**
         * Adds an event to the current batch. When the batch is full, then the batch is sent to Event Hubs.
         *
         * @param event Event to publish to the service.
         *
         * @return Mono that completes or errors when the event is added to the service.
         */
        Mono<Void> publish(EventData event) {
            return getOrCreateBatch().flatMap(batch -> {
                if (batch.tryAdd(event)) {
                    return Mono.empty();
                }

                return Mono.when(
                    producer.send(batch),
                    createBatch().flatMap(newBatch -> {
                        if (!newBatch.tryAdd(event)) {
                            throw Exceptions.propagate(new IllegalArgumentException(String.format(
                                "Event is too large for an empty batch. Max size: %s. Event: %s",
                                newBatch.getMaxSizeInBytes(), event.getBodyAsString())));
                        }

                        return Mono.<Void>empty();
                    }));
            });
        }

        /**
         * Subscribes to a stream of events and publishes event batches when:
         *
         * <ul>
         * <li>The {@link EventDataBatch} is full.</li>
         * <li>Timeout window has elapsed and there is a batch.</li>
         * </ul>
         *
         * @param events Events to publish to the service.
         *
         * @return Mono that completes when all the events have been published.
         */
        Mono<Void> publish(Flux<EventData> events) {
            final Flux<EventDataBatch> fullBatchFlux = Flux.create(sink -> {
                events.subscribe(event -> {
                    // For each event, tries to add it to the current batch. If the current batch is full, then we emit
                    // that. Otherwise an empty, completed, Mono is emitted.
                    getOrCreateBatch()
                        .flatMap(batch -> batch.tryAdd(event) ? Mono.empty() : Mono.just(batch))
                        .flatMap(fullBatch -> {
                            sink.next(fullBatch);

                            return createBatch().map(newBatch -> {
                                if (!newBatch.tryAdd(event)) {
                                    sink.error(new IllegalArgumentException(String.format(
                                        "Event is too large for an empty batch. Max size: %s. Event: %s",
                                        newBatch.getMaxSizeInBytes(), event.getBodyAsString())));
                                }
                                return newBatch;
                            });
                        }).block();
                }, error -> {
                    sink.error(new RuntimeException("Error fetching next event.", error));
                }, () -> {
                    final EventDataBatch lastBatch = currentBatch.getAndSet(null);
                    if (lastBatch != null) {
                        sink.next(lastBatch);
                    }

                    sink.complete();
                });
            });

            final Duration windowPeriod = Duration.ofSeconds(1);
            final Flux<EventDataBatch> emitAtIntervals = Flux.interval(windowPeriod)
                .takeUntilOther(fullBatchFlux.then())
                .flatMap(v -> {
                    final EventDataBatch batch = currentBatch.getAndSet(null);
                    return batch != null ? Mono.just(batch) : Mono.empty();
                });

            return Flux.merge(fullBatchFlux, emitAtIntervals)
                .flatMap(batchToSend -> producer.send(batchToSend))
                .then();
        }

        private Mono<EventDataBatch> getOrCreateBatch() {
            final EventDataBatch current = currentBatch.get();
            return current != null
                ? Mono.just(current)
                : createBatch();
        }

        private Mono<EventDataBatch> createBatch() {
            return producer.createBatch().map(batch -> {
                currentBatch.set(batch);
                return batch;
            });
        }

        public void close() {
            if (isDisposed.getAndSet(true)) {
                return;
            }

            producer.close();
        }
    }
}
