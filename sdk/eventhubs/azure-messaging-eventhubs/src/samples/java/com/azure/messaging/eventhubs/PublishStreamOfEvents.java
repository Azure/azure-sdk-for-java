// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.eventhubs;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Demonstrates how to continuously publish events using {@link EventHubProducerAsyncClient}. {@link CustomPublisher} is
 * publishes size-limited batches when they are full. In addition, it checks for periodically for a partial batch that
 * needs to be sent.
 */
public class PublishStreamOfEvents {
    private static final String EVENT_NUMBER = "EVENT_NUMBER";

    /**
     * Main method to invoke this demo about how continuously publish a stream of events to Event Hubs.
     *
     * @param args Unused arguments to the program.
     */
    public static void main(String[] args) {
        // Limit the size of the batches to 1024 bytes instead of using the default max size.
        final CreateBatchOptions batchOptions = new CreateBatchOptions().setMaximumSizeInBytes(1024);

        // The credential used is DefaultAzureCredential because it combines commonly used credentials
        // in deployment and development and chooses the credential to used based on its running environment.
        // More information can be found at: https://learn.microsoft.com/java/api/overview/azure/identity-readme
        final TokenCredential credential = new DefaultAzureCredentialBuilder()
            .build();
        final String fullyQualifiedNamespace = "<<fully-qualified-namespace>>";
        final String eventHubName = "<<event-hub-name>>";

        // "<<fully-qualified-namespace>>" will look similar to "{your-namespace}.servicebus.windows.net"
        // "<<event-hub-name>>" will be the name of the Event Hub instance you created inside the Event Hubs namespace.
        final CustomPublisher publisher = new CustomPublisher(fullyQualifiedNamespace, eventHubName, credential,
            Duration.ofSeconds(1), batchOptions);

        // This represents a stream of events that we want to publish.
        final Sinks.Many<EventData> events = Sinks.many().multicast().onBackpressureBuffer();

        System.out.println("Publishing events...");
        publisher.publish(events.asFlux()).subscribe(unused -> System.out.println("Completed."),
            error -> System.err.println("Error sending events: " + error),
            () -> System.out.println("Completed sending events."));

        emitEvents(events);

        // The .subscribe() creation and assignment is not a blocking call. For the purpose of this example, we sleep
        // the thread so the program does not end before the send operation is complete.
        try {
            TimeUnit.SECONDS.sleep(10);
        } catch (InterruptedException ignored) {
        } finally {
            System.out.println("Closing custom publisher.");
            publisher.close();
        }
    }

    /**
     * Helper function that emits 50 events. The interval between each event is randomly selected between 0 - 250ms and
     * is a random substring of the lorem ipsum text.
     *
     * @param events Sink for generated events.
     */
    private static void emitEvents(Sinks.Many<EventData> events) {
        final String contents = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do "
            + "eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis "
            + "nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure "
            + "dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur"
            + " sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est "
            + "laborum.";
        final Random random = new Random();

        for (int i = 0; i < 50; i++) {
            int milliseconds = random.nextInt(250);

            try {
                TimeUnit.MILLISECONDS.sleep(milliseconds);
            } catch (InterruptedException ignored) {
            }

            final int size = random.nextInt(contents.length());
            final int endIndex = size < 1 ? contents.length() : size;
            final EventData event = new EventData(contents.substring(0, endIndex));
            event.getProperties().put(EVENT_NUMBER, String.valueOf(i));

            events.emitNext(event, Sinks.EmitFailureHandler.FAIL_FAST);
        }

        events.emitComplete(Sinks.EmitFailureHandler.FAIL_FAST);
    }

    /**
     * Aggregates events into batches based on the given {@link CreateBatchOptions} and sends the batches to the Event
     * Hubs service when either:
     *
     * <ul>
     *     <li>The batch has reached its max size and no more events can be added to it.</li>
     *     <li>The timeout window has elapsed.</li>
     * </ul>
     */
    private static class CustomPublisher implements AutoCloseable {
        private final Logger logger = LoggerFactory.getLogger(CustomPublisher.class);
        private final AtomicBoolean isDisposed = new AtomicBoolean();
        private final AtomicReference<EventDataBatch> currentBatch = new AtomicReference<>();
        private final EventHubProducerAsyncClient producer;
        private final CreateBatchOptions batchOptions;
        private final Duration windowDuration;

        /**
         * Creates a new instance of {@link CustomPublisher}.
         *
         * @param fullyQualifiedNamespace Fully qualified namespace. Will look similar to:
         *     "{your-namespace}.servicebus.windows.net"
         * @param eventHubName Event Hub name
         * @param tokenCredential Credential to authorize with Event Hubs.
         * @param windowDuration Intervals to check for an available {@link EventDataBatch} to send.
         * @param batchOptions Options to use when creating the {@link EventDataBatch}. If {@code null}, the default
         *     batch options are used.
         */
        CustomPublisher(String fullyQualifiedNamespace, String eventHubName, TokenCredential tokenCredential,
            Duration windowDuration, CreateBatchOptions batchOptions) {

            // Create a producer.
            producer = new EventHubClientBuilder()
                .credential(fullyQualifiedNamespace, eventHubName, tokenCredential)
                .buildAsyncProducerClient();

            this.batchOptions = batchOptions != null ? batchOptions : new CreateBatchOptions();
            this.windowDuration = Objects.requireNonNull(windowDuration, "'windowDuration' cannot be null.");
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
            final Flux<EventDataBatch> fullBatchFlux = Flux.<EventDataBatch>create(sink -> {
                events.subscribe(event -> {
                    // For each event, tries to add it to the current batch. If the current batch is full, then we emit
                    // that. Otherwise an empty Mono is emitted.
                    // Blocking at the very end of the method to ensure that the event is added to a batch before
                    // requesting another event.
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
            }).publish().autoConnect();

            // Periodically checks to see if there is an available, not full, batch to send. If there is, it sends that
            // batch. It keeps checking for an available batch until the last event in `events` has been published.
            final Flux<EventDataBatch> emitAtIntervals = Flux.interval(windowDuration)
                .takeUntilOther(events.then())
                .flatMap(v -> {
                    final EventDataBatch batch = currentBatch.getAndSet(null);
                    logger.info("Interval check. Has items? {}", batch != null && batch.getCount() > 0);
                    return batch != null ? Mono.just(batch) : Mono.empty();
                });

            // Merge the two fluxes together so the results
            return Flux.merge(fullBatchFlux, emitAtIntervals)
                .flatMap(batchToSend -> {
                    final String eventNumbers = batchToSend.getEvents()
                        .stream()
                        .map(e -> e.getProperties().getOrDefault(EVENT_NUMBER, "n/a").toString())
                        .collect(Collectors.joining(", "));

                    logger.info("Sending batch with {} events. Size: {} bytes. Event numbers in batch [{}]",
                        batchToSend.getCount(), batchToSend.getSizeInBytes(), eventNumbers);

                    return producer.send(batchToSend);
                })
                .then();
        }

        private Mono<EventDataBatch> getOrCreateBatch() {
            final EventDataBatch current = currentBatch.get();
            return current != null
                ? Mono.just(current)
                : createBatch();
        }

        private Mono<EventDataBatch> createBatch() {
            return producer.createBatch(batchOptions).map(batch -> {
                currentBatch.set(batch);
                return batch;
            });
        }

        public void close() {
            if (isDisposed.getAndSet(true)) {
                return;
            }

            final EventDataBatch batch = currentBatch.getAndSet(null);
            if (batch != null) {
                producer.send(batch).block(Duration.ofSeconds(30));
            }

            producer.close();
        }
    }
}
