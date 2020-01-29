package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.SendOptions;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpConstants;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.ErrorContextProvider;

import static com.azure.core.amqp.implementation.RetryUtil.getRetryPolicy;
import static com.azure.core.amqp.implementation.RetryUtil.withRetry;

import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.messaging.servicebus.models.CreateBatchOptions;
import com.azure.messaging.servicebus.implementation.SBConnectionProcessor;
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

import static com.azure.core.amqp.implementation.RetryUtil.withRetry;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;

@ServiceClient(builder = QueueClientBuilder.class, isAsync = true)
public class SenderAsyncClient implements Closeable {

    private static final String SENDER_ENTITY_PATH_FORMAT = "%s/Partitions/%s";
    private final ClientLogger logger = new ClientLogger(SenderAsyncClient.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final SendOptions senderOptions;
    private Mono<AmqpSendLink> sendLinkMono;
    private final TracerProvider tracerProvider;
    private final MessageSerializer messageSerializer;
    private final AmqpRetryOptions retryOptions;
    private final AmqpRetryPolicy retryPolicy;
    private final boolean isSharedConnection;
    private final String entityPath;

    private final SBConnectionProcessor connectionProcessor;

    private static final SendOptions DEFAULT_SEND_OPTIONS = new SendOptions();

    /**
     * The default maximum allowable size, in bytes, for a batch to be sent.
     */
    public static final int MAX_MESSAGE_LENGTH_BYTES = 256 * 1024;

    /**
     * Creates a new instance of this {@link SenderAsyncClient} that sends messages to
     */
    SenderAsyncClient(/*Mono<AmqpSendLink> amqpSendLinkMono,*/  String entityPath,
                      SBConnectionProcessor connectionProcessor, SendOptions options, AmqpRetryOptions retryOptions,
                      TracerProvider tracerProvider, MessageSerializer messageSerializer, boolean isSharedConnection) {
        // Caching the created link so we don't invoke another link creation.
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        this.entityPath = Objects.requireNonNull(entityPath, "'entityPath' cannot be null.");
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor,
            "'connectionProcessor' cannot be null.");
        //this.sendLinkMono = amqpSendLinkMono.cache();
        this.senderOptions = options;
        this.tracerProvider = tracerProvider;
        this.isSharedConnection = isSharedConnection;
        this.retryPolicy = getRetryPolicy(retryOptions);
    }

    public Mono<Void> send(EventData message) {
        Objects.requireNonNull(message, "'event' cannot be null.");

        return send(Flux.just(message));
    }

