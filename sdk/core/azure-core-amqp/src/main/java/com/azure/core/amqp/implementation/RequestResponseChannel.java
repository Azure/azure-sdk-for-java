// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.util.AsyncCloseable;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.messaging.Accepted;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.engine.impl.DeliveryImpl;
import org.apache.qpid.proton.message.Message;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.core.publisher.SignalType;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.core.util.FluxUtil.monoError;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Represents a bidirectional link between the message broker and the client. Allows client to send a request to the
 * broker and receive the associated response. The {@link RequestResponseChannel} composes a proton-j {@link Sender}
 * link and {@link Receiver} link.
 */
public class RequestResponseChannel implements AsyncCloseable {
    private final ClientLogger logger = new ClientLogger(RequestResponseChannel.class);

    private final Sender sendLink;
    private final Receiver receiveLink;
    private final SendLinkHandler sendLinkHandler;
    private final ReceiveLinkHandler receiveLinkHandler;
    private final SenderSettleMode senderSettleMode;
    // The request-response-channel endpoint states derived from the latest state of the send and receive links.
    private final Sinks.Many<AmqpEndpointState> endpointStates = Sinks.many().multicast().onBackpressureBuffer();
    // The latest state of the send and receive links.
    private volatile AmqpEndpointState sendLinkState;
    private volatile AmqpEndpointState receiveLinkState;
    // Generates unique Id for each message send over the request-response-channel.
    private final AtomicLong requestId = new AtomicLong(0);
    // Tracks the sends that are not yet acknowledged by the broker. Map key is the unique Id
    // of the send and value is the MonoSink to notify upon broker acknowledgment.
    private final ConcurrentSkipListMap<UnsignedLong, MonoSink<Message>> unconfirmedSends =
        new ConcurrentSkipListMap<>();

    // Tracks the count of links that is not terminated yet. Once both the receive and send links
    // are terminated (i.e. pendingLinkTerminations is zero), the request-response-channel is
    // considered as terminated.
    private final AtomicInteger pendingLinkTerminations = new AtomicInteger(2);
    // The Mono that completes once the request-response-channel is terminated.
    private final Sinks.One<Void> closeMono = Sinks.one();
    // A flag indicating that an error in either of the links caused link to terminate.
    private final AtomicBoolean hasError = new AtomicBoolean();
    // A flag indicating that the request-response-channel is closed (after the call to closeAsync()).
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    // Tracks all subscriptions listening for events from various endpoints (sender, receiver & connection),
    // those subscriptions should be disposed when the request-response-channel terminates.
    private final Disposable.Composite subscriptions;

    private final String connectionId;
    private final String linkName;
    private final AmqpRetryOptions retryOptions;
    private final String replyTo;
    private final String activeEndpointTimeoutMessage;
    private final MessageSerializer messageSerializer;
    // The API calls on proton-j entities (e.g., Sender, Receiver) must happen in the non-blocking thread
    // (aka ReactorThread) assigned to the connection's org.apache.qpid.proton.reactor.Reactor object.
    // The provider exposes ReactorDispatcher that can schedule such calls on the ReactorThread.
    private final ReactorProvider provider;

