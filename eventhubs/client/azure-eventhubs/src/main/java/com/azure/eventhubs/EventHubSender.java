// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.implementation.logging.ServiceLogger;
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
public class EventHubSender implements AutoCloseable {
    private static final String EMPTY_PARTITION_KEY = "";

    private final ServiceLogger logger = new ServiceLogger(EventHubSender.class);
    private final int maxMessageSize;

    /**
     * Creates a new instance of the EventHubSender.
     */
    EventHubSender() {
        this(ClientConstants.MAX_MESSAGE_LENGTH_BYTES);
    }

    /**
     * Creates a new instance of this object with batches that are {@code maxMessageSize}.
     *
     * @param maxMessageSize Message size for each batch.
     */
    EventHubSender(int maxMessageSize) {
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
        if (events == null || events.length == 0) {
            return Mono.empty();
        }

        return send(Flux.just(events));
    }

    /**
     * Sends the {@code events} to the specified Event Hubs {@code partitionId}. If {@link EventData#partitionKey()} is
     * specified, these are dropped from being sent.
     *
     * @param partitionId Event Hubs partition to send the events to.
     * @param events Events to send to the service.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(String partitionId, EventData... events) {
        if (events == null || events.length == 0) {
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
        if (events == null) {
            return Mono.empty();
        }

        return send(Flux.fromIterable(events));
    }

    /**
     * Sends the {@code events} to the specified Event Hubs {@code partitionId}. If {@link EventData#partitionKey()} is
     * specified, these are dropped from being sent.
     *
     * @param partitionId Event Hubs partition to send the events to.
     * @param events Events to send to the service.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(String partitionId, Iterable<EventData> events) {
        if (events == null) {
            return Mono.empty();
        }

        return send(partitionId, Flux.fromIterable(events));
    }


    /**
     * Sends the {@code events} to the specified Event Hubs {@code partitionId}. If {@link EventData#partitionKey()} is
     * specified, these are dropped from being sent.
     *
     * @param events Events to send to the service.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Publisher<EventData> events) {
        final AtomicInteger number = new AtomicInteger(0);
        final AtomicInteger totalEvents = new AtomicInteger(0);

        return Flux.defer(() -> events)
            .windowTimeout(100, Duration.ofMillis(500))
            .flatMap(flux -> flux.groupBy(event -> event.partitionKey() != null ? event.partitionKey() : EMPTY_PARTITION_KEY))
            .flatMap(group -> {
                final EventDataCollector eventDataCollector = group.key() == EMPTY_PARTITION_KEY
                    ? new EventDataCollector(maxMessageSize, null)
                    : new EventDataCollector(maxMessageSize, group.key());

                return group.collect(eventDataCollector);
            })
            .doOnNext(batchList -> {
                batchList.forEach(batch -> {
                    number.incrementAndGet();
                    final int totals = totalEvents.addAndGet(batch.size());
                    logger.asWarning().log(String.format("[%s], size: %s, total: %s", batch.partitionKey(), batch.size(), totals));
                });
            })
            .doOnError(error -> {
                logger.asError().log(error.toString());
            })
            .doOnComplete(() -> {
                logger.asInformational().log(String.format("TOTAL BATCHES: %s. EVENTS: %s", number.get(), totalEvents.get()));
            })
            .then();
    }

    /**
     * Sends the {@code events} to the specified Event Hubs {@code partitionId}. If {@link EventData#partitionKey()} is
     * specified, these are dropped from being sent.
     *
     * @param partitionId Event Hubs partition to send the events to.
     * @param events Events to send to the service.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(String partitionId, Publisher<EventData> events) {
        Objects.requireNonNull(partitionId);
        Objects.requireNonNull(events);

        //TODO (conniey): What happens when someone does specify event.partitionKey()? Should I just drop them or send
        // them in another batch?
        final Flux<EventData> filtered = Flux.defer(() -> events)
            .filter(event -> event.partitionKey() == null);

        return send(partitionId, filtered);
    }

    private Mono<Void> send(String partitionId, Flux<EventData> events)  {
        final AtomicInteger number = new AtomicInteger(0);
        final AtomicInteger totalEvents = new AtomicInteger(0);

        return Flux.defer(() -> events)
            .windowTimeout(100, Duration.ofMillis(500))
            .flatMap(flux -> flux.groupBy(event -> {
                if (partitionId != null) {
                    return partitionId;
                }

                return event.partitionKey() != null ? event.partitionKey() : EMPTY_PARTITION_KEY;
            }))
            .flatMap(group -> {
                final EventDataCollector eventDataCollector = EMPTY_PARTITION_KEY.equals(group.key())
                    ? new EventDataCollector(maxMessageSize, null)
                    : new EventDataCollector(maxMessageSize, group.key());

                return group.collect(eventDataCollector);
            })
            //TODO (conniey): Emit contents into Event Hubs.
            .doOnNext(batchList -> {
                batchList.forEach(batch -> {
                    number.incrementAndGet();
                    final int totals = totalEvents.addAndGet(batch.size());
                    logger.asWarning().log(String.format("[%s], size: %s, total: %s", batch.partitionKey(), batch.size(), totals));
                });
            })
            .doOnError(error -> {
                logger.asError().log(error.toString());
            })
            .doOnComplete(() -> {
                logger.asInformational().log(String.format("TOTAL BATCHES: %s. EVENTS: %s", number.get(), totalEvents.get()));
            })
            .then();
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
