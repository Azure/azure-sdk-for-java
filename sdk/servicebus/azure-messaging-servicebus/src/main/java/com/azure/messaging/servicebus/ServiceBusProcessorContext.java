package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.models.AbandonOptions;
import com.azure.messaging.servicebus.models.CompleteOptions;
import com.azure.messaging.servicebus.models.DeadLetterOptions;
import com.azure.messaging.servicebus.models.DeferOptions;

import java.util.Objects;

/**
 * The Service Bus Processor context that holds a received message and additional methods to settle the message.
 */
public final class ServiceBusProcessorContext {

    private final ServiceBusReceivedMessageContext receivedMessageContext;
    private final ServiceBusReceiverAsyncClient receiverClient;

    ServiceBusProcessorContext(ServiceBusReceiverAsyncClient receiverClient,
                               ServiceBusReceivedMessageContext receivedMessageContext) {
        this.receivedMessageContext = Objects.requireNonNull(receivedMessageContext,
            "'receivedMessageContext' cannot be null");
        this.receiverClient = Objects.requireNonNull(receiverClient, "'receiverClient' cannot be null");
    }

    /**
     * Gets the session id of the message or that the error occurred in.
     * @return The session id associated with the error or message. {@code null} if there is no session.
     */
    public String getSessionId() {
        return receivedMessageContext.getSessionId();
    }

    /**
     * Gets the message received from Service Bus.
     * @return The message received from Service Bus or {@code null} if an exception occurred.
     */
    public ServiceBusReceivedMessage getMessage() {
        return receivedMessageContext.getMessage();
    }


    /**
     * Adandon the message in this context.
     */
    public void abandon() {
        receiverClient.abandon(receivedMessageContext.getMessage()).block();
    }

    /**
     * Adandon the message in this context.
     * @param options Additional options for abandoning the message.
     */
    public void abandon(AbandonOptions options) {
        receiverClient.abandon(receivedMessageContext.getMessage(), options).block();
    }

    /**
     * Completes the message in this context.
     */
    public void complete() {
        receiverClient.complete(receivedMessageContext.getMessage()).block();
    }

    /**
     * Completes the message in this context.
     * @param options Additional options for completing the message.
     */
    public void complete(CompleteOptions options) {
        receiverClient.complete(receivedMessageContext.getMessage(), options).block();
    }

    /**
     * Defers the message in this context.
     */
    public void defer() {
        receiverClient.defer(receivedMessageContext.getMessage()).block();
    }

    /**
     * Defers the message in this context.
     * @param options Additional options for defering the message.
     */
    public void defer(DeferOptions options) {
        receiverClient.defer(receivedMessageContext.getMessage(), options).block();
    }

    /**
     * Deadletters the message in this context.
     */
    public void deadLetter() {
        receiverClient.deadLetter(receivedMessageContext.getMessage()).block();
    }

    /**
     * Deadletters the message in this context.
     * @param options Additional options for deadlettering the message.
     */
    public void deadLetter(DeadLetterOptions options) {
        receiverClient.deadLetter(receivedMessageContext.getMessage(), options).block();
    }
}