    /**
     * Creates a new instance of {@link RequestResponseChannel} to send and receive responses from the {@code
     * entityPath} in the message broker.
     *
     * @param connectionId Identifier of the connection.
     * @param fullyQualifiedNamespace Fully qualified namespace for the the host.
     * @param linkName Name of the link.
     * @param entityPath Address in the message broker to send message to.
     * @param session Reactor session associated with this link.
     * @param retryOptions Retry options to use for sending the request response.
     * @param handlerProvider Provides handlers that interact with proton-j's reactor.
     * @param provider The reactor provider that the request will be sent with.
     * @param senderSettleMode to set as {@link SenderSettleMode} on sender.
     * @param receiverSettleMode to set as {@link ReceiverSettleMode} on receiver.
     *
     * @throws RuntimeException if the send/receive links could not be locally scheduled to open.
     */
    protected RequestResponseChannel(AmqpConnection amqpConnection, String connectionId,
        String fullyQualifiedNamespace, String linkName, String entityPath, Session session,
        AmqpRetryOptions retryOptions, ReactorHandlerProvider handlerProvider, ReactorProvider provider,
        MessageSerializer messageSerializer, SenderSettleMode senderSettleMode,
        ReceiverSettleMode receiverSettleMode) {
        this.connectionId = connectionId;
        this.linkName = linkName;
        this.retryOptions = retryOptions;
        this.provider = provider;
        this.senderSettleMode = senderSettleMode;
        this.activeEndpointTimeoutMessage = String.format(
            "RequestResponseChannel connectionId[%s], linkName[%s]: Waiting for send and receive handler to be ACTIVE",
            connectionId, linkName);

        this.replyTo = entityPath.replace("$", "") + "-client-reply-to";
        this.messageSerializer = messageSerializer;

        // Setup send (request) link.
        this.sendLink = session.sender(linkName + ":sender");
        final Target senderTarget = new Target();
        senderTarget.setAddress(entityPath);
        this.sendLink.setTarget(senderTarget);
        this.sendLink.setSource(new Source());
        this.sendLink.setSenderSettleMode(senderSettleMode);

        this.sendLinkHandler = handlerProvider.createSendLinkHandler(connectionId, fullyQualifiedNamespace, linkName,
            entityPath);
        BaseHandler.setHandler(sendLink, sendLinkHandler);

        // Setup receive (response) link.
        this.receiveLink = session.receiver(linkName + ":receiver");
        final Source receiverSource = new Source();
        receiverSource.setAddress(entityPath);
        this.receiveLink.setSource(receiverSource);

        final Target receiverTarget = new Target();
        receiverTarget.setAddress(replyTo);
        this.receiveLink.setTarget(receiverTarget);
        this.receiveLink.setSenderSettleMode(senderSettleMode);
        this.receiveLink.setReceiverSettleMode(receiverSettleMode);

        this.receiveLinkHandler = handlerProvider.createReceiveLinkHandler(connectionId, fullyQualifiedNamespace,
            linkName, entityPath);
        BaseHandler.setHandler(receiveLink, receiveLinkHandler);

        // Subscribe to the events from endpoints (Sender, Receiver & Connection) and track the subscriptions.
        //
        //@formatter:off
        this.subscriptions = Disposables.composite(
            receiveLinkHandler.getDeliveredMessages()
                .map(this::decodeDelivery)
                .subscribe(message -> {
                    logger.verbose("connectionId[{}], linkName[{}]: Settling message: {}", connectionId, linkName,
                        message.getCorrelationId());

                    settleMessage(message);
                }),

            receiveLinkHandler.getEndpointStates().subscribe(state -> {
                updateEndpointState(null, AmqpEndpointStateUtil.getConnectionState(state));
            }, error -> {
                handleError(error, "Error in ReceiveLinkHandler.");
                onTerminalState("ReceiveLinkHandler");
            }, () -> {
                closeAsync().subscribe();
                onTerminalState("ReceiveLinkHandler");
            }),

            sendLinkHandler.getEndpointStates().subscribe(state -> {
                updateEndpointState(AmqpEndpointStateUtil.getConnectionState(state), null);
            }, error -> {
                handleError(error, "Error in SendLinkHandler.");
                onTerminalState("SendLinkHandler");
            }, () -> {
                closeAsync().subscribe();
                onTerminalState("SendLinkHandler");
            }),

            //TODO (conniey): Do we need this if we already close the request response nodes when the
            // connection.closeWork is executed? It would be preferred to get rid of this circular dependency.
            amqpConnection.getShutdownSignals().next().flatMap(signal -> {
                logger.verbose("connectionId[{}] linkName[{}]: Shutdown signal received.", connectionId, linkName);
                return closeAsync();
            }).subscribe()
        );
        //@formatter:on

        // Open send and receive links.
        //
        // Schedule API calls on proton-j entities on the ReactorThread associated with the connection.
        try {
            this.provider.getReactorDispatcher().invoke(() -> {
                this.sendLink.open();
                this.receiveLink.open();
            });
        } catch (IOException | RejectedExecutionException e) {
            throw logger.logExceptionAsError(new RuntimeException(String.format(
                "connectionId[%s], linkName[%s]: Unable to open send and receive link.", connectionId, linkName), e));
        }
    }

    /**
     * Gets the endpoint states for the request-response-channel.
     *
     * @return The endpoint states for the request-response-channel.
     */
    public Flux<AmqpEndpointState> getEndpointStates() {
        return endpointStates.asFlux();
    }

