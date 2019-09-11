// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.implementation.annotation.Immutable;
import com.azure.core.implementation.tracing.ProcessKind;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.eventhubs.implementation.AmqpSendLink;
import com.azure.messaging.eventhubs.implementation.ErrorContextProvider;
import com.azure.messaging.eventhubs.implementation.EventDataUtil;
import com.azure.messaging.eventhubs.models.BatchOptions;
import com.azure.messaging.eventhubs.models.EventHubProducerOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static com.azure.core.implementation.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.implementation.tracing.Tracer.ENTITY_PATH;
import static com.azure.core.implementation.tracing.Tracer.HOST_NAME;
import static com.azure.core.implementation.tracing.Tracer.SPAN_CONTEXT;

/**
 * A producer responsible for transmitting {@link EventData} to a specific Event Hub, grouped together in batches.
 * Depending on the options specified at creation, the producer may be created to allow event data to be automatically
 * routed to an available partition or specific to a partition.
 *
 * <p>
 * Allowing automatic routing of partitions is recommended when:
 * <ul>
 * <li>The sending of events needs to be highly available.</li>
 * <li>The event data should be evenly distributed among all available partitions.</li>
 * </ul>
 * </p>
 *
 * <p>
 * If no partition is specified, the following rules are used for automatically selecting one:
 * <ol>
 * <li>Distribute the events equally amongst all available partitions using a round-robin approach.</li>
 * <li>If a partition becomes unavailable, the Event Hubs service will automatically detect it and forward the
 * message to another available partition.</li>
 * </ol>
 * </p>
 *
 * <p><strong>Create a producer that routes events to any partition</strong></p>
 * To allow automatic routing of messages to available partition, do not specify the {@link
 * EventHubProducerOptions#getPartitionId() partitionId} when creating the {@link EventHubAsyncProducer}.
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncproducer.instantiation}
 *
 * <p><strong>Create a producer that publishes events to partition "foo" with a timeout of 45 seconds.</strong></p>
 * Developers can push events to a single partition by specifying the
 * {@link EventHubProducerOptions#setPartitionId(String) partitionId} when creating an {@link EventHubAsyncProducer}.
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncproducer.instantiation#partitionId}
 *
 * <p><strong>Publish events to the same partition, grouped together using {@link SendOptions#setPartitionKey(String)}
 * .</strong></p>
 * If developers want to push similar events to end up at the same partition, but do not require them to go to a
 * specific partition, they can use {@link SendOptions#setPartitionKey(String)}.
 * <p>
 * In the sample below, all the "sandwiches" end up in the same partition, but it could end up in partition 0, 1, etc.
 * of the available partitions. All that matters to the end user is that they are grouped together.
 * </p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncproducer.send#publisher-sendOptions}
 *
 * <p><strong>Publish events using an {@link EventDataBatch}.</strong></p>
 * Developers can create an {@link EventDataBatch}, add the events they want into it, and publish these
 * events together. When creating a {@link EventDataBatch batch}, developers can specify a set of {@link BatchOptions
 * options} to configure this batch.
 * <p>
 * In the scenario below, the developer is creating a networked video game. They want to receive telemetry about their
 * users' gaming systems, but do not want to slow down the network with telemetry. So they limit the size of their
 * {@link EventDataBatch batches} to be no larger than 256 bytes. The events within the batch also get hashed to the
 * same partition because they all share the same {@link BatchOptions#getPartitionKey()}.
 * </p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncproducer.send#eventDataBatch}
 *
 * @see EventHubAsyncClient#createProducer()
 * @see EventHubAsyncClient#createProducer(EventHubProducerOptions)
 */
@Immutable
public class EventHubAsyncProducer implements Closeable {
    private static final int MAX_PARTITION_KEY_LENGTH = 128;

    /**
     * The default maximum allowable size, in bytes, for a batch to be sent.
     */
    public static final int MAX_MESSAGE_LENGTH_BYTES = 256 * 1024;

    private static final SendOptions DEFAULT_SEND_OPTIONS = new SendOptions();
    private static final BatchOptions DEFAULT_BATCH_OPTIONS = new BatchOptions();

