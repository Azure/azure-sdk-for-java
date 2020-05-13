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
import com.azure.messaging.servicebus.implementation.MessageUtils;
import com.azure.messaging.servicebus.implementation.ServiceBusConstants;
import com.azure.messaging.servicebus.implementation.ServiceBusMessageProcessor;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiveLink;
import org.apache.qpid.proton.amqp.transport.DeliveryState;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoProcessor;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Represents an session that is received when "any" session is accepted from the service.
 */
class UnnamedSessionReceiver implements AutoCloseable {
    private final AtomicBoolean isDisposed = new AtomicBoolean();
    private final MessageLockContainer lockContainer;
    private final AtomicReference<Instant> sessionLockedUntil = new AtomicReference<>();
    private final AtomicReference<String> sessionId = new AtomicReference<>();
    private final ClientLogger logger = new ClientLogger(UnnamedSessionReceiver.class);
    private final ServiceBusReceiveLink receiveLink;
    private final Duration maxSessionLockRenewDuration;
    private final Function<String, Mono<Instant>> renewSessionLock;
    private final Disposable.Composite subscriptions;
    private final Flux<ServiceBusReceivedMessageContext> receivedMessages;
    private final MonoProcessor<ServiceBusReceivedMessageContext> cancelReceiveProcessor = MonoProcessor.create();
    private final DirectProcessor<String> messageReceivedEmitter = DirectProcessor.create();
    private final FluxSink<String> messageReceivedSink = messageReceivedEmitter.sink(FluxSink.OverflowStrategy.BUFFER);