    @Override
    public Mono<Void> closeAsync() {
        final Mono<Void> closeOperationWithTimeout = closeMono.asMono()
            .timeout(retryOptions.getTryTimeout())
            .onErrorResume(TimeoutException.class, error -> {
                return Mono.fromRunnable(() -> {
                    logger.info("connectionId[{}] linkName[{}] Timed out waiting for RequestResponseChannel to complete"
                            + " closing. Manually closing.",
                        connectionId, linkName, error);

                    onTerminalState("SendLinkHandler");
                    onTerminalState("ReceiveLinkHandler");
                });
            })
            .subscribeOn(Schedulers.boundedElastic());

        if (isDisposed.getAndSet(true)) {
            logger.verbose("connectionId[{}] linkName[{}] Channel already closed.", connectionId, linkName);
            return closeOperationWithTimeout;
        }

        logger.verbose("connectionId[{}] linkName[{}] Closing request/response channel.", connectionId, linkName);

        return Mono.fromRunnable(() -> {
            try {
                // Schedule API calls on proton-j entities on the ReactorThread associated with the connection.
                provider.getReactorDispatcher().invoke(() -> {
                    logger.verbose("connectionId[{}] linkName[{}] Closing send link and receive link.",
                        connectionId, linkName);

                    sendLink.close();
                    receiveLink.close();
                });
            } catch (IOException | RejectedExecutionException e) {
                logger.info("connectionId[{}] linkName[{}] Unable to schedule close work. Closing manually.",
                    connectionId, linkName);
                sendLink.close();
                receiveLink.close();
            }
        }).subscribeOn(Schedulers.boundedElastic()).then(closeOperationWithTimeout);
    }

    public boolean isDisposed() {
        return isDisposed.get();
    }

    /**
     * Sends a message to the message broker using the {@code dispatcher} and gets the response.
     *
     * @param message AMQP message to send.
     *
     * @return An AMQP message representing the service's response to the message.
     */
    public Mono<Message> sendWithAck(final Message message) {
        return sendWithAck(message, null);
    }

    /**
     * Sends a message to the message broker using the {@code dispatcher} and gets the response.
     *
     * @param message AMQP message to send.
     * @param deliveryState Delivery state to be sent to service bus with message.
     *
     * @return An AMQP message representing the service's response to the message.
     */
    public Mono<Message> sendWithAck(final Message message, DeliveryState deliveryState) {
        if (isDisposed()) {
            return monoError(logger, new IllegalStateException(
                "Cannot send a message when request response channel is disposed."));
        }

        if (message == null) {
            return monoError(logger, new NullPointerException("message cannot be null"));
        }
        if (message.getMessageId() != null) {
            return monoError(logger, new IllegalArgumentException("message.getMessageId() should be null"));
        }
        if (message.getReplyTo() != null) {
            return monoError(logger, new IllegalArgumentException("message.getReplyTo() should be null"));
        }

        final UnsignedLong messageId = UnsignedLong.valueOf(requestId.incrementAndGet());
        message.setMessageId(messageId);
        message.setReplyTo(replyTo);

        final Mono<Void> onActiveEndpoints = Mono.when(
            sendLinkHandler.getEndpointStates().takeUntil(x -> x == EndpointState.ACTIVE),
            receiveLinkHandler.getEndpointStates().takeUntil(x -> x == EndpointState.ACTIVE));

        return RetryUtil.withRetry(onActiveEndpoints, retryOptions, activeEndpointTimeoutMessage)
            .then(Mono.create(sink -> {
                try {
                    logger.verbose("connectionId[{}], linkName[{}]: Scheduling on dispatcher. MessageId[{}]",
                        connectionId, linkName, messageId);
                    unconfirmedSends.putIfAbsent(messageId, sink);

                    // Schedule API calls on proton-j entities on the ReactorThread associated with the connection.
                    provider.getReactorDispatcher().invoke(() -> {
                        final Delivery delivery = sendLink.delivery(UUID.randomUUID().toString()
                            .replace("-", "").getBytes(UTF_8));

                        if (deliveryState != null) {
                            logger.verbose("connectionId[{}], linkName[{}]: Setting delivery state as [{}].",
                                connectionId, linkName, deliveryState);
                            delivery.setMessageFormat(DeliveryImpl.DEFAULT_MESSAGE_FORMAT);
                            delivery.disposition(deliveryState);
                        }

                        final int payloadSize = messageSerializer.getSize(message)
                            + ClientConstants.MAX_AMQP_HEADER_SIZE_BYTES;
                        final byte[] bytes = new byte[payloadSize];
                        final int encodedSize = message.encode(bytes, 0, payloadSize);
                        receiveLink.flow(1);
                        sendLink.send(bytes, 0, encodedSize);
                        delivery.settle();
                        sendLink.advance();
                    });
                } catch (IOException | RejectedExecutionException e) {
                    sink.error(e);
                }
            }));
    }

    /**
     * Gets the error context for the channel.
     *
     * @return The error context for the channel.
     */
    public AmqpErrorContext getErrorContext() {
        return receiveLinkHandler.getErrorContext(receiveLink);
    }

