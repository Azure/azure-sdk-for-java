// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.ErrorContextProvider;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.messaging.servicebus.implementation.MessagingEntityType;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import com.azure.messaging.servicebus.models.CreateBatchOptions;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static com.azure.core.amqp.implementation.RetryUtil.getRetryPolicy;
import static com.azure.core.amqp.implementation.RetryUtil.withRetry;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;

/**
 * An <b>asynchronous</b> client to send messages to a Service Bus resource.
 *
 * <p><strong>Create an instance of sender</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusasyncsenderclient.instantiation}
 *
 * <p><strong>Create an instance of sender using default credential</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusasyncsenderclient.instantiateWithDefaultCredential}
 *
 * <p><strong>Send messages to a Service Bus resource</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusasyncsenderclient.createBatch}
 *
 * <p><strong>Send messages using a size-limited {@link ServiceBusMessageBatch} to a Service Bus resource</strong></p>
 * {@codesnippet com.azure.messaging.servicebus.servicebusasyncsenderclient.createBatch#CreateBatchOptionsLimitedSize}
 *
 */
@ServiceClient(builder = ServiceBusClientBuilder.class, isAsync = true)
public final class ServiceBusSenderAsyncClient implements AutoCloseable {
    /**
     * The default maximum allowable size, in bytes, for a batch to be sent.
     */
    static final int MAX_MESSAGE_LENGTH_BYTES = 256 * 1024;

    private static final CreateBatchOptions DEFAULT_BATCH_OPTIONS =  new CreateBatchOptions();

    private final ClientLogger logger = new ClientLogger(ServiceBusSenderAsyncClient.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final TracerProvider tracerProvider;
    private final MessageSerializer messageSerializer;
    private final AmqpRetryOptions retryOptions;
    private final AmqpRetryPolicy retryPolicy;
    private final MessagingEntityType entityType;
    private final Runnable onClientClose;
    private final String entityName;
    private final ServiceBusConnectionProcessor connectionProcessor;

    /**
     * Creates a new instance of this {@link ServiceBusSenderAsyncClient} that sends messages to a Service Bus entity.
     */
    ServiceBusSenderAsyncClient(String entityName, MessagingEntityType entityType,
        ServiceBusConnectionProcessor connectionProcessor, AmqpRetryOptions retryOptions,
        TracerProvider tracerProvider, MessageSerializer messageSerializer, Runnable onClientClose) {
        // Caching the created link so we don't invoke another link creation.
        this.messageSerializer = Objects.requireNonNull(messageSerializer,
            "'messageSerializer' cannot be null.");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        this.entityName = Objects.requireNonNull(entityName, "'entityPath' cannot be null.");
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor,
            "'connectionProcessor' cannot be null.");
        this.tracerProvider = tracerProvider;
        this.retryPolicy = getRetryPolicy(retryOptions);
        this.entityType = entityType;

        this.onClientClose = onClientClose;
    }

    /**
     * Gets the fully qualified namespace.
     *
     * @return The fully qualified namespace.
     */
    public String getFullyQualifiedNamespace() {
        return connectionProcessor.getFullyQualifiedNamespace();
    }

    /**
     * Gets the name of the Service Bus resource.
     *
     * @return The name of the Service Bus resource.
     */
    public String getEntityPath() {
        return entityName;
    }

    /**
     * Sends a message to a Service Bus queue or topic.
     *
     * @param message Message to be sent to Service Bus queue or topic.
     *
     * @return The {@link Mono} the finishes this operation on service bus resource.
     *
     * @throws NullPointerException if {@code message} is {@code null}.
     */
    public Mono<Void> send(ServiceBusMessage message) {
        Objects.requireNonNull(message, "'message' cannot be null.");

        return sendInternal(Flux.just(message));
    }

    /**
     * Sends a message to a Service Bus queue or topic.
     *
     * @param message Message to be sent to Service Bus queue or topic.
     * @param sessionId the session id to associate with the message.
     *
     * @return A {@link Mono} the finishes this operation on service bus resource.
     *
     * @throws NullPointerException if {@code message} or {@code sessionId} is {@code null}.
     */
    public Mono<Void> send(ServiceBusMessage message, String sessionId) {
        Objects.requireNonNull(message, "'message' cannot be null.");
        Objects.requireNonNull(sessionId, "'sessionId' cannot be null.");

        //TODO (hemanttanwar): Implement session id feature.
        return Mono.error(new IllegalStateException("Not implemented."));
    }