    UnnamedSessionReceiver(ServiceBusReceiveLink receiveLink, MessageSerializer messageSerializer,
        boolean isAutoComplete, Duration maxSessionLockRenewDuration, AmqpRetryOptions retryOptions, int prefetch,
        Scheduler scheduler, Function<String, Mono<Instant>> renewSessionLock) {
        this.receiveLink = receiveLink;
        this.maxSessionLockRenewDuration = maxSessionLockRenewDuration;
        this.renewSessionLock = renewSessionLock;
        this.lockContainer = new MessageLockContainer(ServiceBusConstants.OPERATION_TIMEOUT);

        final AmqpErrorContext errorContext = new LinkErrorContext(receiveLink.getHostname(),
            receiveLink.getEntityPath(), null, null);
        final SessionMessageManagement messageManagement = new SessionMessageManagement(receiveLink);

        receiveLink.setEmptyCreditListener(() -> 1);

        final Flux<ServiceBusReceivedMessageContext> receivedMessagesFlux = receiveLink
            .receive()
            .publishOn(scheduler)
            .doOnSubscribe(subscription ->  {
                logger.verbose("Adding prefetch to receive link.");
                receiveLink.addCredits(prefetch);
            })
            .takeUntilOther(cancelReceiveProcessor)
            .map(message -> messageSerializer.deserialize(message, ServiceBusReceivedMessage.class))
            .subscribeWith(new ServiceBusMessageProcessor(receiveLink.getLinkName(), isAutoComplete, false,
                Duration.ZERO, retryOptions, errorContext, messageManagement))
            .map(message -> {
                if (!CoreUtils.isNullOrEmpty(message.getLockToken())) {
                    lockContainer.addOrUpdate(message.getLockToken(), message.getLockedUntil());
                }

                return new ServiceBusReceivedMessageContext(message);
            })
            .onErrorResume(error -> {
                logger.warning("sessionId[{}]. Error occurred. Ending session.", sessionId, error);
                return Mono.just(new ServiceBusReceivedMessageContext(getSessionId(), error));
            })
            .doOnNext(context -> {
                if (context.hasError()) {
                    return;
                }

                final String token = CoreUtils.isNullOrEmpty(context.getMessage().getLockToken())
                    ? context.getMessage().getLockToken()
                    : "";
                messageReceivedSink.next(token);
            });

        this.receivedMessages = Flux.concat(receivedMessagesFlux, cancelReceiveProcessor);
        this.subscriptions = Disposables.composite();
        this.subscriptions.add(Flux.switchOnNext(messageReceivedEmitter
            .flatMap(lockToken -> Mono.delay(retryOptions.getTryTimeout()))
            .handle((l, sink) -> {
                logger.info("entityPath[{}]. sessionId[{}]. Did not a receive message within timeout {}.",
                    receiveLink.getEntityPath(), sessionId.get(), retryOptions.getTryTimeout());
                cancelReceiveProcessor.onComplete();
                sink.complete();
            }))
            .subscribe());
        this.subscriptions.add(receiveLink.getSessionId().subscribe(id -> sessionId.set(id)));
        this.subscriptions.add(receiveLink.getSessionLockedUntil().subscribe(lockedUntil -> {
            if (!sessionLockedUntil.compareAndSet(null, lockedUntil)) {
                logger.info("SessionLockedUntil was already set: {}", sessionLockedUntil);
            } else {
                this.subscriptions.add(getRenewLockOperation(lockedUntil));
            }
        }));
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

    private Disposable getRenewLockOperation(Instant initialLockedUntil) {
        final Instant now = Instant.now();
        Duration initialInterval = Duration.between(now, initialLockedUntil);
        if (initialInterval.isNegative()) {
            logger.info("Duration was negative. now[{}] lockedUntil[{}]", now, initialLockedUntil);
            initialInterval = Duration.ZERO;
        } else {
            final Duration adjusted = MessageUtils.adjustServerTimeout(initialInterval);
            if (adjusted.isNegative()) {
                logger.info("Adjusted duration is negative. Adjusted: {}ms", initialInterval.toMillis());
            } else {
                initialInterval = adjusted;
            }
        }

        final EmitterProcessor<Duration> emitterProcessor = EmitterProcessor.create();
        final FluxSink<Duration> sink = emitterProcessor.sink(FluxSink.OverflowStrategy.BUFFER);

        // Adjust the interval, so we can buffer time for the time it'll take to refresh.
        sink.next(MessageUtils.adjustServerTimeout(initialInterval));

        // TODO (conniey): Do we need to stop processing lock renewal after a max duration??
        // if (maxSessionLockRenewDuration == null || maxSessionLockRenewDuration.isZero()) {
        //     return Disposables.disposed();
        // }
        //
        // final Disposable timeoutOperation = Mono.delay(maxSessionLockRenewDuration)
        //     .subscribe(l -> {
        //         if (!sink.isCancelled()) {
        //             sink.error(new AmqpException(true, AmqpErrorCondition.TIMEOUT_ERROR,
        //                 "Could not complete session processing within renewal time. Max session renewal time: "
        //                     + maxSessionLockRenewDuration,
        //                 new LinkErrorContext(receiveLink.getHostname(), receiveLink.getEntityPath(), null, null)));
        //         }
        //     });
        //
        // return Disposables.composite(renewLockSubscription, timeoutOperation);

        return Flux.switchOnNext(emitterProcessor.map(i -> Flux.interval(i)))
            .takeUntilOther(cancelReceiveProcessor)
            .flatMap(delay -> {
                final String id = sessionId.get();

                logger.info("sessionId[{}]. now[{}]. Starting lock renewal.", id, Instant.now());
                if (CoreUtils.isNullOrEmpty(id)) {
                    return Mono.error(new IllegalStateException("Cannot renew session lock without session id."));
                }

                return renewSessionLock.apply(sessionId.get());
            })
            .map(instant -> {
                final Duration next = Duration.between(Instant.now(), instant);
                logger.info("sessionId[{}]. nextExpiration[{}]. Next renewal: [{}]", sessionId, instant, next);

                sink.next(MessageUtils.adjustServerTimeout(next));
                return instant;
            })
            .subscribe(
                lockedUntil -> logger.verbose("lockToken[{}]. lockedUntil[{}]. Lock renewal successful.", sessionId,
                    lockedUntil),
                error -> {
                    logger.error("Error occurred while renewing lock token.", error);
                    cancelReceiveProcessor.onNext(new ServiceBusReceivedMessageContext(sessionId.get(), error));
                },
                () -> {
                    logger.verbose("Renewing session lock task completed.");
                    cancelReceiveProcessor.onComplete();
                });
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
