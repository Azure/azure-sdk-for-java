// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.LockContainer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import org.reactivestreams.Publisher;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.function.Function;

/**
 * Receives messages from to upstream, pushes them downstream and start lock renewal.
 */
final class ServiceBusMessageRenewOperator extends FluxOperator<ServiceBusReceivedMessage, ServiceBusReceivedMessage> {

    private final ClientLogger logger = new ClientLogger(ServiceBusMessageRenewOperator.class);

    private final Function<String, Mono<OffsetDateTime>> onRenewLock;
    private final boolean isAutoRenewLock;
    private final Duration maxAutoLockRenewal;
    private final LockContainer<LockRenewalOperation> messageLockContainer;
    private final Object lock = new Object();

    /**
     * Build a {@link FluxOperator} wrapper around the passed parent {@link Publisher}
     * @param source the {@link Publisher} to decorate
     */
    ServiceBusMessageRenewOperator(
        Flux<? extends ServiceBusReceivedMessage> source, boolean autoLockRenewal, Duration maxAutoLockRenewDuration,
        LockContainer<LockRenewalOperation> messageLockContainer, Function<String, Mono<OffsetDateTime>> onRenewLock) {
        super(source);

        this.onRenewLock = Objects.requireNonNull(onRenewLock, "'onRenewLock' cannot be null.");
        this.messageLockContainer = Objects.requireNonNull(messageLockContainer,
            "'messageLockContainer' cannot be null.");

        this.isAutoRenewLock = autoLockRenewal;
        this.maxAutoLockRenewal = maxAutoLockRenewDuration;

    }

    @Override
    public void subscribe(CoreSubscriber<? super ServiceBusReceivedMessage> actual) {
        source.subscribe(new CoreSubscriber<ServiceBusReceivedMessage>() {

           @Override
            public void onSubscribe(Subscription subscription) {
               Objects.requireNonNull(subscription, "'subscription' cannot be null.");
               actual.onSubscribe(subscription);
            }

            @Override
            public void onNext(ServiceBusReceivedMessage message) {
               synchronized (lock) {
                   final String lockToken = message.getLockToken();
                   LockRenewalOperation renewLockOperation = null;

                   if (isAutoRenewLock
                       && message.getLockedUntil() != null
                       && message.getLockToken() != null) {
                       renewLockOperation = new LockRenewalOperation(message.getLockToken(), maxAutoLockRenewal,
                           false, onRenewLock, message.getLockedUntil());

                       messageLockContainer.addOrUpdate(lockToken, OffsetDateTime.now().plus(maxAutoLockRenewal),
                           renewLockOperation);
                   }

                   try {
                       actual.onNext(message);
                   } catch (Exception e) {
                       logger.error("Exception occurred while handling downstream onNext operation.");
                       onError(e);
                   } finally {
                       if (renewLockOperation != null) {
                           renewLockOperation.close();
                       }
                   }
               }

            }

            @Override
            public void onError(Throwable t) {
                actual.onError(t);
            }

            @Override
            public void onComplete() {
                actual.onComplete();
            }

            @Override
            public Context currentContext() {
                return actual.currentContext();
            }
        });
    }
}
