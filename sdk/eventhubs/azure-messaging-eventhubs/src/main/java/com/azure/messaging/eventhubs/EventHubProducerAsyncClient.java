// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.RetryOptions;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.ErrorCondition;
import com.azure.core.amqp.implementation.AmqpConstants;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.Immutable;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.models.BatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.azure.core.implementation.util.FluxUtil.monoError;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.MAX_MESSAGE_LENGTH_BYTES;

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
 * BatchOptions#getPartitionId() partitionId} when creating the {@link EventHubProducerAsyncClient}.
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncproducerclient.instantiation}
 *
 * <p><strong>Create a producer that publishes events to partition "foo" with a timeout of 45 seconds.</strong></p>
 * Developers can push events to a single partition by specifying the
 * {@link BatchOptions#setPartitionId(String) partitionId} when creating an {@link EventHubProducerAsyncClient}.
 *
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncproducerclient.instantiation#partitionId}
 *
 * <p><strong>Publish events to the same partition, grouped together using {@link SendOptions#setPartitionKey(String)}
 * .</strong></p>
 * If developers want to push similar events to end up at the same partition, but do not require them to go to a
 * specific partition, they can use {@link SendOptions#setPartitionKey(String)}.
 * <p>
 * In the sample below, all the "sandwiches" end up in the same partition, but it could end up in partition 0, 1, etc.
 * of the available partitions. All that matters to the end user is that they are grouped together.
 * </p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncproducerclient.send#publisher-sendOptions}
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
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncproducerclient.send#eventDataBatch}
 */
@Immutable
public class EventHubProducerAsyncClient implements Closeable {
    private static final int MAX_PARTITION_KEY_LENGTH = 128;
    private static final String SENDER_ENTITY_PATH_FORMAT = "%s/Partitions/%s";

    private static final SendOptions DEFAULT_SEND_OPTIONS = new SendOptions();
    private static final BatchOptions DEFAULT_BATCH_OPTIONS = new BatchOptions();

    /**
     * Keeps track of the opened send links. Links are key'd by their entityPath. The send link for allowing the service
     * load balance messages is the eventHubName.
     */
    private final ConcurrentHashMap<String, AmqpSendLink> openLinks = new ConcurrentHashMap<>();
    private final ClientLogger logger = new ClientLogger(EventHubProducerAsyncClient.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final String fullyQualifiedNamespace;
    private final String eventHubName;
    private final EventHubConnection connection;
    private final RetryOptions retryOptions;
    private final TracerProvider tracerProvider;
    private final MessageSerializer messageSerializer;
    private final boolean isSharedConnection;

    /**
     * Creates a new instance of this {@link EventHubProducerAsyncClient} that can send messages to a single partition
     * when {@link BatchOptions#getPartitionId()} is not null or an empty string. Otherwise, allows the service to load
     * balance the messages amongst available partitions.
     */
    EventHubProducerAsyncClient(String fullyQualifiedNamespace, String eventHubName, EventHubConnection connection,
        RetryOptions retryOptions, TracerProvider tracerProvider, MessageSerializer messageSerializer,
        boolean isSharedConnection) {
        this.fullyQualifiedNamespace = fullyQualifiedNamespace;
        this.eventHubName = eventHubName;
        this.connection = connection;
        this.retryOptions = retryOptions;
        this.tracerProvider = tracerProvider;
        this.messageSerializer = messageSerializer;
        this.isSharedConnection = isSharedConnection;
    }

    /**
     * Gets the fully qualified Event Hubs namespace that the connection is associated with. This is likely similar to
     * {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Event Hubs namespace that the connection is associated with
     */
    public String getFullyQualifiedNamespace() {
        return fullyQualifiedNamespace;
    }

    /**
     * Gets the Event Hub name this client interacts with.
     *
     * @return The Event Hub name this client interacts with.
     */
    public String getEventHubName() {
        return eventHubName;
    }

    /**
     * Retrieves information about an Event Hub, including the number of partitions present and their identifiers.
     *
     * @return The set of information for the Event Hub that this client is associated with.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<EventHubProperties> getProperties() {
        return connection.getManagementNode().flatMap(EventHubManagementNode::getEventHubProperties);
    }

    /**
     * Retrieves the identifiers for the partitions of an Event Hub.
     *
     * @return A Flux of identifiers for the partitions of an Event Hub.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<String> getPartitionIds() {
        return getProperties().flatMapMany(properties -> Flux.fromArray(properties.getPartitionIds()));
    }

    /**
     * Retrieves information about a specific partition for an Event Hub, including elements that describe the available
     * events in the partition event stream.
     *
     * @param partitionId The unique identifier of a partition associated with the Event Hub.
     * @return The set of information for the requested partition under the Event Hub this client is associated with.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PartitionProperties> getPartitionProperties(String partitionId) {
        return connection.getManagementNode().flatMap(node -> node.getPartitionProperties(partitionId));
    }

    /**
     * Creates an {@link EventDataBatch} that can fit as many events as the transport allows.
     *
     * @return A new {@link EventDataBatch} that can fit as many events as the transport allows.
     */
    public Mono<EventDataBatch> createBatch() {
        return createBatch(DEFAULT_BATCH_OPTIONS);
    }

    /**
     * Creates an {@link EventDataBatch} that can fit as many events as the transport allows.
     *
     * @param options A set of options used to configure the {@link EventDataBatch}.
     *
     * @return A new {@link EventDataBatch} that can fit as many events as the transport allows.
     */
    public Mono<EventDataBatch> createBatch(BatchOptions options) {
        if (options == null) {
            return monoError(logger, new NullPointerException("'options' cannot be null."));
        }

        final BatchOptions clone = options.clone();

        if (!ImplUtils.isNullOrEmpty(clone.getPartitionKey()) && !ImplUtils.isNullOrEmpty(clone.getPartitionId())) {
            return monoError(logger, new IllegalArgumentException(String.format(Locale.US,
                "BatchOptions.getPartitionKey() and BatchOptions.getPartitionId() are both set. Only one or the"
                    + " other can be used. partitionKey: '%s'. partitionId: '%s'",
                clone.getPartitionKey(), clone.getPartitionId())));
        } else if (!ImplUtils.isNullOrEmpty(clone.getPartitionKey())
            && clone.getPartitionKey().length() > MAX_PARTITION_KEY_LENGTH) {
            return monoError(logger, new IllegalArgumentException(String.format(Locale.US,
                "PartitionKey '%s' exceeds the maximum allowed length: '%s'.", clone.getPartitionKey(),
                MAX_PARTITION_KEY_LENGTH)));
        }

        return getSendLink(clone.getPartitionId())
            .flatMap(link -> link.getLinkSize()
                .flatMap(size -> {
                    final int maximumLinkSize = size > 0
                        ? size
                        : MAX_MESSAGE_LENGTH_BYTES;

                    if (clone.getMaximumSizeInBytes() > maximumLinkSize) {
                        return monoError(logger,
                            new IllegalArgumentException(String.format(Locale.US,
                                "BatchOptions.maximumSizeInBytes (%s bytes) is larger than the link size (%s bytes).",
                                clone.getMaximumSizeInBytes(), maximumLinkSize)));
                    }

                    final int batchSize = clone.getMaximumSizeInBytes() > 0
                        ? clone.getMaximumSizeInBytes()
                        : maximumLinkSize;

                    return Mono.just(new EventDataBatch(batchSize, clone.getPartitionId(), clone.getPartitionKey(),
                        link::getErrorContext));
                }));
    }

    /**
     * Sends a single event to the associated Event Hub. If the size of the single event exceeds the maximum size
     * allowed, an exception will be triggered and the send will fail.
     *
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
        if (event == null) {
            return monoError(logger, new NullPointerException("'event' cannot be null."));
        }

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
     *
     * @param event Event to send to the service.
     * @param options The set of options to consider when sending this event.
     *
     * @return A {@link Mono} that completes when the event is pushed to the service.
     */
    public Mono<Void> send(EventData event, SendOptions options) {
        if (event == null) {
            return monoError(logger, new NullPointerException("'event' cannot be null."));
        } else if (options == null) {
            return monoError(logger, new NullPointerException("'options' cannot be null."));
        }

        return send(Flux.just(event), options);
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     *
     * @param events Events to send to the service.
     *
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Iterable<EventData> events) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        }

        return send(Flux.fromIterable(events));
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     *
     * @param events Events to send to the service.
     * @param options The set of options to consider when sending this batch.
     *
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Iterable<EventData> events, SendOptions options) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        } else if (options == null) {
            return monoError(logger, new NullPointerException("'options' cannot be null."));
        }

        return send(Flux.fromIterable(events), options);
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     *
     * @param events Events to send to the service.
     *
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Flux<EventData> events) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        }

        return send(events, DEFAULT_SEND_OPTIONS);
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     *
     * @param events Events to send to the service.
     * @param options The set of options to consider when sending this batch.
     *
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Flux<EventData> events, SendOptions options) {
        if (events == null) {
            return monoError(logger, new NullPointerException("'events' cannot be null."));
        } else if (options == null) {
            return monoError(logger, new NullPointerException("'options' cannot be null."));
        }

        return sendInternal(events, options);
    }

    /**
     * Sends the batch to the associated Event Hub.
     *
     * @param batch The batch to send to the service.
     *
     * @return A {@link Mono} that completes when the batch is pushed to the service.
     *
     * @throws NullPointerException if {@code batch} is {@code null}.
     * @see EventHubProducerAsyncClient#createBatch()
     * @see EventHubProducerAsyncClient#createBatch(BatchOptions)
     */
    public Mono<Void> send(EventDataBatch batch) {
        if (batch == null) {
            return monoError(logger, new NullPointerException("'batch' cannot be null."));
        } else if (batch.getEvents().isEmpty()) {
            logger.warning("Cannot send an EventBatch that is empty.");
            return Mono.empty();
        }

        if (!ImplUtils.isNullOrEmpty(batch.getPartitionId())) {
            logger.info("Sending batch with size[{}] to partitionId[{}].", batch.getSize(), batch.getPartitionId());
        } else if (!ImplUtils.isNullOrEmpty(batch.getPartitionKey())) {
            logger.info("Sending batch with size[{}] with partitionKey[{}].", batch.getSize(), batch.getPartitionKey());
        } else {
            logger.info("Sending batch with size[{}] to be distributed round-robin in service.", batch.getSize());
        }

        final String partitionKey = batch.getPartitionKey();
        final List<Message> messages = batch.getEvents().stream().map(event -> {
            final Message message = messageSerializer.serialize(event);

            if (!ImplUtils.isNullOrEmpty(partitionKey)) {
                final MessageAnnotations messageAnnotations = message.getMessageAnnotations() == null
                    ? new MessageAnnotations(new HashMap<>())
                    : message.getMessageAnnotations();
                messageAnnotations.getValue().put(AmqpConstants.PARTITION_KEY, partitionKey);
                message.setMessageAnnotations(messageAnnotations);
            }

            return message;
        }).collect(Collectors.toList());

        return getSendLink(batch.getPartitionId())
            .flatMap(link -> messages.size() == 1
                ? link.send(messages.get(0))
                : link.send(messages));
    }

    private Mono<Void> sendInternal(Flux<EventData> events, SendOptions options) {
        final SendOptions clone = options.clone();
        final boolean isTracingEnabled = tracerProvider.isEnabled();

        if (!ImplUtils.isNullOrEmpty(clone.getPartitionKey()) && !ImplUtils.isNullOrEmpty(clone.getPartitionId())) {
            return monoError(logger, new IllegalArgumentException(String.format(Locale.US,
                "BatchOptions.getPartitionKey() and BatchOptions.getPartitionId() are both set. Only one or the"
                    + " other can be used. partitionKey: '%s'. partitionId: '%s'",
                clone.getPartitionKey(), clone.getPartitionId())));
        }

        return getSendLink(options.getPartitionId())
            .flatMap(link -> {
                final AtomicReference<Context> sendSpanContext = isTracingEnabled
                    ? new AtomicReference<>(Context.NONE)
                    : null;

                return link.getLinkSize()
                    .flatMap(size -> {
                        final int batchSize = size > 0 ? size : MAX_MESSAGE_LENGTH_BYTES;
                        final BatchOptions batchOptions = new BatchOptions()
                            .setPartitionKey(options.getPartitionKey())
                            .setPartitionId(options.getPartitionId())
                            .setMaximumSizeInBytes(batchSize);

                        final AtomicBoolean isFirst = new AtomicBoolean(true);
                        return events.map(eventData -> {
                            if (!isTracingEnabled) {
                                return eventData;
                            }

                            final Context parentContext = eventData.getContext();
                            if (isFirst.getAndSet(false)) {
                                // update sendSpanContext only once
                                Context entityContext = parentContext.addData(ENTITY_PATH_KEY, link.getEntityPath());
                                sendSpanContext.set(tracerProvider.startSpan(
                                    entityContext.addData(HOST_NAME_KEY, link.getHostname()), ProcessKind.SEND));
                            }

                            return setSpanContext(eventData, parentContext);
                        }).collect(new EventDataCollector(batchOptions, 1, link::getErrorContext));
                    })
                    .flatMap(list -> sendInternal(Flux.fromIterable(list)))
                    .doOnEach(signal -> {
                        if (isTracingEnabled) {
                            tracerProvider.endSpan(sendSpanContext.get(), signal);
                        }
                    });
            });
    }

    private EventData setSpanContext(EventData event, Context parentContext) {
        Optional<Object> eventContextData = event.getContext().getData(SPAN_CONTEXT_KEY);
        if (eventContextData.isPresent()) {
            // if message has context (in case of retries), link it to the span
            Object spanContextObject = eventContextData.get();
            if (spanContextObject instanceof Context) {
                tracerProvider.addSpanLinks((Context) eventContextData.get());

                // TODO (samvaity): not supported in Opencensus yet
                // builder.addLink((Context)eventContextData.get());
            } else {
                logger.warning("Event Data context type is not of type Context, but type: {}. Not adding span links.",
                    spanContextObject.getClass());
            }
        } else {
            // Starting the span makes the sampling decision (nothing is logged at this time)
            Context eventSpanContext = tracerProvider.startSpan(parentContext, ProcessKind.MESSAGE);
            if (eventSpanContext != null) {
                Optional<Object> eventDiagnosticIdOptional = eventSpanContext.getData(DIAGNOSTIC_ID_KEY);

                if (eventDiagnosticIdOptional.isPresent()) {
                    event.addProperty(DIAGNOSTIC_ID_KEY, eventDiagnosticIdOptional.get().toString());
                    tracerProvider.endSpan(eventSpanContext, Signal.complete());
                    event.addContext(SPAN_CONTEXT_KEY, eventSpanContext);
                }
            }
        }

        return event;
    }

    private Mono<Void> sendInternal(Flux<EventDataBatch> eventBatches) {
        return eventBatches
            .flatMap(this::send)
            .then()
            .doOnError(error -> {
                logger.error("Error sending batch.", error);
            });
    }

    private String getEntityPath(String partitionId) {
        return ImplUtils.isNullOrEmpty(partitionId)
            ? eventHubName
            : String.format(Locale.US, SENDER_ENTITY_PATH_FORMAT, eventHubName, partitionId);
    }

    private String getLinkName(String partitionId) {
        return ImplUtils.isNullOrEmpty(partitionId)
            ? StringUtil.getRandomString("EC")
            : StringUtil.getRandomString("PS");
    }

    private Mono<AmqpSendLink> getSendLink(String partitionId) {
        final String entityPath = getEntityPath(partitionId);
        final AmqpSendLink openLink = openLinks.get(entityPath);

        if (openLink != null) {
            return Mono.just(openLink);
        } else {
            return connection.createSendLink(getLinkName(partitionId), entityPath, retryOptions)
                .map(link -> openLinks.computeIfAbsent(entityPath, unusedKey -> link));
        }
    }

    /**
     * Disposes of the {@link EventHubProducerAsyncClient} by closing the underlying connection to the service.
     */
    @Override
    public void close() {
        if (!isDisposed.getAndSet(true)) {
            openLinks.forEach((key, value) -> {
                try {
                    value.close();
                } catch (IOException e) {
                    logger.warning("Error closing link for partition: {}", key, e);
                }
            });
            openLinks.clear();

            if (!isSharedConnection) {
                connection.close();
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
        private final String partitionId;
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
            this.partitionId = options.getPartitionId();
            this.contextProvider = contextProvider;

            currentBatch = new EventDataBatch(maxMessageSize, partitionId, partitionKey, contextProvider);
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

                currentBatch = new EventDataBatch(maxMessageSize, partitionId, partitionKey, contextProvider);
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
