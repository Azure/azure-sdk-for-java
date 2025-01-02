// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.AsyncCloseable;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LoggingEventBuilder;
import com.azure.messaging.servicebus.implementation.LockContainer;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static com.azure.core.amqp.implementation.ClientConstants.ENTITY_PATH_KEY;
import static com.azure.core.amqp.implementation.ClientConstants.LINK_NAME_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.MESSAGE_ID_LOGGING_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SESSION_ID_KEY;

/**
 * Represents a session (in v1 stack) that is received when "any" session is accepted from the service.
 */
class ServiceBusSessionReceiver implements AsyncCloseable, AutoCloseable {
    private static final ClientLogger LOGGER = new ClientLogger(ServiceBusSessionReceiver.class);

    private final AtomicBoolean isDisposed = new AtomicBoolean();
    // Each session-specific receiver tracks the lock of the received messages via lock-container.
    // When the app uses SessionManager (multiplexing session receivers) and wants to perform message
    // disposition, SessionManager uses lock-container to find the session receiver owning the message.
    // The locks in the lock-container are cleaned up at fixed (service operation timeout) intervals;
    // also, the lock is removed after the completion of the message disposition.
    private final LockContainer<OffsetDateTime> lockContainer;
    private final AtomicReference<OffsetDateTime> sessionLockedUntil = new AtomicReference<>();
    private final AtomicReference<LockRenewalOperation> renewalOperation = new AtomicReference<>();
    private final String sessionId;
    private final ServiceBusReceiveLink receiveLink;
    private final Disposable.Composite subscriptions;
    private final Flux<ServiceBusMessageContext> receivedMessages;
    private final MonoProcessor<ServiceBusMessageContext> cancelReceiveProcessor = MonoProcessor.create();
    private final DirectProcessor<String> messageReceivedEmitter = DirectProcessor.create();
    private final FluxSink<String> messageReceivedSink = messageReceivedEmitter.sink(FluxSink.OverflowStrategy.BUFFER);
    private final AmqpRetryOptions retryOptions;

    /**
     * Creates a receiver for the first available session.
     *
     * @param receiveLink Service Bus receive link for available session.
     * @param sessionId Identifier of the Service Bus Session that the receiver is associated with.
     * @param messageSerializer Serializes and deserializes messages from Service Bus.
     * @param retryOptions Retry options for the receiver.
     * @param prefetch Number of messages to prefetch from session.
     * @param scheduler The scheduler to publish messages on.
     * @param renewSessionLock Function to renew the session lock.
     * @param maxSessionLockRenewDuration Maximum time to renew the session lock for. {@code null} or {@link
     *     Duration#ZERO} to disable session lock renewal.
     * @param sessionIdleTimeout Timeout after which session receiver will be disposed if there are no more messages
     *                           and the receiver is idle. Set it to {@code null} to not dispose receiver.
     */
    ServiceBusSessionReceiver(String sessionId, ServiceBusReceiveLink receiveLink, MessageSerializer messageSerializer,
        AmqpRetryOptions retryOptions, int prefetch, Scheduler scheduler,
        Function<String, Mono<OffsetDateTime>> renewSessionLock, Duration maxSessionLockRenewDuration,
        Duration sessionIdleTimeout) {

        this.sessionId = sessionId;
        this.receiveLink = receiveLink;
        this.lockContainer = new LockContainer<>(ServiceBusConstants.OPERATION_TIMEOUT);
        this.retryOptions = retryOptions;

        receiveLink.setEmptyCreditListener(() -> 0);

        this.receivedMessages = receiveLink.receive().publishOn(scheduler).doOnSubscribe(subscription -> {
            withReceiveLinkInformation(LOGGER.atVerbose()).addKeyValue("prefetch", prefetch)
                .log("Adding prefetch to receive link.");

            if (prefetch > 0) {
                receiveLink.addCredits(prefetch).subscribe();
            }
        }).doOnRequest(request -> {  // request is of type long.
            if (prefetch == 0) {  //  add "request" number of credits
                receiveLink.addCredits((int) request).subscribe();
            } else {  // keep total credits "prefetch" if prefetch is not 0.
                receiveLink.addCredits(Math.max(0, prefetch - receiveLink.getCredits())).subscribe();
            }
        })
            .limitRate(1)
            // Continue receiving messages from the underlying AMQP link unless it is cancelled by another source.
            .takeUntilOther(cancelReceiveProcessor)
            .map(message -> {
                final ServiceBusReceivedMessage deserialized
                    = messageSerializer.deserialize(message, ServiceBusReceivedMessage.class);

                if (!CoreUtils.isNullOrEmpty(deserialized.getLockToken()) && deserialized.getLockedUntil() != null) {
                    lockContainer.addOrUpdate(deserialized.getLockToken(), deserialized.getLockedUntil(),
                        deserialized.getLockedUntil());
                } else {
                    LOGGER.atInfo()
                        .addKeyValue(SESSION_ID_KEY, deserialized.getSessionId())
                        .addKeyValue(MESSAGE_ID_LOGGING_KEY, deserialized.getMessageId())
                        .log("There is no lock token.");
                }

                return new ServiceBusMessageContext(deserialized);
            })
            .onErrorResume(error -> {
                withReceiveLinkInformation(LOGGER.atWarning()).log("Error occurred. Ending session.", error);

                return Mono.just(new ServiceBusMessageContext(getSessionId(), error));
            })
            .doOnNext(context -> {
                if (context.hasError()) {
                    return;
                }

                final ServiceBusReceivedMessage message = context.getMessage();
                final String token = !CoreUtils.isNullOrEmpty(message.getLockToken()) ? message.getLockToken() : "";

                LOGGER.atVerbose()
                    .addKeyValue(SESSION_ID_KEY, context.getSessionId())
                    .addKeyValue(MESSAGE_ID_LOGGING_KEY, message.getMessageId())
                    .log("Received message.");

                messageReceivedSink.next(token);
            });

        this.subscriptions = Disposables.composite();

        // Creates a subscription that disposes/closes the receiver when there are no more messages in the session and
        // receiver is idle.
        if (sessionIdleTimeout != null) {
            this.subscriptions
                .add(Flux.switchOnNext(messageReceivedEmitter.map((String lockToken) -> Mono.delay(sessionIdleTimeout)))
                    .subscribe(item -> {
                        withReceiveLinkInformation(LOGGER.atInfo()).addKeyValue("timeout", sessionIdleTimeout)
                            .log("Did not a receive message within timeout.");
                        cancelReceiveProcessor.onComplete();
                    }));
        }

        this.subscriptions.add(receiveLink.getSessionLockedUntil().subscribe(lockedUntil -> {
            if (!sessionLockedUntil.compareAndSet(null, lockedUntil)) {
                withReceiveLinkInformation(LOGGER.atInfo()).addKeyValue("existingLockToken", sessionLockedUntil.get())
                    .addKeyValue("newLockToken", lockedUntil)
                    .log("SessionLockedUntil was already set.");

                return;
            }
            this.renewalOperation.compareAndSet(null,
                new LockRenewalOperation(sessionId, maxSessionLockRenewDuration, true, renewSessionLock, lockedUntil));
        }));
    }

