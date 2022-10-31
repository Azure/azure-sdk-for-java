// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpConnection;
import com.azure.core.amqp.AmqpEndpointState;
import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
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
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static com.azure.core.amqp.implementation.AmqpLoggingUtils.addSignalTypeAndResult;
import static com.azure.core.amqp.implementation.AmqpLoggingUtils.createContextWithConnectionId;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.core.util.FluxUtil.monoError;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Represents a bidirectional link between the message broker and the client. Allows client to send a request to the
 * broker and receive the associated response. The {@link RequestResponseChannel} composes a proton-j {@link Sender}
 * link and {@link Receiver} link.
 */
public class RequestResponseChannel implements AsyncCloseable {

    private static final String MANAGEMENT_OPERATION_KEY = "operation";
    private final ClientLogger logger;

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

    private final AmqpRetryOptions retryOptions;
    private final String replyTo;
    private final String activeEndpointTimeoutMessage;
    private final MessageSerializer messageSerializer;
    // The API calls on proton-j entities (e.g., Sender, Receiver) must happen in the non-blocking thread
    // (aka ReactorThread) assigned to the connection's org.apache.qpid.proton.reactor.Reactor object.
    // The provider exposes ReactorDispatcher that can schedule such calls on the ReactorThread.
    private final ReactorProvider provider;

