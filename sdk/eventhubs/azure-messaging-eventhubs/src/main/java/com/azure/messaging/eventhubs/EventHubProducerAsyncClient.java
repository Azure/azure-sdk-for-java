// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpConstants;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.StringUtil;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.messaging.eventhubs.implementation.EventHubManagementNode;
import com.azure.messaging.eventhubs.models.CreateBatchOptions;
import com.azure.messaging.eventhubs.models.SendOptions;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.MAX_MESSAGE_LENGTH_BYTES;

/**
 * An <b>asynchronous</b> producer responsible for transmitting {@link EventData} to a specific Event Hub, grouped
 * together in batches. Depending on the {@link CreateBatchOptions options} specified when creating an
 * {@link EventDataBatch}, the events may be automatically routed to an available partition or specific to a partition.
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
 * If no partition id is specified, the following rules are used for automatically selecting one:
 * <ol>
 * <li>Distribute the events equally amongst all available partitions using a round-robin approach.</li>
 * <li>If a partition becomes unavailable, the Event Hubs service will automatically detect it and forward the
 * message to another available partition.</li>
 * </ol>
 * </p>
 *
 * <p><strong>Create a producer and publish events to any partition</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch}
 *
 * <p><strong>Publish events to partition "foo"</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-partitionId}
 *
 * <p><strong>Publish events to the same partition, grouped together using partition key</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-partitionKey}
 *
 * <p><strong>Publish events using a size-limited {@link EventDataBatch}</strong></p>
 * {@codesnippet com.azure.messaging.eventhubs.eventhubasyncproducerclient.createBatch#CreateBatchOptions-int}
 *
 * @see EventHubClientBuilder#buildAsyncProducerClient()
 * @see EventHubProducerClient To synchronously generate events to an Event Hub, see EventHubProducerClient.
 */
@ServiceClient(builder = EventHubClientBuilder.class, isAsync = true)
public class EventHubProducerAsyncClient implements Closeable {
    private static final int MAX_PARTITION_KEY_LENGTH = 128;
    private static final String SENDER_ENTITY_PATH_FORMAT = "%s/Partitions/%s";

    private static final SendOptions DEFAULT_SEND_OPTIONS = new SendOptions();
    private static final CreateBatchOptions DEFAULT_BATCH_OPTIONS = new CreateBatchOptions();

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
    private final AmqpRetryOptions retryOptions;
    private final TracerProvider tracerProvider;
    private final MessageSerializer messageSerializer;
    private final boolean isSharedConnection;

