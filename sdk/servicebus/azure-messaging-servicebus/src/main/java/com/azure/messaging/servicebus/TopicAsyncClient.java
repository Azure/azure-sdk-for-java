package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.SendOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.amqp.implementation.TracerProvider;
import com.azure.core.annotation.ServiceClient;
import com.azure.messaging.servicebus.implementation.ServiceBusConnectionProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

@ServiceClient(builder = TopicClientBuilder.class, isAsync = true)
public final class TopicAsyncClient implements Closeable {

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final boolean isSharedConnection;
    private final ServiceBusConnectionProcessor connectionProcessor;
    private static final SendOptions DEFAULT_SEND_OPTIONS = new SendOptions();

    /**
     * Creates a new instance of this {@link QueueSenderAsyncClient} that sends messages to
     */
    TopicAsyncClient(String entityPath, ServiceBusConnectionProcessor connectionProcessor, SendOptions options,
                           AmqpRetryOptions retryOptions, TracerProvider tracerProvider,
                           MessageSerializer messageSerializer, boolean isSharedConnection) {
        this.connectionProcessor = Objects.requireNonNull(connectionProcessor,
            "'connectionProcessor' cannot be null.");
        this.isSharedConnection = isSharedConnection;
    }
    /**
     * Disposes of the {@link TopicAsyncClient}. If the client had a dedicated connection, the underlying
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

        public Mono<Void> send(Message message) {
            Objects.requireNonNull(message, "'event' cannot be null.");

            return send(Flux.just(message));
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

            return null;
        }

    public String getTopicName() {
            return null;
        }

    public Flux<Message> peek() {
        return null;
    }
    public Mono<Long> schedule(Message message, Instant scheduledEnqueueTimeUt){
        return null;
    }

    public Mono<Void> cancelScheduledMessage(long sequenceNumber){
        return null;
    }

    public Mono<Long> schedule(Message message, Instant scheduledEnqueueTimeUt, TransactionContext context){
        return null;
    }
    public Mono<Void> send(Message message, TransactionContext context){
        return null;
    }
}
