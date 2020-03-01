// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpErrorCondition;
import com.azure.core.amqp.exception.AmqpException;

import com.azure.core.amqp.implementation.AmqpSendLink;
import com.azure.core.amqp.implementation.ErrorContextProvider;

import static com.azure.core.amqp.implementation.RetryUtil.getRetryPolicy;
import static com.azure.core.amqp.implementation.RetryUtil.withRetry;

import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.util.Context;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.messaging.servicebus.implementation.CreateBatchOptions;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import org.apache.qpid.proton.amqp.messaging.MessageAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Signal;

import java.io.Closeable;
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

import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;

/**
 * The client to send messages to Queue.
 */
@ServiceClient(builder = ServiceBusClientBuilder.class, isAsync = true)
public final class ServiceBusSenderAsyncClient implements Closeable {

    private final ClientLogger logger = new ClientLogger(ServiceBusSenderAsyncClient.class);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final TracerProvider tracerProvider;
    private final MessageSerializer messageSerializer;
    private final AmqpRetryOptions retryOptions;
    private final AmqpRetryPolicy retryPolicy;
    private final String queueName;
    private final ServiceBusConnectionProcessor connectionProcessor;

    /**
     * The default maximum allowable size, in bytes, for a batch to be sent.
     */
    public static final int MAX_MESSAGE_LENGTH_BYTES = 256 * 1024;

    /**
     * Creates a new instance of this {@link ServiceBusSenderAsyncClient} that sends messages to
     */
    ServiceBusSenderAsyncClient(String queueName, ServiceBusConnectionProcessor connectionProcessor,
                                AmqpRetryOptions retryOptions, TracerProvider tracerProvider,
                                MessageSerializer messageSerializer) {
        // Caching the created link so we don't invoke another link creation.
        this.messageSerializer = Objects.requireNonNull(messageSerializer,
            "'messageSerializer' cannot be null.");
        this.retryOptions = Objects.requireNonNull(retryOptions, "'retryOptions' cannot be null.");
        this.queueName = Objects.requireNonNull(queueName, "'entityPath' cannot be null.");
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor,
            "'connectionProcessor' cannot be null.");
        this.tracerProvider = tracerProvider;
        this.retryPolicy = getRetryPolicy(retryOptions);
    }

    /**
     *
     * @param serviceBusMessage to be sent to Service Bus Queue.
     * @param sessionId the session id to associate with the message.
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> send(ServiceBusMessage serviceBusMessage, String sessionId) {
        //TODO(sessionid) Implement session id feature
        return send(Flux.just(serviceBusMessage));
    }

    /**
     *
     * @param serviceBusMessage to be sent Service Bus Queue.
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    public Mono<Void> send(ServiceBusMessage serviceBusMessage) {
        Objects.requireNonNull(serviceBusMessage, "'message' cannot be null.");
        return send(Flux.just(serviceBusMessage));
    }

    /**
     * Sends a set of messages to the associated Service Bus using a batched approach. If the size of messages exceed
     * the maximum size of a single batch, an exception will be triggered and the send will fail.
     * By default, the message size is the max amount allowed on the link.

     * @param messages to send to the Service Bus Queue..
     * @return A {@link Mono} that completes when all messages are pushed to the service.
     */
    public Mono<Void> send(Flux<ServiceBusMessage> messages) {
        Objects.requireNonNull(messages, "'messages' cannot be null.");

        return sendInternal(messages);
    }

    private Mono<Void> sendInternal(Flux<ServiceBusMessage> messages) {
        return getSendLink()
            .flatMap(link -> link.getLinkSize()
                .flatMap(size -> {
                    final int batchSize = size > 0 ? size : MAX_MESSAGE_LENGTH_BYTES;
                    final CreateBatchOptions batchOptions = new CreateBatchOptions()
                        .setMaximumSizeInBytes(batchSize);
                    return messages.collect(new AmqpMessageCollector(batchOptions, 1,
                        link::getErrorContext, tracerProvider));
                })
                .flatMap(list -> sendInternalBatch(Flux.fromIterable(list))));
    }

    private Mono<Void> sendInternalBatch(Flux<MessageBatch> eventBatches) {
        return eventBatches
            .flatMap(this::send)
            .then()
            .doOnError(error -> {
                logger.error("Error sending batch.", error);
            });
    }


    /**
     * Sends a scheduled message to the Azure Service Bus entity this sender is connected to. A scheduled message is
     * enqueued and made available to receivers only at the scheduled enqueue time.
     *
     * @param serviceBusMessage to be sent to the Service Bus Queue.
     * @param scheduledEnqueueTimeUtc Declares at which time the message should appear on the Service Bus Queue.
     * @return The sequence number of the scheduled message which can be used to cancel the scheduling of the message.
     */
    public Mono<Long> schedule(ServiceBusMessage serviceBusMessage, Instant scheduledEnqueueTimeUtc) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     * Cancels the enqueuing of an already sent scheduled message, if it was not already enqueued.
     * @param sequenceNumber of the scheduled message to cancel.
     * @return The {@link Mono} that finishes this operation on service bus resource.
     */
    public Mono<Void> cancelScheduledMessage(long sequenceNumber) {
        //TODO(feature-to-implement)
        return null;
    }

    /**
     *
     * @return The name of  the queue.
     */
    public String getQueueName() {
        return this.queueName;
    }

    /**
     * Sends a message batch to the Azure Service Bus entity this sender is connected to.
     *
     * @param batch of messages which allows client to send maximum allowed size for a batch of messages.
     * @return The {@link Mono} the finishes this operation on service bus resource.
     */
    Mono<Void> send(MessageBatch batch) {
        Objects.requireNonNull(batch, "'batch' cannot be null.");
        final boolean isTracingEnabled = tracerProvider.isEnabled();
        final AtomicReference<Context> parentContext = isTracingEnabled
            ? new AtomicReference<>(Context.NONE)
            : null;

        if (batch.getServiceBusMessageList().isEmpty()) {
            logger.info("Cannot send an EventBatch that is empty.");
            return Mono.empty();
        }

        logger.info("Sending batch with size[{}].", batch.getCount());

        Context sharedContext = null;
        final List<org.apache.qpid.proton.message.Message> messages = new ArrayList<>();

        for (int i = 0; i < batch.getServiceBusMessageList().size(); i++) {
            final ServiceBusMessage event = batch.getServiceBusMessageList().get(i);
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
        final String entityPath = queueName;
        final String linkName = queueName;

        return connectionProcessor
            .flatMap(connection -> connection.createSendLink(linkName, entityPath, retryOptions));
    }

    private static class AmqpMessageCollector implements Collector<ServiceBusMessage, List<MessageBatch>,
        List<MessageBatch>> {
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
        public BiConsumer<List<MessageBatch>, ServiceBusMessage> accumulator() {
            return (list, event) -> {
                MessageBatch batch = currentBatch;
                if (batch.tryAdd(event)) {
                    return;
                }

                if (maxNumberOfBatches != null && list.size() == maxNumberOfBatches) {
                    final String message = String.format(Locale.US,
                        "EventData does not fit into maximum number of batches. '%s'", maxNumberOfBatches);

                    throw new AmqpException(false, AmqpErrorCondition.LINK_PAYLOAD_SIZE_EXCEEDED, message,
                        contextProvider.getErrorContext());
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

    /**
     * Disposes of the {@link ServiceBusSenderAsyncClient}. If the client had a dedicated connection, the underlying
     * connection is also closed.
     */
    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }
        connectionProcessor.dispose();

    }
}
