// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.amqp.implementation;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.AmqpRetryPolicy;
import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.amqp.exception.AmqpResponseCode;
import com.azure.core.amqp.implementation.handler.ReceiveLinkHandler;
import com.azure.core.amqp.implementation.handler.SendLinkHandler;
import com.azure.core.util.logging.ClientLogger;
import org.apache.qpid.proton.Proton;
import org.apache.qpid.proton.amqp.UnsignedLong;
import org.apache.qpid.proton.amqp.messaging.Source;
import org.apache.qpid.proton.amqp.messaging.Target;
import org.apache.qpid.proton.amqp.transport.ReceiverSettleMode;
import org.apache.qpid.proton.amqp.transport.SenderSettleMode;
import org.apache.qpid.proton.engine.BaseHandler;
import org.apache.qpid.proton.engine.Delivery;
import org.apache.qpid.proton.engine.EndpointState;
import org.apache.qpid.proton.engine.Receiver;
import org.apache.qpid.proton.engine.Sender;
import org.apache.qpid.proton.engine.Session;
import org.apache.qpid.proton.message.Message;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.Closeable;
import java.io.IOException;
import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Represents a bidirectional link between the message broker and the client. Allows client to send a request to the
 * broker and receive the associated response.
 */
public class RequestResponseChannel implements Closeable {
    private static final String STATUS_CODE = "status-code";
    private static final String STATUS_DESCRIPTION = "status-description";

    private final ConcurrentSkipListMap<UnsignedLong, MonoSink<Message>> unconfirmedSends =
        new ConcurrentSkipListMap<>();
    private final ClientLogger logger = new ClientLogger(RequestResponseChannel.class);

    private final Sender sendLink;
    private final Receiver receiveLink;
    private final String replyTo;
    private final MessageSerializer messageSerializer;
    private final ReactorProvider provider;
    private final Duration operationTimeout;
    private final AtomicBoolean hasOpened = new AtomicBoolean();
    private final AtomicLong requestId = new AtomicLong(0);
    private final SendLinkHandler sendLinkHandler;
    private final ReceiveLinkHandler receiveLinkHandler;
    private final Disposable subscription;
    private final AmqpRetryPolicy retryPolicy;

    /**
     * Creates a new instance of {@link RequestResponseChannel} to send and receive responses from the
     * {@code entityPath} in the message broker.
     *
     * @param connectionId Identifier of the connection.
     * @param fullyQualifiedNamespace Fully qualified namespace for the the host.
     * @param linkName Name of the link.
     * @param entityPath Address in the message broker to send message to.
     * @param session Reactor session associated with this link.
     * @param retryOptions Retry options to use for sending the request response.
     * @param handlerProvider Provides handlers that interact with proton-j's reactor.
     * @param provider The reactor provider that the request will be sent with.
     */
    public RequestResponseChannel(String connectionId, String fullyQualifiedNamespace, String linkName,
            String entityPath, Session session, AmqpRetryOptions retryOptions, ReactorHandlerProvider handlerProvider,
            ReactorProvider provider, MessageSerializer messageSerializer) {
        this.provider = provider;
        this.operationTimeout = retryOptions.getTryTimeout();
        this.retryPolicy = RetryUtil.getRetryPolicy(retryOptions);

        this.replyTo = entityPath.replace("$", "") + "-client-reply-to";
        this.messageSerializer = messageSerializer;
        this.sendLink = session.sender(linkName + ":sender");
        final Target target = new Target();
        target.setAddress(entityPath);
        this.sendLink.setTarget(target);
        sendLink.setSource(new Source());
        this.sendLink.setSenderSettleMode(SenderSettleMode.SETTLED);
        this.sendLinkHandler = handlerProvider.createSendLinkHandler(connectionId, fullyQualifiedNamespace, linkName,
            entityPath);
        BaseHandler.setHandler(sendLink, sendLinkHandler);

        this.receiveLink = session.receiver(linkName + ":receiver");
        final Source source = new Source();
        source.setAddress(entityPath);
        this.receiveLink.setSource(source);
        final Target receiverTarget = new Target();
        receiverTarget.setAddress(replyTo);
        this.receiveLink.setTarget(receiverTarget);
        this.receiveLink.setSenderSettleMode(SenderSettleMode.SETTLED);
        this.receiveLink.setReceiverSettleMode(ReceiverSettleMode.SECOND);
        this.receiveLinkHandler = handlerProvider.createReceiveLinkHandler(connectionId, fullyQualifiedNamespace,
            linkName, entityPath);
        BaseHandler.setHandler(this.receiveLink, receiveLinkHandler);

        this.subscription = receiveLinkHandler.getDeliveredMessages().map(this::decodeDelivery).subscribe(message -> {
            logger.verbose("Settling message: {}", message.getCorrelationId());
            settleMessage(message);
        }, this::handleException);
    }