    protected Message decodeDelivery(Delivery delivery) {
        final Message response = Proton.message();
        final int msgSize = delivery.pending();
        final byte[] buffer = new byte[msgSize];

        final int read = receiveLink.recv(buffer, 0, msgSize);

        response.decode(buffer, 0, read);
        if (this.senderSettleMode == SenderSettleMode.SETTLED) {
            // No op. Delivery comes settled from the sender
            delivery.disposition(Accepted.getInstance());
            delivery.settle();
        }
        return response;
    }

    private void settleMessage(Message message) {
        final String id = String.valueOf(message.getCorrelationId());
        final UnsignedLong correlationId = UnsignedLong.valueOf(id);
        final MonoSink<Message> sink = unconfirmedSends.remove(correlationId);

        if (sink == null) {
            int size = unconfirmedSends.size();
            logger.warning("connectionId[{}] linkName[{}] Received delivery without pending messageId[{}]. size[{}]",
                connectionId, linkName, id, size);
            return;
        }

        sink.success(message);
    }

    private void handleError(Throwable error, String message) {
        if (hasError.getAndSet(true)) {
            return;
        }

        logger.error("connectionId[{}] linkName[{}] {} Disposing unconfirmed sends.", connectionId, linkName, message,
            error);

        endpointStates.emitError(error, (signalType, emitResult) -> {
            logger.warning("connectionId[{}] linkName[{}] signal[{}] result[{}] Could not emit error to sink.",
                connectionId, linkName, signalType, emitResult);
            return false;
        });

        terminateUnconfirmedSends(error);

        closeAsync().subscribe();
    }

    private void onTerminalState(String handlerName) {
        if (pendingLinkTerminations.get() <= 0) {
            logger.verbose("connectionId[{}] linkName[{}]: Already disposed send/receive links.");
            return;
        }

        final int remaining = pendingLinkTerminations.decrementAndGet();
        logger.verbose("connectionId[{}] linkName[{}]: {} disposed. Remaining: {}",
            connectionId, linkName, handlerName, remaining);

        if (remaining == 0) {
            subscriptions.dispose();

            terminateUnconfirmedSends(new AmqpException(true,
                "The RequestResponseChannel didn't receive the acknowledgment for the send due receive link termination.",
                null));

            endpointStates.emitComplete(((signalType, emitResult) -> onEmitSinkFailure(signalType, emitResult,
                "Could not emit complete signal.")));

            closeMono.emitEmpty((signalType, emitResult) -> onEmitSinkFailure(signalType, emitResult,
                handlerName + ". Error closing mono."));
        }
    }

    private boolean onEmitSinkFailure(SignalType signalType, Sinks.EmitResult emitResult, String message) {
        logger.verbose("connectionId[{}] linkName[{}] signal[{}] result[{}] {}",
            connectionId, linkName, signalType, emitResult, message);

        return false;
    }

    // Derive and emits the endpoint state for this RequestResponseChannel from the current endpoint state
    // of send and receive links.
    private synchronized void updateEndpointState(AmqpEndpointState sendLinkState, AmqpEndpointState receiveLinkState) {
        if (sendLinkState != null) {
            this.sendLinkState = sendLinkState;
        } else if (receiveLinkState != null) {
            this.receiveLinkState = receiveLinkState;
        }

        logger.verbose("connectionId[{}] linkName[{}] sendState[{}] receiveState[{}] Updating endpoint states.",
            connectionId, linkName, this.sendLinkState, this.receiveLinkState);

        if (this.sendLinkState == this.receiveLinkState) {
            this.endpointStates.emitNext(this.sendLinkState, Sinks.EmitFailureHandler.FAIL_FAST);
        }
    }

    // Terminate the unconfirmed MonoSinks by notifying the given error.
    private void terminateUnconfirmedSends(Throwable error) {
        logger.verbose("connectionId[{}] linkName[{}] terminating {} unconfirmed sends (reason: {}).",
            connectionId, linkName, unconfirmedSends.size(), error.getMessage());
        Map.Entry<UnsignedLong, MonoSink<Message>> next;
        int count = 0;
        while ((next = unconfirmedSends.pollFirstEntry()) != null) {
            // pollFirstEntry: atomic retrieve and remove of each entry.
            next.getValue().error(error);
            count++;
        }
        // The below log can also help debug if the external code that error() calls into never return.
        logger.verbose("connectionId[{}] linkName[{}] completed the termination of {} unconfirmed sends (reason: {}).",
            connectionId, linkName, count, error.getMessage());
    }
}
