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
import com.azure.messaging.servicebus.implementation.CreateBatchOptions;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.io.Closeable;
import java.io.IOException;
import java.time.Instant;
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
public final class QueueSenderAsyncClient implements Closeable {


    private final ClientLogger logger = new ClientLogger(QueueSenderAsyncClient.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final SendOptions senderOptions;
    private final TracerProvider tracerProvider;
    private final MessageSerializer messageSerializer;
    private final AmqpRetryOptions retryOptions;
    private final AmqpRetryPolicy retryPolicy;
    private final boolean isSharedConnection;
    private final String queueName;

    private final ServiceBusConnectionProcessor connectionProcessor;

    private static final SendOptions DEFAULT_SEND_OPTIONS = new SendOptions();

    /**
     * The default maximum allowable size, in bytes, for a batch to be sent.
     */
    public static final int MAX_MESSAGE_LENGTH_BYTES = 256 * 1024;

    /**
     * Creates a new instance of this {@link QueueSenderAsyncClient} that sends messages to
     */
    QueueSenderAsyncClient(String queueName, ServiceBusConnectionProcessor connectionProcessor, SendOptions options,
                           AmqpRetryOptions retryOptions, TracerProvider tracerProvider,
                           MessageSerializer messageSerializer, boolean isSharedConnection) {
        // Caching the created link so we don't invoke another link creation.
        this.messageSerializer = Objects.requireNonNull(messageSerializer, "'messageSerializer' cannot be null.");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        this.queueName = Objects.requireNonNull(queueName, "'entityPath' cannot be null.");
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor,
            "'connectionProcessor' cannot be null.");
        this.senderOptions = options;
        this.tracerProvider = tracerProvider;
        this.isSharedConnection = isSharedConnection;
        this.retryPolicy = getRetryPolicy(retryOptions);
    }

    public Mono<Void> send(Message message) {
        Objects.requireNonNull(message, "'message' cannot be null.");

        return send(Flux.just(message));
    }

    public Mono<Void> send(Message message, String sessionId) {
        //TODO Implement session id feature
        Objects.requireNonNull(message, "'message' cannot be null.");
        return send(Flux.just(message));
    }

    public Mono<Void> send(Message message, SendOptions options, String sessionId) {
        Objects.requireNonNull(message, "'message' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");
        //TODO Implement session id feature
        return send(Flux.just(message), options);
    }

    public Mono<Void> send(Message message, SendOptions options) {
        Objects.requireNonNull(message, "'message' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");

        return send(Flux.just(message), options);
    }


    public Mono<Void> send(Iterable<Message> messages) {
        Objects.requireNonNull(messages, "'messages' cannot be null.");

        return send(Flux.fromIterable(messages));
    }

    /**
     * Sends a set of events to the associated Event Hub using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     * @param messages Events to send to the service.
     * @param options The set of options to consider when sending this batch.
     *
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Iterable<Message> messages, SendOptions options) {
        Objects.requireNonNull(messages, "'options' cannot be null.");

        return send(Flux.fromIterable(messages), options);
    }

    /**
     * Sends a set of events to the associated Queue using a batched approach. If the size of events exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     * @param messages Events to send to the service.
     *
     * @return A {@link Mono} that completes when all events are pushed to the service.
     */
    public Mono<Void> send(Flux<Message> messages) {
        Objects.requireNonNull(messages, "'messages' cannot be null.");

        return send(messages, DEFAULT_SEND_OPTIONS);
    }

    /**
     * Sends a set of messages to the associated Event Hub using a batched approach. If the size of messages exceed the
     * maximum size of a single batch, an exception will be triggered and the send will fail. By default, the message
     * size is the max amount allowed on the link.
     * @param messages Events to send to the service.
     * @param options The set of options to consider when sending this batch.
     *
     * @return A {@link Mono} that completes when all messages are pushed to the service.
     */
    public Mono<Void> send(Flux<Message> messages, SendOptions options) {
        Objects.requireNonNull(messages, "'messages' cannot be null.");
        Objects.requireNonNull(options, "'options' cannot be null.");

        return sendInternal(messages, options);
    }


    private Mono<Void> sendInternal(Flux<Message> messages, SendOptions options) {
        final String partitionKey = options.getPartitionKey();
        final String partitionId = options.getPartitionId();

        if (!CoreUtils.isNullOrEmpty(partitionKey)
            && !CoreUtils.isNullOrEmpty(partitionId)) {
            return monoError(logger, new IllegalArgumentException(String.format(Locale.US,
                "SendOptions.getPartitionKey() and SendOptions.getPartitionId() are both set. Only one or the"
                    + " other can be used. partitionKey: '%s'. partitionId: '%s'",
                partitionKey, partitionId)));
        }

        return getSendLink()
            .flatMap(link -> link.getLinkSize()
                .flatMap(size -> {
                    final int batchSize = size > 0 ? size : MAX_MESSAGE_LENGTH_BYTES;
                    final CreateBatchOptions batchOptions = new CreateBatchOptions()
                        .setMaximumSizeInBytes(batchSize);
                    return messages.collect(new AmqpMessageCollector(batchOptions, 1, link::getErrorContext,
                        tracerProvider));
                })
                .flatMap(list -> sendInternal(Flux.fromIterable(list))));
    }

