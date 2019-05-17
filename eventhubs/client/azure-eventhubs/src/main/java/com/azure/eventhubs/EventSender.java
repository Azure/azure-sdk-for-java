// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.eventhubs.implementation.ClientConstants;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * This sender class is a logical representation of sending events to Event Hubs.
 *
 * @see EventHubClient#createSender()
 */
public class EventSender implements AutoCloseable {
    private static final String EMPTY_PARTITION_KEY = "";
    private static final Duration WINDOW_DURATION = Duration.ofMillis(1000);
    private static final int WINDOW_SIZE = 100;

    private final ServiceLogger logger = new ServiceLogger(EventSender.class);
    private final int maxMessageSize;

    //TODO (conniey): Remove this after I verify it all works.
    private final AtomicInteger number = new AtomicInteger(0);
    private final AtomicInteger totalEvents = new AtomicInteger(0);

    /**
     * Creates a new instance of the EventSender.
     */
    EventSender() {
        this(ClientConstants.MAX_MESSAGE_LENGTH_BYTES);
    }

    /**
     * Creates a new instance of this object with batches that are {@code maxMessageSize}.
     *
     * @param maxMessageSize Message size for each batch.
     */
    EventSender(int maxMessageSize) {
        this.maxMessageSize = maxMessageSize;
    }

    /**
     * Sends the {@code events} to the Event Hubs service. If {@link EventData#partitionKey()} is specified, the events
     * are grouped by that key and sent to the service in batches.
     *
     * @param events Events to send to the service.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(EventData... events) {
        Objects.requireNonNull(events);

        if (events.length == 0) {
            return Mono.empty();
        }

        return send(Flux.just(events));
    }

    /**
     * Sends the {@code events} to the specified Event Hubs {@code partitionId}. The value of
     * {@link EventData#partitionKey()} is ignored. Events always go to the specified {@code partitionId}.
     *
     * @param partitionId Event Hubs partition to send the events to.
     * @param events Events to send to the service.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(String partitionId, EventData... events) {
        Objects.requireNonNull(partitionId);
        Objects.requireNonNull(events);

        if (events.length == 0) {
            return Mono.empty();
        }

        return send(partitionId, Flux.just(events));
    }

    /**
     * Sends the {@code events} to the Event Hubs service. If {@link EventData#partitionKey()} is specified, the events
     * are grouped by that key and sent to the service in batches.
     *
     * @param events Events to send to the service.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Iterable<EventData> events) {
        Objects.requireNonNull(events);

        return send(Flux.fromIterable(events));
    }

    /**
     * Sends the {@code events} to the specified Event Hubs {@code partitionId}. The value of
     * {@link EventData#partitionKey()} is ignored. Events always go to the specified {@code partitionId}.
     *
     * @param partitionId Event Hubs partition to send the events to.
     * @param events Events to send to the service.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(String partitionId, Iterable<EventData> events) {
        Objects.requireNonNull(partitionId);
        Objects.requireNonNull(events);

        return send(partitionId, Flux.fromIterable(events));
    }

    /**
     * Sends the {@code events} to the Event Hubs service. If {@link EventData#partitionKey()} is specified, the events
     * are grouped by that key and sent to the service in batches.
     *
     * @param events Events to send to the service.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Publisher<EventData> events) {
        Objects.requireNonNull(events);

        return sendInternal(null, Flux.defer(() -> events));
    }

    /**
     * Sends the {@code events} to the specified Event Hubs {@code partitionId}. The value of
     * {@link EventData#partitionKey()} is ignored. Events always go to the specified {@code partitionId}.
     *
     * @param partitionId Event Hubs partition to send the events to.
     * @param events Events to send to the service.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     * @throws NullPointerException if {@code partitionId} or {@code events} are null.
     */
    public Mono<Void> send(String partitionId, Publisher<EventData> events) {
        Objects.requireNonNull(partitionId);
        Objects.requireNonNull(events);

        return sendInternal(partitionId, Flux.defer(() -> events));
    }