    /**
     * Creates a new instance of this {@link EventHubProducerAsyncClient} that can send messages to a single partition
     * when {@link CreateBatchOptions#getPartitionId()} is not null or an empty string. Otherwise, allows the service to
     * load balance the messages amongst available partitions.
     */
    EventHubProducerAsyncClient(String fullyQualifiedNamespace, String eventHubName, EventHubConnection connection,
        AmqpRetryOptions retryOptions, TracerProvider tracerProvider, MessageSerializer messageSerializer,
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
     * @return The fully qualified Event Hubs namespace that the connection is associated with.
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
    public Mono<EventHubProperties> getEventHubProperties() {
        return connection.getManagementNode().flatMap(EventHubManagementNode::getEventHubProperties);
    }

    /**
     * Retrieves the identifiers for the partitions of an Event Hub.
     *
     * @return A Flux of identifiers for the partitions of an Event Hub.
     */
    public Flux<String> getPartitionIds() {
        return getEventHubProperties().flatMapMany(properties -> Flux.fromIterable(properties.getPartitionIds()));
    }

    /**
     * Retrieves information about a specific partition for an Event Hub, including elements that describe the available
     * events in the partition event stream.
     *
     * @param partitionId The unique identifier of a partition associated with the Event Hub.
     *
     * @return The set of information for the requested partition under the Event Hub this client is associated with.
     *
     * @throws NullPointerException if {@code partitionId} is null.
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
     * Creates an {@link EventDataBatch} configured with the options specified.
     *
     * @param options A set of options used to configure the {@link EventDataBatch}.
     *
     * @return A new {@link EventDataBatch} that can fit as many events as the transport allows.
     *
     * @throws NullPointerException if {@code options} is null.
     */
    public Mono<EventDataBatch> createBatch(CreateBatchOptions options) {
        if (options == null) {
            return monoError(logger, new NullPointerException("'options' cannot be null."));
        }

        final String partitionKey = options.getPartitionKey();
        final String partitionId = options.getPartitionId();
        final int batchMaxSize = options.getMaximumSizeInBytes();

        if (!CoreUtils.isNullOrEmpty(partitionKey)
            && !CoreUtils.isNullOrEmpty(partitionId)) {
            return monoError(logger, new IllegalArgumentException(String.format(Locale.US,
                "CreateBatchOptions.getPartitionKey() and CreateBatchOptions.getPartitionId() are both set. "
                    + "Only one or the other can be used. partitionKey: '%s'. partitionId: '%s'",
                partitionKey, partitionId)));
        } else if (!CoreUtils.isNullOrEmpty(partitionKey)
            && partitionKey.length() > MAX_PARTITION_KEY_LENGTH) {
            return monoError(logger, new IllegalArgumentException(String.format(Locale.US,
                "Partition key '%s' exceeds the maximum allowed length: '%s'.", partitionKey,
                MAX_PARTITION_KEY_LENGTH)));
        }

        return getSendLink(partitionId)
            .flatMap(link -> link.getLinkSize()
                .flatMap(size -> {
                    final int maximumLinkSize = size > 0
                        ? size
                        : MAX_MESSAGE_LENGTH_BYTES;

                    if (batchMaxSize > maximumLinkSize) {
                        return monoError(logger,
                            new IllegalArgumentException(String.format(Locale.US,
                                "BatchOptions.maximumSizeInBytes (%s bytes) is larger than the link size (%s bytes).",
                                batchMaxSize, maximumLinkSize)));
                    }

                    final int batchSize = batchMaxSize > 0
                        ? batchMaxSize
                        : maximumLinkSize;

                    return Mono.just(new EventDataBatch(batchSize, partitionId, partitionKey, link::getErrorContext,
                        tracerProvider));
                }));
    }

    /**
     * Sends a single event to the associated Event Hub. If the size of the single event exceeds the maximum size
     * allowed, an exception will be triggered and the send will fail.
     *
     * <p>
     * For more information regarding the maximum event size allowed, see
     * <a href="https://docs.microsoft.com/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     *
     * @param event Event to send to the service.
     *
     * @return A {@link Mono} that completes when the event is pushed to the service.
     */
    Mono<Void> send(EventData event) {
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
     * <a href="https://docs.microsoft.com/azure/event-hubs/event-hubs-quotas">Azure Event Hubs Quotas and
     * Limits</a>.
     * </p>
     *
     * @param event Event to send to the service.
     * @param options The set of options to consider when sending this event.
     *
     * @return A {@link Mono} that completes when the event is pushed to the service.
     */
    Mono<Void> send(EventData event, SendOptions options) {
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
    Mono<Void> send(Iterable<EventData> events) {
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
    Mono<Void> send(Iterable<EventData> events, SendOptions options) {
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
    Mono<Void> send(Flux<EventData> events) {
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
    Mono<Void> send(Flux<EventData> events, SendOptions options) {
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
     * @see EventHubProducerAsyncClient#createBatch(CreateBatchOptions)
     */
    public Mono<Void> send(EventDataBatch batch) {
        if (batch == null) {
            return monoError(logger, new NullPointerException("'batch' cannot be null."));
        } else if (batch.getEvents().isEmpty()) {
            logger.warning(Messages.CANNOT_SEND_EVENT_BATCH_EMPTY);
            return Mono.empty();
        }

        if (!CoreUtils.isNullOrEmpty(batch.getPartitionId())) {
            logger.info("Sending batch with size[{}] to partitionId[{}].", batch.getCount(), batch.getPartitionId());
        } else if (!CoreUtils.isNullOrEmpty(batch.getPartitionKey())) {
            logger.info("Sending batch with size[{}] with partitionKey[{}].",
                batch.getCount(), batch.getPartitionKey());
        } else {
            logger.info("Sending batch with size[{}] to be distributed round-robin in service.", batch.getCount());
        }

        final String partitionKey = batch.getPartitionKey();
        final boolean isTracingEnabled = tracerProvider.isEnabled();
        final AtomicReference<Context> parentContext = isTracingEnabled
            ? new AtomicReference<>(Context.NONE)
            : null;

        final List<Message> messages = batch.getEvents().stream().map(event -> {
            final Message message = messageSerializer.serialize(event);
            if (isTracingEnabled) {
                parentContext.set(event.getContext());
            }
            if (!CoreUtils.isNullOrEmpty(partitionKey)) {
                final MessageAnnotations messageAnnotations = message.getMessageAnnotations() == null
                    ? new MessageAnnotations(new HashMap<>())
                    : message.getMessageAnnotations();
                messageAnnotations.getValue().put(AmqpConstants.PARTITION_KEY, partitionKey);
                message.setMessageAnnotations(messageAnnotations);
            }

            return message;
        }).collect(Collectors.toList());

        return getSendLink(batch.getPartitionId())
            .flatMap(link -> {
                if (isTracingEnabled) {
                    Context userSpanContext = parentContext.get();
                    Context entityContext = userSpanContext.addData(ENTITY_PATH_KEY, link.getEntityPath());
                    // start send span and store updated context
                    parentContext.set(tracerProvider.startSpan(
                        entityContext.addData(HOST_NAME_KEY, link.getHostname()), ProcessKind.SEND));
                }
                return messages.size() == 1
                    ? link.send(messages.get(0))
                    : link.send(messages);

            }).doOnEach(signal -> {
                if (isTracingEnabled) {
                    tracerProvider.endSpan(parentContext.get(), signal);
                }
            });
    }

    private Mono<Void> sendInternal(Flux<EventData> events, SendOptions options) {
        final String partitionKey = options.getPartitionKey();
        final String partitionId = options.getPartitionId();

        if (!CoreUtils.isNullOrEmpty(partitionKey)
            && !CoreUtils.isNullOrEmpty(partitionId)) {
            return monoError(logger, new IllegalArgumentException(String.format(Locale.US,
                "SendOptions.getPartitionKey() and SendOptions.getPartitionId() are both set. Only one or the"
                    + " other can be used. partitionKey: '%s'. partitionId: '%s'",
                partitionKey, partitionId)));
        }

        return getSendLink(options.getPartitionId())
            .flatMap(link -> link.getLinkSize()
                .flatMap(size -> {
                    final int batchSize = size > 0 ? size : MAX_MESSAGE_LENGTH_BYTES;
                    final CreateBatchOptions batchOptions = new CreateBatchOptions()
                        .setPartitionKey(options.getPartitionKey())
                        .setPartitionId(options.getPartitionId())
                        .setMaximumSizeInBytes(batchSize);
                    return events.collect(new EventDataCollector(batchOptions, 1, link::getErrorContext,
                        tracerProvider));
                })
                .flatMap(list -> sendInternal(Flux.fromIterable(list))));
    }

    private Mono<Void> sendInternal(Flux<EventDataBatch> eventBatches) {
        return eventBatches
            .flatMap(this::send)
            .then()
            .doOnError(error -> {
                logger.error(Messages.ERROR_SENDING_BATCH, error);
            });
    }

    private String getEntityPath(String partitionId) {
        return CoreUtils.isNullOrEmpty(partitionId)
            ? eventHubName
            : String.format(Locale.US, SENDER_ENTITY_PATH_FORMAT, eventHubName, partitionId);
    }

    private String getLinkName(String partitionId) {
        return CoreUtils.isNullOrEmpty(partitionId)
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
     * Disposes of the {@link EventHubProducerAsyncClient}. If the client had a dedicated connection, the underlying
     * connection is also closed.
     */
    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        openLinks.forEach((key, value) -> value.close());
        openLinks.clear();

        if (!isSharedConnection) {
            connection.close();
        }
    }

    /**
     * Collects EventData into EventDataBatch to send to Event Hubs. If {@code maxNumberOfBatches} is {@code null} then
     * it'll collect as many batches as possible. Otherwise, if there are more events than can fit into {@code
     * maxNumberOfBatches}, then the collector throws a {@link AmqpException} with {@link
     * AmqpErrorCondition#LINK_PAYLOAD_SIZE_EXCEEDED}.
     */
    private static class EventDataCollector implements Collector<EventData, List<EventDataBatch>,
        List<EventDataBatch>> {
        private final String partitionKey;
        private final String partitionId;
        private final int maxMessageSize;
        private final Integer maxNumberOfBatches;
        private final ErrorContextProvider contextProvider;
        private final TracerProvider tracerProvider;

        private volatile EventDataBatch currentBatch;

        EventDataCollector(CreateBatchOptions options, Integer maxNumberOfBatches, ErrorContextProvider contextProvider,
            TracerProvider tracerProvider) {
            this.maxNumberOfBatches = maxNumberOfBatches;
            this.maxMessageSize = options.getMaximumSizeInBytes() > 0
                ? options.getMaximumSizeInBytes()
                : MAX_MESSAGE_LENGTH_BYTES;
            this.partitionKey = options.getPartitionKey();
            this.partitionId = options.getPartitionId();
            this.contextProvider = contextProvider;
            this.tracerProvider = tracerProvider;

            currentBatch = new EventDataBatch(maxMessageSize, partitionId, partitionKey, contextProvider,
                tracerProvider);
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
                        Messages.EVENT_DATA_DOES_NOT_FIT, maxNumberOfBatches);

                    throw new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, message,
                        contextProvider.getErrorContext());
                }

                currentBatch = new EventDataBatch(maxMessageSize, partitionId, partitionKey, contextProvider,
                    tracerProvider);
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