    private final AmqpMetricsProvider metricsProvider;
    private static final String START_SEND_TIME_CONTEXT_KEY = "send-start-time";
    private static final String OPERATION_CONTEXT_KEY = "amqpOperation";
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
        ReceiverSettleMode receiverSettleMode, AmqpMetricsProvider metricsProvider) {

        Map<String, Object> loggingContext = createContextWithConnectionId(connectionId);
        loggingContext.put(LINK_NAME_KEY, linkName);
        this.logger = new ClientLogger(RequestResponseChannel.class, loggingContext);

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

        this.metricsProvider = metricsProvider;

        // Subscribe to the events from endpoints (Sender, Receiver & Connection) and track the subscriptions.
        //
        //@formatter:off
        this.subscriptions = Disposables.composite(
            receiveLinkHandler.getDeliveredMessages()
                .map(this::decodeDelivery)
                .subscribe(message -> {
                    logger.atVerbose()
                        .addKeyValue("messageId", message.getCorrelationId())
                        .log("Settling message.");

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

            // To ensure graceful closure of request-response-channel instance that won the race between
            // its creation and its parent connection close.
            amqpConnection.getShutdownSignals().next().flatMap(signal -> {
                logger.verbose("Shutdown signal received.");
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
            throw logger.logExceptionAsWarning(new RuntimeException("Unable to open send and receive link.", e));
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
                    logger.info("Timed out waiting for RequestResponseChannel to complete closing. Manually closing.");

                    onTerminalState("SendLinkHandler");
                    onTerminalState("ReceiveLinkHandler");
                });
            })
            .subscribeOn(Schedulers.boundedElastic());

        if (isDisposed.getAndSet(true)) {
            logger.verbose("Channel already closed.");
            return closeOperationWithTimeout;
        }

        logger.verbose("Closing request/response channel.");

        return Mono.fromRunnable(() -> {
            try {
                // Schedule API calls on proton-j entities on the ReactorThread associated with the connection.
                provider.getReactorDispatcher().invoke(() -> {
                    logger.verbose("Closing send link and receive link.");

                    sendLink.close();
                    receiveLink.close();
                });
            } catch (IOException | RejectedExecutionException e) {
                logger.info("Unable to schedule close work. Closing manually.");

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
            return monoError(logger, new RequestResponseChannelClosedException());
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
            .then(captureStartTime(message, Mono.create(sink -> {
                try {
                    logger.atVerbose()
                        .addKeyValue("messageId", message.getCorrelationId())
                        .log("Scheduling on dispatcher.");
                    unconfirmedSends.putIfAbsent(messageId, sink);

                    // Schedule API calls on proton-j entities on the ReactorThread associated with the connection.
                    provider.getReactorDispatcher().invoke(() -> {
                        if (isDisposed()) {
                            sink.error(new RequestResponseChannelClosedException(sendLink.getLocalState(),
                                receiveLink.getLocalState()));
                            return;
                        }

                        final Delivery delivery = sendLink.delivery(UUID.randomUUID().toString()
                            .replace("-", "").getBytes(UTF_8));

                        if (deliveryState != null) {
                            logger.atVerbose()
                                .addKeyValue("state", deliveryState)
                                .log("Setting delivery state.");
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
                    recordDelivery(getSinkContext(sink), null);
                    sink.error(e);
                }
            })));
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
            logger.atWarning()
                .addKeyValue("messageId", id)
                .log("Received delivery without pending message.");
            return;
        }

        recordDelivery(getSinkContext(sink), message);
        sink.success(message);
    }

    private void handleError(Throwable error, String message) {
        if (hasError.getAndSet(true)) {
            return;
        }

        logger.atWarning()
            .log("{} Disposing unconfirmed sends.", message, error);

        endpointStates.emitError(error, (signalType, emitResult) -> {
            addSignalTypeAndResult(logger.atWarning(), signalType, emitResult)
                .log("Could not emit error to sink.");

            return false;
        });

        terminateUnconfirmedSends(error);

        closeAsync().subscribe();
    }

    private void onTerminalState(String handlerName) {
        if (pendingLinkTerminations.get() <= 0) {
            logger.atVerbose()
                .log("Already disposed send/receive links.");

            return;
        }

        final int remaining = pendingLinkTerminations.decrementAndGet();
        logger.verbose("{} disposed. Remaining: {}", handlerName, remaining);

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
        addSignalTypeAndResult(logger.atVerbose(), signalType, emitResult)
            .log(message);
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

        logger.atVerbose()
            .addKeyValue("sendState", this.sendLinkState)
            .addKeyValue("receiveState", this.receiveLinkState)
            .log("Updating endpoint states.");

        if (this.sendLinkState == this.receiveLinkState) {
            this.endpointStates.emitNext(this.sendLinkState, Sinks.EmitFailureHandler.FAIL_FAST);
        }
    }

    // Terminate the unconfirmed MonoSinks by notifying the given error.
    private void terminateUnconfirmedSends(Throwable error) {
        logger.verbose("Terminating {} unconfirmed sends (reason: {}).", unconfirmedSends.size(), error.getMessage());
        Map.Entry<UnsignedLong, MonoSink<Message>> next;
        int count = 0;
        while ((next = unconfirmedSends.pollFirstEntry()) != null) {
            // pollFirstEntry: atomic retrieve and remove of each entry.
            MonoSink<Message> sink = next.getValue();
            recordDelivery(getSinkContext(sink), null);
            sink.error(error);
            count++;
        }

        // The below log can also help debug if the external code that error() calls into never return.
        logger.atVerbose()
            .log("completed the termination of {} unconfirmed sends (reason: {}).",  count, error.getMessage());
    }

    /**
     * Captures current time in mono context - used to report send metric
     */
    private Mono<Message> captureStartTime(Message toSend, Mono<Message> publisher) {
        if (metricsProvider.isRequestResponseDurationEnabled()) {
            String operationName = "unknown";
            if (toSend != null && toSend.getApplicationProperties() != null && toSend.getApplicationProperties().getValue() != null) {
                Map<String, Object> properties = toSend.getApplicationProperties().getValue();
                Object operationObj = properties.get(MANAGEMENT_OPERATION_KEY);
                if (operationObj instanceof String) {
                    operationName = (String) operationObj;
                }
            }

            return publisher.contextWrite(
                Context.of(START_SEND_TIME_CONTEXT_KEY, Instant.now()).put(OPERATION_CONTEXT_KEY, operationName));
        }

        return publisher;
    }

    @SuppressWarnings("deprecation")
    private static ContextView getSinkContext(MonoSink<?> sink) {
        // Use currentContext instead of contextView as it's supported back to Reactor 3.4.0 and gives the widest
        // range of support possible.
        return sink.currentContext();
    }

    /**
     * Records send call duration metric.
     **/
    private void recordDelivery(ContextView context, Message response) {
        if (metricsProvider.isRequestResponseDurationEnabled()) {
            Object startTimestamp = context.getOrDefault(START_SEND_TIME_CONTEXT_KEY, null);
            Object operationName = context.getOrDefault(OPERATION_CONTEXT_KEY, null);
            AmqpResponseCode responseCode = response == null ? null : RequestResponseUtils.getStatusCode(response);
            if (startTimestamp instanceof Instant && operationName instanceof String) {
                metricsProvider.recordRequestResponseDuration(
                    ((Instant) startTimestamp).toEpochMilli(),
                    (String) operationName,
                    responseCode);
            }
        }
    }
}