    private Mono<Void> sendInternal(String partitionId, Flux<EventData> events) {
        final Flux<EventDataBatch> batches = Flux.defer(() -> events)
            .windowTimeout(WINDOW_SIZE, WINDOW_DURATION)
            .flatMap(flux -> flux.map(event -> {
                // If the user has specified that they want all events to go to a particular partition, we overwrite
                // any partitionKey on that event. This is probably non-null due to user error.
                if (!ImplUtils.isNullOrEmpty(partitionId) && !ImplUtils.isNullOrEmpty(event.partitionKey())) {
                    logger.asInformational().log(
                        "EventData has partitionKey '%s' set and user has specified partitionId '%s'. Event will be"
                            + " sent to the given partitionId.",
                        event.partitionKey(), partitionId);

                    event.partitionKey(null);
                }

                return event;
            }).groupBy(event -> {
                // It is OK to group by this because when partitionId is not null, partitionKey is always empty.
                return event.partitionKey() != null ? event.partitionKey() : EMPTY_PARTITION_KEY;
            })).flatMap(group -> {
                EventDataCollector collector;

                if (!ImplUtils.isNullOrEmpty(partitionId) || EMPTY_PARTITION_KEY.equals(group.key())) {
                    collector = new EventDataCollector(maxMessageSize);
                } else {
                    collector = new EventDataCollector(maxMessageSize, group.key());
                }

                return group.collect(collector);
            }).flatMap(Flux::fromIterable);

        return sendBatch(partitionId, batches);
    }

    private Mono<Void> sendBatch(String partitionId, Flux<EventDataBatch> eventBatches) {
        return eventBatches
            .flatMap(batch -> sendBatch(partitionId, batch))
            .doOnError(error -> {
                logger.asError().log(error.toString());
            }).doOnComplete(() -> {
                logger.asInformational().log(String.format("TOTAL BATCHES: %s. EVENTS: %s", number.get(), totalEvents.get()));
            }).then();
    }

    //TODO (conniey): Add implementation to push through proton-j link.
    private Mono<Void> sendBatch(String partitionId, EventDataBatch batch) {
        number.incrementAndGet();
        final int totals = totalEvents.addAndGet(batch.size());
        logger.asWarning().log(String.format("[%s], size: %s, total: %s", batch.partitionKey(), batch.size(), totals));

        return Mono.empty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {

    }

    /*
     * Collects {@link EventData} into {@link EventDataBatch} to send to Event Hubs.
     */
    private static class EventDataCollector implements Collector<EventData, List<EventDataBatch>, List<EventDataBatch>> {
        private final String partitionKey;
        private final int maxMessageSize;
        private volatile EventDataBatch currentBatch;

        EventDataCollector(int maxMessageSize) {
            this(maxMessageSize, null);
        }

        EventDataCollector(int maxMessageSize, String partitionKey) {
            this.partitionKey = partitionKey;
            this.maxMessageSize = maxMessageSize;
            currentBatch = new EventDataBatch(maxMessageSize, partitionKey);
        }

        @Override
        public Supplier<List<EventDataBatch>> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<List<EventDataBatch>, EventData> accumulator() {
            return (list, event) -> {
                EventDataBatch batch = currentBatch;
                if (batch.tryAdd(event)) {
                    return;
                }

                currentBatch = new EventDataBatch(maxMessageSize, partitionKey);
                currentBatch.tryAdd(event);
                list.add(batch);
            };
        }

        @Override
        public BinaryOperator<List<EventDataBatch>> combiner() {
            return (existing, another) -> {
                existing.addAll(another);
                return existing;
            };
        }

        @Override
        public Function<List<EventDataBatch>, List<EventDataBatch>> finisher() {
            return list -> {
                EventDataBatch batch = currentBatch;
                currentBatch = null;

                if (batch != null) {
                    list.add(batch);
                }

                return list;
            };
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.emptySet();
        }
    }
}
