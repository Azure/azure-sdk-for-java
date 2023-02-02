// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.DeferOptions;

import java.time.Duration;
import java.util.Objects;

/**
 * The Service Bus processor message context that holds a received message and additional methods to settle the message.
 */
public final class ServiceBusReceivedMessageContext {
    private final ServiceBusMessageContext receivedMessageContext;
    private final ServiceBusReceiverAsyncClient receiverClient;
    private final String fullyQualifiedNamespace;
    private final String entityPath;

    ServiceBusReceivedMessageContext(ServiceBusReceiverAsyncClient receiverClient,
                                     ServiceBusMessageContext receivedMessageContext) {
        this.receivedMessageContext = Objects.requireNonNull(receivedMessageContext,
            "'receivedMessageContext' cannot be null");
        this.receiverClient = Objects.requireNonNull(receiverClient, "'receiverClient' cannot be null");
        entityPath = receiverClient.getEntityPath();
        fullyQualifiedNamespace = receiverClient.getFullyQualifiedNamespace();
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
     *  Gets the Service Bus resource this instance of {@link ServiceBusProcessorClient} interacts with.
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
        receiverClient.abandon(receivedMessageContext.getMessage()).block();
    }

    /**
     * Abandons the {@link #getMessage() message} in this context.
     *
     * @param timeout the timeout for the block
     */
    public void abandon(Duration timeout) {
        receiverClient.abandon(receivedMessageContext.getMessage()).block(timeout);
    }

    /**
     * Abandons the {@link #getMessage() message} in this context.
     *
     * @param options Additional options for abandoning the message.
     */
    public void abandon(AbandonOptions options) {
        receiverClient.abandon(receivedMessageContext.getMessage(), options).block();
    }

    /**
     * Abandons the {@link #getMessage() message} in this context.
     *
     * @param options Additional options for abandoning the message.
     * @param timeout the timeout for the block
     */
    public void abandon(AbandonOptions options, Duration timeout) {
        receiverClient.abandon(receivedMessageContext.getMessage(), options).block(timeout);
    }

    /**
     * Completes the {@link #getMessage() message} in this context.
     */
    public void complete() {
        receiverClient.complete(receivedMessageContext.getMessage()).block();
    }

    /**
     * Completes the {@link #getMessage() message} in this context.
     *
     * @param timeout the timeout for the block
     */
    public void complete(Duration timeout) {
        receiverClient.complete(receivedMessageContext.getMessage()).block(timeout);
    }

    /**
     * Completes the {@link #getMessage() message} in this context.
     *
     * @param options Additional options for completing the message.
     * @throws NullPointerException if {@code options} are null.
     */
    public void complete(CompleteOptions options) {
        receiverClient.complete(receivedMessageContext.getMessage(), options).block();
    }

    /**
     * Completes the {@link #getMessage() message} in this context.
     *
     * @param options Additional options for completing the message.
     * @param timeout the timeout for the block
     * @throws NullPointerException if {@code options} are null.
     */
    public void complete(CompleteOptions options, Duration timeout) {
        receiverClient.complete(receivedMessageContext.getMessage(), options).block(timeout);
    }

    /**
     * Defers the {@link #getMessage() message} in this context.
     */
    public void defer() {
        receiverClient.defer(receivedMessageContext.getMessage()).block();
    }

    /**
     * Defers the {@link #getMessage() message} in this context.
     *
     * @param timeout the timeout for the block
     */
    public void defer(Duration timeout) {
        receiverClient.defer(receivedMessageContext.getMessage()).block(timeout);
    }

    /**
     * Defers the {@link #getMessage() message} in this context.
     *
     * @param options Additional options for deferring the message.
     * @throws NullPointerException if {@code options} are null.
     */
    public void defer(DeferOptions options) {
        receiverClient.defer(receivedMessageContext.getMessage(), options).block();
    }

    /**
     * Defers the {@link #getMessage() message} in this context.
     *
     * @param options Additional options for deferring the message.
     * @param timeout the timeout for the block
     * @throws NullPointerException if {@code options} are null.
     */
    public void defer(DeferOptions options, Duration timeout) {
        receiverClient.defer(receivedMessageContext.getMessage(), options).block(timeout);
    }

    /**
     * Dead-letters the {@link #getMessage() message} in this context.
     */
    public void deadLetter() {
        receiverClient.deadLetter(receivedMessageContext.getMessage()).block();
    }

    /**
     * Dead-letters the {@link #getMessage() message} in this context.
     *
     * @param timeout the timeout for the block
     */
    public void deadLetter(Duration timeout) {
        receiverClient.deadLetter(receivedMessageContext.getMessage()).block(timeout);
    }

    /**
     * Dead-letters the {@link #getMessage() message} in this context.
     *
     * @param options Additional options for dead-lettering the message.
     *
     * @throws NullPointerException if {@code options} are null.
     */
    public void deadLetter(DeadLetterOptions options) {
        receiverClient.deadLetter(receivedMessageContext.getMessage(), options).block();
    }

    /**
     * Dead-letters the {@link #getMessage() message} in this context.
     *
     * @param options Additional options for dead-lettering the message.
     * @param timeout the timeout for the block
     *
     * @throws NullPointerException if {@code options} are null.
     */
    public void deadLetter(DeadLetterOptions options, Duration timeout) {
        receiverClient.deadLetter(receivedMessageContext.getMessage(), options).block(timeout);
    }
}