    /**
     * Creates a {@link ServiceBusMessageBatch} that can fit as many messages as the transport allows.
     *
     * @return A {@link ServiceBusMessageBatch} that can fit as many messages as the transport allows.
     */
    public Mono<ServiceBusMessageBatch> createBatch() {
        return createBatch(DEFAULT_BATCH_OPTIONS);
    }

    /**
     * Creates an {@link ServiceBusMessageBatch} configured with the options specified.
     *
     * @param options A set of options used to configure the {@link ServiceBusMessageBatch}.
     * @return A new {@link ServiceBusMessageBatch} configured with the given options.
     * @throws NullPointerException if {@code options} is null.
     */
    public Mono<ServiceBusMessageBatch> createBatch(CreateBatchOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null.");

        final int maxSize = options.getMaximumSizeInBytes();

        return getSendLink().flatMap(link -> link.getLinkSize().flatMap(size -> {
            final int maximumLinkSize = size > 0
                ? size
                : MAX_MESSAGE_LENGTH_BYTES;

            if (maxSize > maximumLinkSize) {
                return monoError(logger, new IllegalArgumentException(String.format(Locale.US,
                        "CreateBatchOptions.getMaximumSizeInBytes (%s bytes) is larger than the link size"
                            + " (%s bytes).", maxSize, maximumLinkSize)));
            }

            final int batchSize = maxSize > 0
                ? maxSize
                : maximumLinkSize;

            return Mono.just(
                new ServiceBusMessageBatch(batchSize, link::getErrorContext, tracerProvider, messageSerializer));
        }));
    }

