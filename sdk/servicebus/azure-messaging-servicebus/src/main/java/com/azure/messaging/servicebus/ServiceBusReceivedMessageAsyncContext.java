// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.DeferOptions;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * A message received by {@link ServiceBusProcessorAsyncClient}, with non-blocking settlement operations.
 *
 * <p>Settlement operations are cold: subscribe to the returned {@link Mono}, or compose it into the message
 * handler's returned {@link Mono}. When using manual settlement, disable automatic completion on the processor.
 * Complete the message only after the application's work has succeeded. Lock loss can cause redelivery, so
 * processing should be idempotent.</p>
 */
public final class ServiceBusReceivedMessageAsyncContext {
    private final ServiceBusReceiverAsyncClient receiver;
    private final ServiceBusReceivedMessage message;

    ServiceBusReceivedMessageAsyncContext(ServiceBusReceiverAsyncClient receiver, ServiceBusMessageContext context) {
        this.receiver = Objects.requireNonNull(receiver, "'receiver' cannot be null");
        this.message = Objects.requireNonNull(context.getMessage(), "'message' cannot be null");
    }

    /**
     * Gets the received message.
     * @return The received message.
     */
    public ServiceBusReceivedMessage getMessage() {
        return message;
    }

    /**
     * Gets the queue or topic subscription path.
     * @return The entity path.
     */
    public String getEntityPath() {
        return receiver.getEntityPath();
    }

    /**
     * Gets the fully qualified namespace.
     * @return The fully qualified namespace, such as {@code namespace.servicebus.windows.net}.
     */
    public String getFullyQualifiedNamespace() {
        return receiver.getFullyQualifiedNamespace();
    }

    /**
     * Completes the message.
     * @return A cold operation that completes when the message has been settled.
     */
    public Mono<Void> complete() {
        return Mono.defer(() -> receiver.complete(message));
    }

    /**
     * Completes the message with additional options.
     * @param options Settlement options.
     * @return A cold operation that completes when the message has been settled.
     */
    public Mono<Void> complete(CompleteOptions options) {
        return Mono.defer(() -> receiver.complete(message, options));
    }

    /**
     * Abandons the message, making it available for redelivery.
     * @return A cold operation that completes when the message has been abandoned.
     */
    public Mono<Void> abandon() {
        return Mono.defer(() -> receiver.abandon(message));
    }

    /**
     * Abandons the message with additional options.
     * @param options Settlement options.
     * @return A cold operation that completes when the message has been abandoned.
     */
    public Mono<Void> abandon(AbandonOptions options) {
        return Mono.defer(() -> receiver.abandon(message, options));
    }

    /**
     * Defers the message for later retrieval by sequence number.
     * @return A cold operation that completes when the message has been deferred.
     */
    public Mono<Void> defer() {
        return Mono.defer(() -> receiver.defer(message));
    }

    /**
     * Defers the message with additional options.
     * @param options Settlement options.
     * @return A cold operation that completes when the message has been deferred.
     */
    public Mono<Void> defer(DeferOptions options) {
        return Mono.defer(() -> receiver.defer(message, options));
    }

    /**
     * Moves the message to the dead-letter queue.
     * @return A cold operation that completes when the message has been dead-lettered.
     */
    public Mono<Void> deadLetter() {
        return Mono.defer(() -> receiver.deadLetter(message));
    }

    /**
     * Moves the message to the dead-letter queue with additional options.
     * @param options Settlement options.
     * @return A cold operation that completes when the message has been dead-lettered.
     */
    public Mono<Void> deadLetter(DeadLetterOptions options) {
        return Mono.defer(() -> receiver.deadLetter(message, options));
    }
}