    public Mono<Void> send(EventData event, SendOptions options) {
        Objects.requireNonNull(event, "'event' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");

        return send(Flux.just(event), options);
    }


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
     *
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
     *
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Flux<EventData> events, SendOptions options) {
        Objects.requireNonNull(events, "'events' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");

        return sendInternal(events, options);
    }


    /*private Mono<Void> sendInternal(Flux<EventData> events, SendOptions options) {
        final String partitionId = options.getPartitionId();

        //verifyPartitionKey(partitionKey);
        if (tracerProvider.isEnabled()) {
            return sendInternalTracingEnabled(events, options);
        } else {
            return sendInternalTracingDisabled(events, options);
        }
    }
    */
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
                    return events.collect(new AmqpMessageCollector(batchOptions, 1, link::getErrorContext,
                        tracerProvider));
                })
                .flatMap(list -> sendInternal(Flux.fromIterable(list))));
    }

    private Mono<Void> sendInternalTracingDisabled(Flux<EventData> events, SendOptions options) {
        return sendLinkMono.flatMap(link -> {
            return link.getLinkSize()
                .flatMap(size -> {
                    final int batchSize = size > 0 ? size : MAX_MESSAGE_LENGTH_BYTES;
                    final CreateBatchOptions batchOptions = new CreateBatchOptions()
                        .setPartitionKey(options.getPartitionKey())
                        .setPartitionId(options.getPartitionId())
                        .setMaximumSizeInBytes(batchSize);

                    return events.collect(new AmqpMessageCollector(batchOptions, 1,
                        () -> link.getErrorContext(), tracerProvider));
                })
                .flatMap(list -> sendInternal(Flux.fromIterable(list)));
        });
    }

    private Mono<Void> sendInternalTracingEnabled(Flux<EventData> events, SendOptions options) {
        return sendLinkMono.flatMap(link -> {
            final AtomicReference<Context> sendSpanContext = new AtomicReference<>(Context.NONE);
            return link.getLinkSize()
                .flatMap(size -> {
                    final int batchSize = size > 0 ? size : MAX_MESSAGE_LENGTH_BYTES;
                    final CreateBatchOptions batchOptions = new CreateBatchOptions()
                        .setPartitionKey(options.getPartitionKey())
                        .setPartitionId(options.getPartitionId())
                        .setMaximumSizeInBytes(batchSize);

                    return events.map(eventData -> {
                        Context parentContext = eventData.getContext();
                        Context entityContext = parentContext.addData(ENTITY_PATH_KEY, link.getEntityPath());
                        sendSpanContext.set(tracerProvider.startSpan(entityContext.addData(HOST_NAME_KEY, link.getHostname()), ProcessKind.SEND));
                        // add span context on event data
                        return setSpanContext(eventData, parentContext);
                    }).collect(new AmqpMessageCollector(batchOptions, 1, () -> link.getErrorContext(), tracerProvider));
                })
                .flatMap(list -> sendInternal(Flux.fromIterable(list)))
                .doOnEach(signal -> {
                    tracerProvider.endSpan(sendSpanContext.get(), signal);
                });
        });
    }
    private Mono<Void> sendInternal(Flux<EventDataBatch> eventBatches) {
        return eventBatches
            .flatMap(this::send)
            .then()
            .doOnError(error -> {
                logger.error("Error sending batch.", error);
            });
    }

    public Mono<Void> send(EventDataBatch batch) {
        Objects.requireNonNull(batch, "'batch' cannot be null.");
        final boolean isTracingEnabled = tracerProvider.isEnabled();
        final AtomicReference<Context> parentContext = isTracingEnabled
            ? new AtomicReference<>(Context.NONE)
            : null;

        if (batch.getEvents().isEmpty()) {
            logger.info("Cannot send an EventBatch that is empty.");
            return Mono.empty();
        }

        logger.info("Sending batch with partitionKey[{}], size[{}].", batch.getPartitionKey(), batch.getCount());

        Context sharedContext = null;
        final List<Message> messages = new ArrayList<>();

        for (int i = 0; i < batch.getEvents().size(); i++) {
            final EventData event = batch.getEvents().get(i);
            if (isTracingEnabled) {
                parentContext.set(event.getContext());
                if (i == 0) {
                    sharedContext = tracerProvider.getSharedSpanBuilder(parentContext.get());
                }
                tracerProvider.addSpanLinks(sharedContext.addData(SPAN_CONTEXT_KEY, event.getContext()));
            }
            final Message message = messageSerializer.serialize(event);

            if (!CoreUtils.isNullOrEmpty(batch.getPartitionKey())) {
                final MessageAnnotations messageAnnotations = message.getMessageAnnotations() == null
                    ? new MessageAnnotations(new HashMap<>())
                    : message.getMessageAnnotations();
                messageAnnotations.getValue().put(AmqpConstants.PARTITION_KEY, batch.getPartitionKey());
                message.setMessageAnnotations(messageAnnotations);
            }
            messages.add(message);
        }

        final Context finalSharedContext = sharedContext != null ? sharedContext : Context.NONE;

        return withRetry(
            getSendLink(batch.getPartitionId()).flatMap(link -> {
                if (isTracingEnabled) {
                    Context entityContext = finalSharedContext.addData(ENTITY_PATH_KEY, link.getEntityPath());
                    // Start send span and store updated context
                    parentContext.set(tracerProvider.startSpan(
                        entityContext.addData(HOST_NAME_KEY, link.getHostname()), ProcessKind.SEND));
                }
                return messages.size() == 1
                    ? link.send(messages.get(0))
                    : link.send(messages);

            })
                .doOnEach(signal -> {
                    if (isTracingEnabled) {
                        tracerProvider.endSpan(parentContext.get(), signal);
                    }
                })
                .doOnError(error -> {
                    if (isTracingEnabled) {
                        tracerProvider.endSpan(parentContext.get(), Signal.error(error));
                    }
                }), retryOptions.getTryTimeout(), retryPolicy);

    }

    private String getEntityPath(String partitionId) {
        return CoreUtils.isNullOrEmpty(partitionId)
            ? entityPath
            : String.format(Locale.US, SENDER_ENTITY_PATH_FORMAT, entityPath, partitionId);
    }

    private Mono<AmqpSendLink> getSendLink(String partitionId) {
        final String entityPath = getEntityPath(partitionId);
        final String linkName = getEntityPath(partitionId);

        return connectionProcessor
            .flatMap(connection -> connection.createSendLink(linkName, entityPath, retryOptions));
    }

    private static class AmqpMessageCollector implements Collector<EventData, List<EventDataBatch>, List<EventDataBatch>> {
        private final String partitionKey;
        private final String partitionId;
        private final int maxMessageSize;
        private final Integer maxNumberOfBatches;
        private final ErrorContextProvider contextProvider;
        private final TracerProvider tracerProvider;

        private volatile EventDataBatch currentBatch;

        AmqpMessageCollector(CreateBatchOptions options, Integer maxNumberOfBatches,
                             ErrorContextProvider contextProvider,
                             TracerProvider tracerProvider) {
            this.maxNumberOfBatches = maxNumberOfBatches;
            this.maxMessageSize = options.getMaximumSizeInBytes() > 0
                ? options.getMaximumSizeInBytes()
                : MAX_MESSAGE_LENGTH_BYTES;
            this.partitionKey = options.getPartitionKey();
            this.contextProvider = contextProvider;
            this.partitionId = options.getPartitionId();
            this.tracerProvider = tracerProvider;

            currentBatch = new EventDataBatch(maxMessageSize, options.getPartitionId(), partitionKey, contextProvider,
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
                        "EventData does not fit into maximum number of batches. '%s'", maxNumberOfBatches);

                    throw new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, message, contextProvider.getErrorContext());
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
                logger.warning(String.format(Locale.US,
                    "Event Data context type is not of type Context, but type: %s. Not adding span links.",
                    spanContextObject != null ? spanContextObject.getClass() : "null"));
            }

            return event;
        } else {
            // Starting the span makes the sampling decision (nothing is logged at this time)
            Context eventSpanContext = tracerProvider.startSpan(parentContext, ProcessKind.SEND);
            if (eventSpanContext != null) {
                Optional<Object> eventDiagnosticIdOptional = eventSpanContext.getData(DIAGNOSTIC_ID_KEY);

                if (eventDiagnosticIdOptional.isPresent()) {
                    event.addContext(DIAGNOSTIC_ID_KEY, eventDiagnosticIdOptional.get().toString());
                    tracerProvider.endSpan(eventSpanContext, Signal.complete());
                    event.addContext(SPAN_CONTEXT_KEY, eventSpanContext);
                }
            }
        }
        return  event;
    }

    /**
     * Disposes of the {@link SenderAsyncClient} by closing the underlying connection to the service.
     * @throws IOException if the underlying transport could not be closed and its resources could not be
     *                     disposed.
     */
    /*@Override
    public void close() throws IOException {
        if (!isDisposed.getAndSet(true)) {
            final AmqpSendLink block = sendLinkMono.block( retryOptions.getTryTimeout());
            if (block != null) {
                block.close();
            }
        }
    }
    */
    /**
     * Disposes of the {@link SenderAsyncClient}. If the client had a dedicated connection, the underlying
     * connection is also closed.
     */
    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        if (!isSharedConnection) {
            connectionProcessor.dispose();
        }
    }
}