    @Override
    public void close() {
        this.subscription.dispose();

        if (hasOpened.getAndSet(false)) {
            sendLink.close();
            receiveLink.close();
        }
    }

    /**
     * Sends a message to the message broker using the {@code dispatcher} and gets the response.
     *
     * @param message AMQP message to send.
     *
     * @return An AMQP message representing the service's response to the message.
     */
    public Mono<Message> sendWithAck(final Message message) {
        if (!hasOpened.getAndSet(true)) {
            sendLink.open();
            receiveLink.open();
        }

        if (message == null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("message cannot be null"));
        }
        if (message.getMessageId() != null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("message.getMessageId() should be null"));
        }
        if (message.getReplyTo() != null) {
            throw logger.logExceptionAsError(new IllegalArgumentException("message.getReplyTo() should be null"));
        }

        final UnsignedLong messageId = UnsignedLong.valueOf(requestId.incrementAndGet());
        message.setMessageId(messageId);
        message.setReplyTo(replyTo);

        return RetryUtil.withRetry(
            Mono.when(sendLinkHandler.getEndpointStates().takeUntil(x -> x == EndpointState.ACTIVE),
                receiveLinkHandler.getEndpointStates().takeUntil(x -> x == EndpointState.ACTIVE)),
            operationTimeout, retryPolicy)
            .then(
                Mono.create(sink -> {
                    try {
                        logger.verbose("Scheduling on dispatcher. Message Id {}", messageId);
                        unconfirmedSends.putIfAbsent(messageId, sink);

                        provider.getReactorDispatcher().invoke(() -> {
                            send(message);
                        });
                    } catch (IOException e) {
                        sink.error(e);
                    }
                }));
    }

    // Not thread-safe This must be invoked from reactor/dispatcher thread. And assumes that this is run on a link
    // that is open.
    private void send(final Message message) {
        sendLink.delivery(UUID.randomUUID().toString().replace("-", "").getBytes(UTF_8));

        final int payloadSize = messageSerializer.getSize(message)
            + ClientConstants.MAX_AMQP_HEADER_SIZE_BYTES;
        final byte[] bytes = new byte[payloadSize];
        final int encodedSize = message.encode(bytes, 0, payloadSize);

        receiveLink.flow(1);
        sendLink.send(bytes, 0, encodedSize);
        sendLink.advance();
    }

    private Message decodeDelivery(Delivery delivery) {
        final Message response = Proton.message();
        final int msgSize = delivery.pending();
        final byte[] buffer = new byte[msgSize];

        final int read = receiveLink.recv(buffer, 0, msgSize);

        response.decode(buffer, 0, read);
        delivery.settle();

        return response;
    }

    private void settleMessage(Message message) {
        final String id = String.valueOf(message.getCorrelationId());
        final UnsignedLong correlationId = UnsignedLong.valueOf(id);
        final MonoSink<Message> sink = unconfirmedSends.remove(correlationId);

        if (sink == null) {
            int size = unconfirmedSends.size();
            logger.warning("Received delivery without pending messageId[{}]. Size[{}]", id, size);
            return;
        }

        final int statusCode = (int) message.getApplicationProperties().getValue().get(STATUS_CODE);

        if (statusCode != AmqpResponseCode.ACCEPTED.getValue() && statusCode != AmqpResponseCode.OK.getValue()) {
            final String statusDescription =
                (String) message.getApplicationProperties().getValue().get(STATUS_DESCRIPTION);

            sink.error(ExceptionUtil.amqpResponseCodeToException(statusCode, statusDescription,
                receiveLinkHandler.getErrorContext(receiveLink)));
        } else {
            sink.success(message);
        }
    }

    private void handleException(Throwable error) {
        if (error instanceof AmqpException) {
            AmqpException exception = (AmqpException) error;

            if (!exception.isTransient()) {
                logger.error("Exception encountered. Closing channel and clearing unconfirmed sends.", exception);
                close();

                unconfirmedSends.forEach((key, value) -> {
                    value.error(error);
                });
            }
        }
    }
}
