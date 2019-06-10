// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.implementation.logging.ServiceLogger;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
public class EventSender {
    /**
     * The default maximum allowable size, in bytes, for a batch to be sent.
     */
    public static final int MAX_MESSAGE_LENGTH_BYTES = 256 * 1024;

    private static final SenderOptions DEFAULT_OPTIONS = new SenderOptions();
    private static final EventBatchingOptions DEFAULT_BATCHING_OPTIONS = new EventBatchingOptions();

    private final ServiceLogger logger = new ServiceLogger(EventSender.class);

    //TODO (conniey): Remove this after I verify it all works.
    private final AtomicInteger number = new AtomicInteger(0);
    private final AtomicInteger totalEvents = new AtomicInteger(0);
    private final SenderOptions senderOptions;

    /**
     * Creates a new instance of the EventSender.
     */
    EventSender() {
        this(DEFAULT_OPTIONS);
    }

    /**
     * Creates a new instance of this EventSender with batches that are {@code maxMessageSize} and sends messages to {
     *
     * @code partitionId}.
     */
    EventSender(SenderOptions options) {
        Objects.requireNonNull(options);

        this.senderOptions = options;
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     *
     * @param events Events to send to the service.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Iterable<EventData> events) {
        Objects.requireNonNull(events);

        return send(Flux.fromIterable(events));
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     *
     * @param events Events to send to the service.
     * @param options The set of options to consider when sending this batch.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Iterable<EventData> events, EventBatchingOptions options) {
        Objects.requireNonNull(events);

        return send(Flux.fromIterable(events), options);
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     *
     * @param events Events to send to the service.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Publisher<EventData> events) {
        Objects.requireNonNull(events);

        return sendInternal(Flux.from(events), DEFAULT_BATCHING_OPTIONS);
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     *
     * @param events Events to send to the service.
     * @param options The set of options to consider when sending this batch.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Publisher<EventData> events, EventBatchingOptions options) {
        Objects.requireNonNull(events);
        Objects.requireNonNull(options);

        return sendInternal(Flux.from(events), options);
    }

    private Mono<Void> sendInternal(Flux<EventData> events, EventBatchingOptions options) {
        final String partitionId = senderOptions.partitionId();

        //TODO (conniey): When we implement partial success, update the maximum number of batches or remove it completely.
        return events.collect(new EventDataCollector(options, 1))
            .flatMap(list -> sendBatch(partitionId, Flux.fromIterable(list)));
    }

    private Mono<Void> sendBatch(String partitionId, Flux<EventDataBatch> eventBatches) {
        return eventBatches
            .flatMap(batch -> sendBatch(partitionId, batch))
            .doOnError(error -> {
                logger.asError().log(error.toString());
            }).doOnComplete(() -> {
                logger.asInfo().log(String.format("TOTAL BATCHES: %s. EVENTS: %s", number.get(), totalEvents.get()));
            }).then();
    }

    //TODO (conniey): Add implementation to push through proton-j link.
    private Mono<Void> sendBatch(String partitionId, EventDataBatch batch) {
        number.incrementAndGet();
        final int totals = totalEvents.addAndGet(batch.getSize());
        logger.asWarning().log(String.format("[%s], size: %s, total: %s", batch.getPartitionKey(), batch.getSize(), totals));

        return Mono.empty();
    }

    /*
     * Collects EventData into EventDataBatch to send to Event Hubs. If maxNumberOfBatches is null then it'll collect as
     * many batches as possible. Otherwise, if there are more events than can fit into maxNumberOfBatches, then the
     * collector throws a PayloadSizeExceededException.
     */
    private static class EventDataCollector implements Collector<EventData, List<EventDataBatch>, List<EventDataBatch>> {
        private final String batchLabel;
        private final int maxMessageSize;
        private final Integer maxNumberOfBatches;
        private volatile EventDataBatch currentBatch;

        EventDataCollector(EventBatchingOptions options, Integer maxNumberOfBatches) {
            this.maxNumberOfBatches = maxNumberOfBatches;
            this.maxMessageSize = options.maximumSizeInBytes();
            this.batchLabel = options.batchLabel();

            currentBatch = new EventDataBatch(options.maximumSizeInBytes(), options.batchLabel());
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

                if (maxNumberOfBatches != null && list.size() == maxNumberOfBatches) {
                    throw new AmqpException(false, ErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, String.format(Locale.US,
                        "EventData does not fit into maximum number of batches. '%s'", maxNumberOfBatches));
                }

                currentBatch = new EventDataBatch(maxMessageSize, batchLabel);
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