    private final ClientLogger logger = new ClientLogger(EventHubAsyncProducer.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final EventHubProducerOptions senderOptions;
    private final Mono<AmqpSendLink> sendLinkMono;
    private final boolean isPartitionSender;
    private final TracerProvider tracerProvider;

    /**
     * Creates a new instance of this {@link EventHubAsyncProducer} that sends messages to {@link
     * EventHubProducerOptions#getPartitionId() options.partitionId()} if it is not {@code null} or an empty string,
     * otherwise, allows the service to load balance the messages amongst available partitions.
     */
    EventHubAsyncProducer(Mono<AmqpSendLink> amqpSendLinkMono, EventHubProducerOptions options,
                          TracerProvider tracerProvider) {
        // Caching the created link so we don't invoke another link creation.
        this.sendLinkMono = amqpSendLinkMono.cache();
        this.senderOptions = options;
        this.isPartitionSender = !ImplUtils.isNullOrEmpty(options.getPartitionId());
        this.tracerProvider = tracerProvider;
    }

    /**
     * Creates an {@link EventDataBatch} that can fit as many events as the transport allows.
     * @return A new {@link EventDataBatch} that can fit as many events as the transport allows.
     */
    public Mono<EventDataBatch> createBatch() {
        return createBatch(DEFAULT_BATCH_OPTIONS);
    }

    /**
     * Creates an {@link EventDataBatch} that can fit as many events as the transport allows.
     * @param options A set of options used to configure the {@link EventDataBatch}.
     * @return A new {@link EventDataBatch} that can fit as many events as the transport allows.
     */
    public Mono<EventDataBatch> createBatch(BatchOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");

        final BatchOptions clone = options.clone();

        verifyPartitionKey(clone.getPartitionKey());

        return sendLinkMono.flatMap(link -> link.getLinkSize()
            .flatMap(size -> {
                final int maximumLinkSize = size > 0
                    ? size
                    : MAX_MESSAGE_LENGTH_BYTES;

                if (clone.getMaximumSizeInBytes() > maximumLinkSize) {
                    return Mono.error(new IllegalArgumentException(String.format(Locale.US,
                        "BatchOptions.maximumSizeInBytes (%s bytes) is larger than the link size (%s bytes).",
                        clone.getMaximumSizeInBytes(), maximumLinkSize)));
                }

                final int batchSize = clone.getMaximumSizeInBytes() > 0
                    ? clone.getMaximumSizeInBytes()
                    : maximumLinkSize;

                return Mono.just(new EventDataBatch(batchSize, clone.getPartitionKey(), link::getErrorContext));
            }));
    }

    /**
     * Sends a single event to the associated Event Hub. If the size of the single event exceeds the maximum size
     * allowed, an exception will be triggered and the send will fail.

     * <p>
     * For more information regarding the maximum event size allowed, see
     * <a href="https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     *
     * @param event Event to send to the service.
     *
     * @return A {@link Mono} that completes when the event is pushed to the service.
     */
    public Mono<Void> send(EventData event) {
        Objects.requireNonNull(event, "'event' cannot be null.");

        return send(Flux.just(event));
    }

    /**
     * Sends a single event to the associated Event Hub with the send options. If the size of the single event exceeds
     * the maximum size allowed, an exception will be triggered and the send will fail.
     *
     * <p>
     * For more information regarding the maximum event size allowed, see
     * <a href="https://docs.microsoft.com/en-us/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     * @param event Event to send to the service.
     * @param options The set of options to consider when sending this event.
     *
     * @return A {@link Mono} that completes when the event is pushed to the service.
     */
    public Mono<Void> send(EventData event, SendOptions options) {
        Objects.requireNonNull(event, "'event' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");

        return send(Flux.just(event), options);
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     * @param events Events to send to the service.
     *
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Iterable<EventData> events) {
        Objects.requireNonNull(events, "'events' cannot be null.");

        return send(Flux.fromIterable(events));
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     * @param events Events to send to the service.
     * @param options The set of options to consider when sending this batch.
     *
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Iterable<EventData> events, SendOptions options) {
        Objects.requireNonNull(events, "'options' cannot be null.");

        return send(Flux.fromIterable(events), options);
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     * @param events Events to send to the service.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Flux<EventData> events) {
        Objects.requireNonNull(events, "'events' cannot be null.");

        return send(events, DEFAULT_SEND_OPTIONS);
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     * @param events Events to send to the service.
     * @param options The set of options to consider when sending this batch.
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Flux<EventData> events, SendOptions options) {
        Objects.requireNonNull(events, "'events' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");

        return sendInternal(events, options);
    }

    /**
     * Sends the batch to the associated Event Hub.
     *
     * @param batch The batch to send to the service.
     * @return A {@link Mono} that completes when the batch is pushed to the service.
     * @throws NullPointerException if {@code batch} is {@code null}.
     * @see EventHubAsyncProducer#createBatch()
     * @see EventHubAsyncProducer#createBatch(BatchOptions)
     */
    public Mono<Void> send(EventDataBatch batch) {
        Objects.requireNonNull(batch, "'batch' cannot be null.");

        if (batch.getEvents().isEmpty()) {
            logger.info("Cannot send an EventBatch that is empty.");
            return Mono.empty();
        }

        logger.info("Sending batch with partitionKey[{}], size[{}].", batch.getPartitionKey(), batch.getSize());

        final List<Message> messages = EventDataUtil.toAmqpMessage(batch.getPartitionKey(), batch.getEvents());

        return sendLinkMono.flatMap(link -> messages.size() == 1
            ? link.send(messages.get(0))
            : link.send(messages));
    }

    private Mono<Void> sendInternal(Flux<EventData> events, SendOptions options) {
        final String partitionKey = options.getPartitionKey();

        verifyPartitionKey(partitionKey);
        if (tracerProvider.isEnabled()) {
            return sendInternalTracingEnabled(events, partitionKey);
        } else {
            return sendInternalTracingDisabled(events, partitionKey);
        }
    }

    private Mono<Void> sendInternalTracingDisabled(Flux<EventData> events, String partitionKey) {
        return sendLinkMono.flatMap(link -> {
            return link.getLinkSize()
                .flatMap(size -> {
                    final int batchSize = size > 0 ? size : MAX_MESSAGE_LENGTH_BYTES;
                    final BatchOptions batchOptions = new BatchOptions()
                        .setPartitionKey(partitionKey)
                        .setMaximumSizeInBytes(batchSize);

                    return events.collect(new EventDataCollector(batchOptions, 1, link::getErrorContext));
                })
                .flatMap(list -> sendInternal(Flux.fromIterable(list)));
        });
    }

    private Mono<Void> sendInternalTracingEnabled(Flux<EventData> events, String partitionKey) {
        return sendLinkMono.flatMap(link -> {
            final AtomicReference<Context> sendSpanContext = new AtomicReference<>(Context.NONE);
            return link.getLinkSize()
                .flatMap(size -> {
                    final int batchSize = size > 0 ? size : MAX_MESSAGE_LENGTH_BYTES;
                    final BatchOptions batchOptions = new BatchOptions()
                        .setPartitionKey(partitionKey)
                        .setMaximumSizeInBytes(batchSize);

                    return events.map(eventData -> {
                        Context parentContext = eventData.getContext();
                        Context entityContext = parentContext.addData(ENTITY_PATH, link.getEntityPath());
                        sendSpanContext.set(tracerProvider
                            .startSpan(entityContext.addData(HOST_NAME, link.getHostname()), ProcessKind.SEND));
                        // add span context on event data
                        return setSpanContext(eventData, parentContext);
                    }).collect(new EventDataCollector(batchOptions, 1, link::getErrorContext));
                })
                .flatMap(list -> sendInternal(Flux.fromIterable(list)))
                .doOnEach(signal -> {
                    tracerProvider.endSpan(sendSpanContext.get(), signal);
                });
        });
    }

    private EventData setSpanContext(EventData event, Context parentContext) {
        Optional<Object> eventContextData = event.getContext().getData(SPAN_CONTEXT);
        if (eventContextData.isPresent()) {
            // if message has context (in case of retries), link it to the span
            Object spanContextObject = eventContextData.get();
            if (spanContextObject instanceof Context) {
                tracerProvider.addSpanLinks((Context) eventContextData.get());
                // TODO (samvaity): not supported in Opencensus yet
                // builder.addLink((Context)eventContextData.get());
            } else {
                logger.warning(String.format(Locale.US,
                    "Event Data context type is not of type Context, but type: %s. Not adding span links.",
                    spanContextObject != null ? spanContextObject.getClass() : "null"));
            }

            return event;
        } else {
            // Starting the span makes the sampling decision (nothing is logged at this time)
            Context eventSpanContext = tracerProvider.startSpan(parentContext, ProcessKind.RECEIVE);
            if (eventSpanContext != null) {
                Optional<Object> eventDiagnosticIdOptional = eventSpanContext.getData(DIAGNOSTIC_ID_KEY);

                if (eventDiagnosticIdOptional.isPresent()) {
                    event.addProperty(DIAGNOSTIC_ID_KEY, eventDiagnosticIdOptional.get().toString());
                    tracerProvider.endSpan(eventSpanContext, Signal.complete());
                    event.addContext(SPAN_CONTEXT, eventSpanContext);
                }
            }
        }
        return  event;
    }

    private Mono<Void> sendInternal(Flux<EventDataBatch> eventBatches) {
        return eventBatches
            .flatMap(this::send)
            .then()
            .doOnError(error -> {
                logger.error("Error sending batch.", error);
            });
    }

    private void verifyPartitionKey(String partitionKey) {
        if (ImplUtils.isNullOrEmpty(partitionKey)) {
            return;
        }

        if (isPartitionSender) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(Locale.US,
                "BatchOptions.partitionKey() cannot be set when an EventHubProducer is created with"
                    + "EventHubProducerOptions.partitionId() set. This EventHubProducer can only send events to "
                    + "partition '%s'.",
                senderOptions.getPartitionId())));
        } else if (partitionKey.length() > MAX_PARTITION_KEY_LENGTH) {
            throw logger.logExceptionAsError(new IllegalArgumentException(String.format(Locale.US,
                "PartitionKey '%s' exceeds the maximum allowed length: '%s'.", partitionKey,
                MAX_PARTITION_KEY_LENGTH)));
        }
    }

    /**
     * Disposes of the {@link EventHubAsyncProducer} by closing the underlying connection to the service.
     * @throws IOException if the underlying transport could not be closed and its resources could not be
     *     disposed.
     */
    @Override
    public void close() throws IOException {
        if (!isDisposed.getAndSet(true)) {
            final AmqpSendLink block = sendLinkMono.block(senderOptions.getRetry().getTryTimeout());
            if (block != null) {
                block.close();
            }
        }
    }

    /**
     * Collects EventData into EventDataBatch to send to Event Hubs. If {@code maxNumberOfBatches} is {@code null} then
     * it'll collect as many batches as possible. Otherwise, if there are more events than can fit into {@code
     * maxNumberOfBatches}, then the collector throws a {@link AmqpException} with {@link
     * ErrorCondition#LINK_PAYLOAD_SIZE_EXCEEDED}.
     */
    private static class EventDataCollector implements Collector<EventData, List<EventDataBatch>,
        List<EventDataBatch>> {
        private final String partitionKey;
        private final int maxMessageSize;
        private final Integer maxNumberOfBatches;
        private final ErrorContextProvider contextProvider;

        private volatile EventDataBatch currentBatch;

        EventDataCollector(BatchOptions options, Integer maxNumberOfBatches, ErrorContextProvider contextProvider) {
            this.maxNumberOfBatches = maxNumberOfBatches;
            this.maxMessageSize = options.getMaximumSizeInBytes() > 0
                ? options.getMaximumSizeInBytes()
                : MAX_MESSAGE_LENGTH_BYTES;
            this.partitionKey = options.getPartitionKey();
            this.contextProvider = contextProvider;

            currentBatch = new EventDataBatch(this.maxMessageSize, options.getPartitionKey(), contextProvider);
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
                    final String message = String.format(Locale.US,
                        "EventData does not fit into maximum number of batches. '%s'", maxNumberOfBatches);

                    throw new AmqpException(false, ErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, message,
                        contextProvider.getErrorContext());
                }

                currentBatch = new EventDataBatch(maxMessageSize, partitionKey, contextProvider);
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
