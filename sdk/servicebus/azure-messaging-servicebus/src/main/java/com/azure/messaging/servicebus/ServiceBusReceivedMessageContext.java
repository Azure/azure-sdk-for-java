// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.DeferOptions;
import reactor.core.publisher.Mono;

import java.util.Objects;

/**
 * The Service Bus processor message context that holds a received message and additional methods to settle the message.
 */
public final class ServiceBusReceivedMessageContext {
    private final ServiceBusMessageContext receivedMessageContext;
    private final ServiceBusReceiverAsyncClient receiverClient;
    private final SessionsMessagePump.SessionReceiversTracker sessionReceivers;
    private final String fullyQualifiedNamespace;
    private final String entityPath;

    ServiceBusReceivedMessageContext(ServiceBusReceiverAsyncClient receiverClient,
        ServiceBusMessageContext receivedMessageContext) {
        this.receivedMessageContext
            = Objects.requireNonNull(receivedMessageContext, "'receivedMessageContext' cannot be null");
        this.receiverClient = Objects.requireNonNull(receiverClient, "'receiverClient' cannot be null");
        this.sessionReceivers = null;
        entityPath = receiverClient.getEntityPath();
        fullyQualifiedNamespace = receiverClient.getFullyQualifiedNamespace();
    }

    ServiceBusReceivedMessageContext(SessionsMessagePump.SessionReceiversTracker sessionReceivers,
        ServiceBusMessageContext receivedMessageContext) {
        this.receivedMessageContext = receivedMessageContext;
        this.sessionReceivers = sessionReceivers;
        this.receiverClient = null;
        entityPath = sessionReceivers.getEntityPath();
        fullyQualifiedNamespace = sessionReceivers.getFullyQualifiedNamespace();
    }

    /**
     * Gets the message received from Service Bus.
     *
     * @return The message received from Service Bus.
     */
    public ServiceBusReceivedMessage getMessage() {
        return receivedMessageContext.getMessage();
    }

    /**
     * Gets the Service Bus resource this instance of {@link ServiceBusProcessorClient} interacts with.
     *
     * @return The Service Bus resource this instance of {@link ServiceBusProcessorClient} interacts with.
     */
    public String getEntityPath() {
        return this.entityPath;
    }

    /**
     * Gets the fully qualified Service Bus namespace that this instance of {@link ServiceBusProcessorClient}
     * is associated with. This is likely similar to {@code {yournamespace}.servicebus.windows.net}.
     *
     * @return The fully qualified Service Bus namespace that this instance of {@link ServiceBusProcessorClient}
     * is associated with.
     */
    public String getFullyQualifiedNamespace() {
        return this.fullyQualifiedNamespace;
    }

    /**
     * Abandons the {@link #getMessage() message} in this context.
     */
    public void abandon() {
        abandon(new AbandonOptions());
    }

    /**
     * Abandons the {@link #getMessage() message} in this context.
     *
     * @param options Additional options for abandoning the message.
     * @throws NullPointerException if {@code options} are null.
     */
    public void abandon(AbandonOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null");
        if (sessionReceivers != null) {
            sessionReceivers.abandon(receivedMessageContext.getMessage(), options).block();
            return;
        }
        receiverClient.abandon(receivedMessageContext.getMessage(), options).block();
    }

    /**
     * Completes the {@link #getMessage() message} in this context.
     */
    public void complete() {
        complete(new CompleteOptions());
    }

    /**
     * Completes the {@link #getMessage() message} in this context.
     *
     * @param options Additional options for completing the message.
     * @throws NullPointerException if {@code options} are null.
     */
    public void complete(CompleteOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null");
        if (sessionReceivers != null) {
            sessionReceivers.complete(receivedMessageContext.getMessage(), options).block();
            return;
        }
        receiverClient.complete(receivedMessageContext.getMessage(), options).block();
    }

    /**
     * Defers the {@link #getMessage() message} in this context.
     */
    public void defer() {
        defer(new DeferOptions());
    }

    /**
     * Defers the {@link #getMessage() message} in this context.
     *
     * @param options Additional options for deferring the message.
     * @throws NullPointerException if {@code options} are null.
     */
    public void defer(DeferOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null");
        if (sessionReceivers != null) {
            sessionReceivers.defer(receivedMessageContext.getMessage(), options).block();
            return;
        }
        receiverClient.defer(receivedMessageContext.getMessage(), options).block();
    }

    /**
     * Dead-letters the {@link #getMessage() message} in this context.
     */
    public void deadLetter() {
        deadLetter(new DeadLetterOptions());
    }

    /**
     * Dead-letters the {@link #getMessage() message} in this context.
     *
     * @param options Additional options for dead-lettering the message.
     * @throws NullPointerException if {@code options} are null.
     */
    public void deadLetter(DeadLetterOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null");
        if (sessionReceivers != null) {
            sessionReceivers.deadLetter(receivedMessageContext.getMessage(), options).block();
            return;
        }
        receiverClient.deadLetter(receivedMessageContext.getMessage(), options).block();
    }

    /**
     * Abandons the {@link #getMessage() message} in this context.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> abandonAsync() {
        return abandonAsync(new AbandonOptions());
    }

    /**
     * Abandons the {@link #getMessage() message} in this context.
     *
     * @param options Additional options for abandoning the message.
     * @throws NullPointerException if {@code options} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> abandonAsync(AbandonOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null");
        if (sessionReceivers != null) {
            return sessionReceivers.abandon(receivedMessageContext.getMessage(), options);
        }
        return receiverClient.abandon(receivedMessageContext.getMessage(), options);
    }

    /**
     * Completes the {@link #getMessage() message} in this context.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> completeAsync() {
        return completeAsync(new CompleteOptions());
    }

    /**
     * Completes the {@link #getMessage() message} in this context.
     *
     * @param options Additional options for completing the message.
     * @throws NullPointerException if {@code options} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> completeAsync(CompleteOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null");
        if (sessionReceivers != null) {
            return sessionReceivers.complete(receivedMessageContext.getMessage(), options);
        }
        return receiverClient.complete(receivedMessageContext.getMessage(), options);
    }

    /**
     * Defers the {@link #getMessage() message} in this context.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deferAsync() {
        return deferAsync(new DeferOptions());
    }

    /**
     * Defers the {@link #getMessage() message} in this context.
     *
     * @param options Additional options for deferring the message.
     * @throws NullPointerException if {@code options} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deferAsync(DeferOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null");
        if (sessionReceivers != null) {
            return sessionReceivers.defer(receivedMessageContext.getMessage(), options);
        }
        return receiverClient.defer(receivedMessageContext.getMessage(), options);
    }

    /**
     * Dead-letters the {@link #getMessage() message} in this context.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deadLetterAsync() {
        return deadLetterAsync(new DeadLetterOptions());
    }

    /**
     * Dead-letters the {@link #getMessage() message} in this context.
     *
     * @param options Additional options for dead-lettering the message.
     * @throws NullPointerException if {@code options} are null.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deadLetterAsync(DeadLetterOptions options) {
        Objects.requireNonNull(options, "'options' cannot be null");
        if (sessionReceivers != null) {
            return sessionReceivers.deadLetter(receivedMessageContext.getMessage(), options);
        }
        return receiverClient.deadLetter(receivedMessageContext.getMessage(), options);
    }

}