    private Mono<Void> sendInternal(Flux<MessageBatch> eventBatches) {
        return eventBatches
            .flatMap(this::send)
            .then()
            .doOnError(error -> {
                logger.error("Error sending batch.", error);
            });
    }


    public long schedule(Message message, Instant scheduledEnqueueTimeUt){
        return 0;
    }

    public void cancelScheduledMessage(long sequenceNumber){

    }

    public String getQueueName() {
        return this.queueName;
    }

    /**
     *
     * @param batch of messages which allows client to send maximum allowed size for a batch of messages.
     * @return
     */
    public Mono<Void> send(MessageBatch batch) {
        Objects.requireNonNull(batch, "'batch' cannot be null.");
        final boolean isTracingEnabled = tracerProvider.isEnabled();
        final AtomicReference<Context> parentContext = isTracingEnabled
            ? new AtomicReference<>(Context.NONE)
            : null;

        if (batch.getMessageList().isEmpty()) {
            logger.info("Cannot send an EventBatch that is empty.");
            return Mono.empty();
        }

        logger.info("Sending batch with size[{}].", batch.getCount());

        Context sharedContext = null;
        final List<org.apache.qpid.proton.message.Message> messages = new ArrayList<>();

        for (int i = 0; i < batch.getMessageList().size(); i++) {
            final Message event = batch.getMessageList().get(i);
            if (isTracingEnabled) {
                parentContext.set(event.getContext());
                if (i == 0) {
                    sharedContext = tracerProvider.getSharedSpanBuilder(parentContext.get());
                }
                tracerProvider.addSpanLinks(sharedContext.addData(SPAN_CONTEXT_KEY, event.getContext()));
            }
            final org.apache.qpid.proton.message.Message message = messageSerializer.serialize(event);

            final MessageAnnotations messageAnnotations = message.getMessageAnnotations() == null
                    ? new MessageAnnotations(new HashMap<>())
                    : message.getMessageAnnotations();

            message.setMessageAnnotations(messageAnnotations);
            messages.add(message);
        }

        final Context finalSharedContext = sharedContext != null ? sharedContext : Context.NONE;

        return withRetry(
            getSendLink().flatMap(link -> {
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

    private Mono<AmqpSendLink> getSendLink() {
        final String entityPath = queueName;//getQueueName(partitionId);
        final String linkName = queueName;//getQueueName(partitionId);

        return connectionProcessor
            .flatMap(connection -> connection.createSendLink(linkName, entityPath, retryOptions));
    }

    private static class AmqpMessageCollector implements Collector<Message, List<MessageBatch>, List<MessageBatch>> {
        private final int maxMessageSize;
        private final Integer maxNumberOfBatches;
        private final ErrorContextProvider contextProvider;
        private final TracerProvider tracerProvider;

        private volatile MessageBatch currentBatch;

        AmqpMessageCollector(CreateBatchOptions options, Integer maxNumberOfBatches,
                             ErrorContextProvider contextProvider,
                             TracerProvider tracerProvider) {
            this.maxNumberOfBatches = maxNumberOfBatches;
            this.maxMessageSize = options.getMaximumSizeInBytes() > 0
                ? options.getMaximumSizeInBytes()
                : MAX_MESSAGE_LENGTH_BYTES;
            this.contextProvider = contextProvider;
            this.tracerProvider = tracerProvider;

            currentBatch = new MessageBatch(maxMessageSize, contextProvider, tracerProvider);
        }

        @Override
        public Supplier<List<MessageBatch>> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<List<MessageBatch>, Message> accumulator() {
            return (list, event) -> {
                MessageBatch batch = currentBatch;
                if (batch.tryAdd(event)) {
                    return;
                }

                if (maxNumberOfBatches != null && list.size() == maxNumberOfBatches) {
                    final String message = String.format(Locale.US,
                        "EventData does not fit into maximum number of batches. '%s'", maxNumberOfBatches);

                    throw new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, message, contextProvider.getErrorContext());
                }

                currentBatch = new MessageBatch(maxMessageSize, contextProvider, tracerProvider);
                currentBatch.tryAdd(event);
                list.add(batch);
            };
        }

        @Override
        public BinaryOperator<List<MessageBatch>> combiner() {
            return (existing, another) -> {
                existing.addAll(another);
                return existing;
            };
        }

        @Override
        public Function<List<MessageBatch>, List<MessageBatch>> finisher() {
            return list -> {
                MessageBatch batch = currentBatch;
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

    private Message setSpanContext(Message event, Context parentContext) {
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
     * Disposes of the {@link QueueSenderAsyncClient}. If the client had a dedicated connection, the underlying
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
    public Mono<Long> schedule(Message message, Instant scheduledEnqueueTimeUt, TransactionContext context){
        return null;
    }
    public Mono<Void> send(Message message, TransactionContext context){
        return null;
    }
}
