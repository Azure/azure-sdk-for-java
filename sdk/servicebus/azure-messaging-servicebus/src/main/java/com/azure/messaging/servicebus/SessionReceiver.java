// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.exception.AmqpErrorContext;
import com.azure.core.amqp.exception.LinkErrorContext;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.MessageLockContainer;
import com.azure.messaging.servicebus.implementation.MessageManagementOperations;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import com.azure.messaging.servicebus.implementation.ServiceBusMessageProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

class SessionReceiver implements AutoCloseable {
    // Start the peek from before the beginning of the stream.
    private final AtomicLong lastPeekedSequenceNumber = new AtomicLong(-1);
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final MessageLockContainer lockContainer;
    private final AtomicReference<Instant> sessionLockedUntil = new AtomicReference<>();
    private final AtomicReference<String> sessionId = new AtomicReference<>();
    private final ClientLogger logger = new ClientLogger(SessionReceiver.class);
    private final ServiceBusReceiveLink receiveLink;
    private final Disposable.Composite subscriptions;
    private final Flux<ServiceBusReceivedMessageContext> receivedMessages;

    SessionReceiver(ServiceBusReceiveLink receiveLink, MessageSerializer messageSerializer, boolean isAutoComplete,
        boolean autoLockRenewal, Duration maxAutoLockRenewDuration, AmqpRetryOptions retryOptions) {
        this.receiveLink = receiveLink;
        this.lockContainer = new MessageLockContainer(ServiceBusConstants.OPERATION_TIMEOUT);

        final AmqpErrorContext errorContext = new LinkErrorContext(receiveLink.getHostname(),
            receiveLink.getEntityPath(), null, null);
        final SessionMessageManagement messageManagement = new SessionMessageManagement(receiveLink);

        this.receivedMessages = receiveLink
            .receive()
            .map(message -> messageSerializer.deserialize(message, ServiceBusReceivedMessage.class))
            .subscribeWith(new ServiceBusMessageProcessor(receiveLink.getLinkName(), isAutoComplete, autoLockRenewal,
                maxAutoLockRenewDuration, retryOptions, errorContext, messageManagement))
            .map(message -> {
                if (!CoreUtils.isNullOrEmpty(message.getLockToken())) {
                    lockContainer.addOrUpdate(message.getLockToken(), message.getLockedUntil());
                }

                return new ServiceBusReceivedMessageContext(message);
            });

        this.subscriptions = Disposables.composite(
            receivedMessages.next().subscribe(first -> {
                logger.info("entityPath[{}]. Setting session id: {}", first.getSessionId());
                if (!sessionId.compareAndSet(null, first.getSessionId())) {
                    logger.warning("entityPath[{}]. Session id already set. Existing: {}", sessionId.get());
                } else {
                    logger.info("Session id: {}", first.getSessionId());
                }
            }),

            Flux.switchOnNext(receivedMessages
                .flatMap(message -> Mono.delay(retryOptions.getTryTimeout())
                    .handle((l, sink) -> {
                        logger.info("entityPath[{}]. sessionId[{}]. Closing. Did not receive message within timeout.",
                            receiveLink.getEntityPath(), sessionId.get());

                        close();
                        sink.complete();
                    })))
                .subscribe(),

            receiveLink.getSessionId().subscribe(id -> sessionId.set(id)),
            receiveLink.getSessionLockedUntil().subscribe(lockedUntil -> {
                if (!sessionLockedUntil.compareAndSet(null, lockedUntil)) {
                    logger.info("SessionLockedUntil was already set: {}", sessionLockedUntil);
                }
            })
        );
    }

    /**
     * Gets whether or not the receiver contains the lock token.
     *
     * @param lockToken Lock token for the message.
     * @return {@code true} if the session receiver contains the lock token to the unsettled delivery; {@code false}
     * otherwise.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws IllegalArgumentException if {@code lockToken} is empty.
     */
    boolean containsLockToken(String lockToken) {
        if (lockToken == null) {
            throw logger.logExceptionAsError(new NullPointerException("'lockToken' cannot be null."));
        } else if (lockToken.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'lockToken' cannot be an empty string."));
        }

        return lockContainer.contains(lockToken);
    }

    String getLinkName() {
        return receiveLink.getLinkName();
    }

    String getSessionId() {
        return sessionId.get();
    }

    /**
     * Receives messages from session.
     *
     * @return A flux of messages for the session.
     */
    Flux<ServiceBusReceivedMessageContext> receive() {
        return receivedMessages;
    }

    void setSessionLockedUntil(Instant lockedUntil) {
        sessionLockedUntil.set(lockedUntil);
    }

    long getLastPeekedSequenceNumber() {
        return lastPeekedSequenceNumber.get();
    }

    void setLastPeekedSequenceNumber(long sequenceNumber) {
        lastPeekedSequenceNumber.set(sequenceNumber);
    }

    Mono<Void> updateDisposition(String lockToken, DeliveryState deliveryState) {
        return receiveLink.updateDisposition(lockToken, deliveryState);
    }

    @Override
    public void close() {
        if (isDisposed.getAndSet(true)) {
            return;
        }

        receiveLink.dispose();
        subscriptions.dispose();
    }

    private static final class SessionMessageManagement implements MessageManagementOperations {
        private final ServiceBusReceiveLink link;

        private SessionMessageManagement(ServiceBusReceiveLink link) {
            this.link = link;
        }

        @Override
        public Mono<Void> updateDisposition(String lockToken, DeliveryState deliveryState) {
            return link.updateDisposition(lockToken, deliveryState);
        }

        @Override
        public Mono<Instant> renewMessageLock(String lockToken, String associatedLinkName) {
            return Mono.just(Instant.now().plusSeconds(60));
        }
    }
}