    /**
     * Gets whether or not the receiver contains the lock token.
     *
     * @param lockToken Lock token for the message.
     *
     * @return {@code true} if the session receiver contains the lock token to the unsettled delivery; {@code false}
     *     otherwise.
     * @throws NullPointerException if {@code lockToken} is null.
     * @throws IllegalArgumentException if {@code lockToken} is empty.
     */
    boolean containsLockToken(String lockToken) {
        if (lockToken == null) {
            throw LOGGER.logExceptionAsError(new NullPointerException("'lockToken' cannot be null."));
        } else if (lockToken.isEmpty()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'lockToken' cannot be an empty string."));
        }

        return lockContainer.containsUnexpired(lockToken);
    }

    String getLinkName() {
        return receiveLink.getLinkName();
    }

    String getSessionId() {
        return sessionId;
    }

    /**
     * Receives messages from session.
     *
     * @return A flux of messages for the session.
     */
    Flux<ServiceBusMessageContext> receive() {
        return receivedMessages;
    }

    /**
     * Updates the session lock time.
     *
     * @param lockedUntil Gets the time when the session is locked until.
     */
    void setSessionLockedUntil(OffsetDateTime lockedUntil) {
        sessionLockedUntil.set(lockedUntil);
    }

    Mono<Void> updateDisposition(String lockToken, DeliveryState deliveryState) {
        return receiveLink.updateDisposition(lockToken, deliveryState).doFinally(ignored -> {
            // Though the lock-container is cleanup at a fixed interval, it's a good
            // idea to remove the lock early when possible to reduce GC pressure.
            lockContainer.remove(lockToken);
        });
    }

    @Override
    public Mono<Void> closeAsync() {
        if (isDisposed.getAndSet(true)) {
            return receiveLink.closeAsync();
        }

        final LockRenewalOperation operation = renewalOperation.getAndSet(null);
        if (operation != null) {
            operation.close();
        }

        return receiveLink.closeAsync().doFinally(signal -> subscriptions.dispose());
    }

    @Override
    public void close() {
        closeAsync().block(retryOptions.getTryTimeout());
    }

    private LoggingEventBuilder withReceiveLinkInformation(LoggingEventBuilder builder) {
        return builder.addKeyValue(SESSION_ID_KEY, sessionId)
            .addKeyValue(ENTITY_PATH_KEY, receiveLink.getEntityPath())
            .addKeyValue(LINK_NAME_KEY, receiveLink.getLinkName());
    }
}