    /**
     * Sends a message batch to the Azure Service Bus entity this sender is connected to.
     *
     * @param batch of messages which allows client to send maximum allowed size for a batch of messages.
     *
     * @return A {@link Mono} the finishes this operation on service bus resource.
     *
     * @throws NullPointerException if {@code batch} is {@code null}.
     */
    public Mono<Void> send(ServiceBusMessageBatch batch) {
        Objects.requireNonNull(batch, "'batch' cannot be null.");

        final boolean isTracingEnabled = tracerProvider.isEnabled();
        final AtomicReference<Context> parentContext = isTracingEnabled
            ? new AtomicReference<>(Context.NONE)
            : null;

        if (batch.getMessages().isEmpty()) {
            logger.info("Cannot send an EventBatch that is empty.");
            return Mono.empty();
        }

        logger.info("Sending batch with size[{}].", batch.getCount());

        Context sharedContext = null;
        final List<org.apache.qpid.proton.message.Message> messages = new ArrayList<>();

        for (int i = 0; i < batch.getMessages().size(); i++) {
            final ServiceBusMessage event = batch.getMessages().get(i);
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

    /**
     * Sends a scheduled message to the Azure Service Bus entity this sender is connected to. A scheduled message is
     * enqueued and made available to receivers only at the scheduled enqueue time.
     *
     * @param message Message to be sent to the Service Bus Queue.
     * @param scheduledEnqueueTime Instant at which the message should appear in the Service Bus queue or topic.
     *
     * @return The sequence number of the scheduled message which can be used to cancel the scheduling of the message.
     *
     * @throws NullPointerException if {@code message} or {@code scheduledEnqueueTime} is {@code null}.
     */
    public Mono<Long> scheduleMessage(ServiceBusMessage message, Instant scheduledEnqueueTime) {
        Objects.requireNonNull(message, "'message' cannot be null.");
        Objects.requireNonNull(scheduledEnqueueTime, "'scheduledEnqueueTime' cannot be null.");

        return getSendLink()
            .flatMap(link -> link.getLinkSize().flatMap(size -> {
                int maxSize =  size > 0
                    ? size
                    : MAX_MESSAGE_LENGTH_BYTES;

                return connectionProcessor
                    .flatMap(connection -> connection.getManagementNode(entityName, entityType))
                    .flatMap(managementNode -> managementNode.schedule(message, scheduledEnqueueTime, maxSize));
            }));
    }

    /**
     * Cancels the enqueuing of an already scheduled message, if it was not already enqueued.
     *
     * @param sequenceNumber of the scheduled message to cancel.
     *
     * @return The {@link Mono} that finishes this operation on service bus resource.
     */
    public Mono<Void> cancelScheduledMessage(long sequenceNumber) {
        return connectionProcessor
            .flatMap(connection -> connection.getManagementNode(entityName, entityType))
            .flatMap(managementNode -> managementNode.cancelScheduledMessage(sequenceNumber));
    }

    /**
     * Disposes of the {@link ServiceBusSenderAsyncClient}. If the client had a dedicated connection, the underlying
     * connection is also closed.
     */
    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        onClientClose.run();
    }

    private Mono<Void> sendInternal(Flux<ServiceBusMessage> messages) {
        return getSendLink()
            .flatMap(link -> link.getLinkSize()
                .flatMap(size -> {
                    final int batchSize = size > 0 ? size : MAX_MESSAGE_LENGTH_BYTES;
                    final CreateBatchOptions batchOptions = new CreateBatchOptions()
                        .setMaximumSizeInBytes(batchSize);
                    return messages.collect(new AmqpMessageCollector(batchOptions, 1,
                        link::getErrorContext, tracerProvider, messageSerializer));
                })
                .flatMap(list -> sendInternalBatch(Flux.fromIterable(list))));
    }

    private Mono<Void> sendInternalBatch(Flux<ServiceBusMessageBatch> eventBatches) {
        return eventBatches
            .flatMap(this::send)
            .then()
            .doOnError(error -> {
                logger.error("Error sending batch.", error);
            });
    }

    private Mono<AmqpSendLink> getSendLink() {
        return connectionProcessor
            .flatMap(connection -> connection.createSendLink(entityName, entityName, retryOptions));
    }

    private static class AmqpMessageCollector implements Collector<ServiceBusMessage, List<ServiceBusMessageBatch>,
        List<ServiceBusMessageBatch>> {
        private final int maxMessageSize;
        private final Integer maxNumberOfBatches;
        private final ErrorContextProvider contextProvider;
        private final TracerProvider tracerProvider;
        private final MessageSerializer serializer;

        private volatile ServiceBusMessageBatch currentBatch;

        AmqpMessageCollector(CreateBatchOptions options, Integer maxNumberOfBatches,
            ErrorContextProvider contextProvider, TracerProvider tracerProvider, MessageSerializer serializer) {
            this.maxNumberOfBatches = maxNumberOfBatches;
            this.maxMessageSize = options.getMaximumSizeInBytes() > 0
                ? options.getMaximumSizeInBytes()
                : MAX_MESSAGE_LENGTH_BYTES;
            this.contextProvider = contextProvider;
            this.tracerProvider = tracerProvider;
            this.serializer = serializer;

            currentBatch = new ServiceBusMessageBatch(maxMessageSize, contextProvider, tracerProvider, serializer);
        }

        @Override
        public Supplier<List<ServiceBusMessageBatch>> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<List<ServiceBusMessageBatch>, ServiceBusMessage> accumulator() {
            return (list, event) -> {
                ServiceBusMessageBatch batch = currentBatch;
                if (batch.tryAdd(event)) {
                    return;
                }

                if (maxNumberOfBatches != null && list.size() == maxNumberOfBatches) {
                    final String message = String.format(Locale.US,
                        "EventData does not fit into maximum number of batches. '%s'", maxNumberOfBatches);

                    throw new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, message,
                        contextProvider.getErrorContext());
                }

                currentBatch = new ServiceBusMessageBatch(maxMessageSize, contextProvider, tracerProvider, serializer);
                currentBatch.tryAdd(event);
                list.add(batch);
            };
        }

        @Override
        public BinaryOperator<List<ServiceBusMessageBatch>> combiner() {
            return (existing, another) -> {
                existing.addAll(another);
                return existing;
            };
        }

        @Override
        public Function<List<ServiceBusMessageBatch>, List<ServiceBusMessageBatch>> finisher() {
            return list -> {
                ServiceBusMessageBatch batch = currentBatch;
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
