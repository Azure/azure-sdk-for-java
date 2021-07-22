// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.messaging.servicebus;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.amqp.implementation.MessageSerializer;
import com.azure.core.util.AsyncCloseable;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
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

/**
 * Represents an session that is received when "any" session is accepted from the service.
 */
class ServiceBusSessionReceiver implements AsyncCloseable, AutoCloseable {
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final LockContainer<OffsetDateTime> lockContainer;
    private final AtomicReference<OffsetDateTime> sessionLockedUntil = new AtomicReference<>();
    private final AtomicReference<String> sessionId = new AtomicReference<>();
    private final AtomicReference<LockRenewalOperation> renewalOperation = new AtomicReference<>();
    private final ClientLogger logger = new ClientLogger(ServiceBusSessionReceiver.class);
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
     * @param messageSerializer Serializes and deserializes messages from Service Bus.
     * @param retryOptions Retry options for the receiver.
     * @param prefetch Number of messages to prefetch from session.
     * @param disposeOnIdle true to dispose the session receiver if there are no more messages and the receiver is
     *     idle.
     * @param scheduler The scheduler to publish messages on.
     * @param renewSessionLock Function to renew the session lock.
     * @param maxSessionLockRenewDuration Maximum time to renew the session lock for. {@code null} or {@link
     *     Duration#ZERO} to disable session lock renewal.
     */
    ServiceBusSessionReceiver(ServiceBusReceiveLink receiveLink, MessageSerializer messageSerializer,
        AmqpRetryOptions retryOptions, int prefetch, boolean disposeOnIdle, Scheduler scheduler,
        Function<String, Mono<OffsetDateTime>> renewSessionLock, Duration maxSessionLockRenewDuration) {

        this.receiveLink = receiveLink;
        this.lockContainer = new LockContainer<>(ServiceBusConstants.OPERATION_TIMEOUT);
        this.retryOptions = retryOptions;

        receiveLink.setEmptyCreditListener(() -> 0);

        final Flux<ServiceBusMessageContext> receivedMessagesFlux = receiveLink
            .receive()
            .publishOn(scheduler)
            .doOnSubscribe(subscription -> {
                logger.verbose("Adding prefetch to receive link.");
                if (prefetch > 0) {
                    receiveLink.addCredits(prefetch).subscribe();
                }
            })
            .doOnRequest(request -> {  // request is of type long.
                if (prefetch == 0) {  //  add "request" number of credits
                    receiveLink.addCredits((int) request).subscribe();
                } else {  // keep total credits "prefetch" if prefetch is not 0.
                    receiveLink.addCredits(Math.max(0, prefetch - receiveLink.getCredits())).subscribe();
                }
            })
            .takeUntilOther(cancelReceiveProcessor)
            .map(message -> {
                final ServiceBusReceivedMessage deserialized = messageSerializer.deserialize(message,
                    ServiceBusReceivedMessage.class);

                //TODO (conniey): For session receivers, do they have a message lock token?
                if (!CoreUtils.isNullOrEmpty(deserialized.getLockToken()) && deserialized.getLockedUntil() != null) {
                    lockContainer.addOrUpdate(deserialized.getLockToken(), deserialized.getLockedUntil(),
                        deserialized.getLockedUntil());
                } else {
                    logger.info("sessionId[{}] message[{}]. There is no lock token.",
                        deserialized.getSessionId(), deserialized.getMessageId());
                }

                return new ServiceBusMessageContext(deserialized);
            })
            .onErrorResume(error -> {
                logger.warning("sessionId[{}]. Error occurred. Ending session.", sessionId, error);
                return Mono.just(new ServiceBusMessageContext(getSessionId(), error));
            })
            .doOnNext(context -> {
                if (context.hasError()) {
                    return;
                }

                final ServiceBusReceivedMessage message = context.getMessage();
                final String token = !CoreUtils.isNullOrEmpty(message.getLockToken())
                    ? message.getLockToken()
                    : "";

                logger.verbose("Received sessionId[{}] messageId[{}]", context.getSessionId(), message.getMessageId());
                messageReceivedSink.next(token);
            });

        this.receivedMessages = Flux.concat(receivedMessagesFlux, cancelReceiveProcessor);
        this.subscriptions = Disposables.composite();

        // Creates a subscription that disposes/closes the receiver when there are no more messages in the session and
        // receiver is idle.
        if (disposeOnIdle) {
            this.subscriptions.add(Flux.switchOnNext(messageReceivedEmitter
                .map((String lockToken) -> Mono.delay(this.retryOptions.getTryTimeout())))
                .subscribe(item -> {
                    logger.info("entityPath[{}]. sessionId[{}]. Did not a receive message within timeout {}.",
                        receiveLink.getEntityPath(), sessionId.get(), retryOptions.getTryTimeout());
                    cancelReceiveProcessor.onComplete();
                }));
        }

        this.subscriptions.add(receiveLink.getSessionId().subscribe(id -> {
            if (!sessionId.compareAndSet(null, id)) {
                logger.warning("Another method set sessionId. Existing: {}. Returned: {}.", sessionId.get(), id);
            }
        }));
        this.subscriptions.add(receiveLink.getSessionLockedUntil().subscribe(lockedUntil -> {
            if (!sessionLockedUntil.compareAndSet(null, lockedUntil)) {
                logger.info("SessionLockedUntil was already set: {}", sessionLockedUntil);
                return;
            }
            this.renewalOperation.compareAndSet(null, new LockRenewalOperation(sessionId.get(),
                maxSessionLockRenewDuration, true, renewSessionLock, lockedUntil));
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
            throw logger.logExceptionAsError(new NullPointerException("'lockToken' cannot be null."));
        } else if (lockToken.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'lockToken' cannot be an empty string."));
        }

        return lockContainer.containsUnexpired(lockToken);
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
        return receiveLink.updateDisposition(lockToken, deliveryState);
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
}
