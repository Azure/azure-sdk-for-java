// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.LockContainer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;
import reactor.core.publisher.Mono;
import org.reactivestreams.Publisher;
import reactor.util.context.Context;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Receives messages from to upstream, subscribe lock renewal subscriber.
 */
final class FluxAutoRenew extends FluxOperator<ServiceBusReceivedMessage, ServiceBusReceivedMessage> {

    private final ClientLogger logger = new ClientLogger(FluxAutoRenew.class);

    private final Function<String, Mono<OffsetDateTime>> onRenewLock;
    private final Duration maxAutoLockRenewal;
    private final LockContainer<LockRenewalOperation> messageLockContainer;
    private final AtomicReference<LockRenewSubscriber> lockRenewSubscriber = new AtomicReference<>();

    /**
     * Build a {@link FluxOperator} wrapper around the passed parent {@link Publisher}
     * @param source the {@link Publisher} to decorate
     *
     * @throws NullPointerException If {@code onRenewLock}, {@code messageLockContainer},
     * {@code maxAutoLockRenewDuration} is null.
     *
     * @throws IllegalArgumentException If maxLockRenewalDuration is zero or negative.
     */
    FluxAutoRenew(
        Flux<? extends ServiceBusReceivedMessage> source, Duration maxAutoLockRenewDuration,
        LockContainer<LockRenewalOperation> messageLockContainer, Function<String, Mono<OffsetDateTime>> onRenewLock) {
        super(source);

        this.onRenewLock = Objects.requireNonNull(onRenewLock, "'onRenewLock' cannot be null.");
        this.messageLockContainer = Objects.requireNonNull(messageLockContainer,
            "'messageLockContainer' cannot be null.");

        Objects.requireNonNull(maxAutoLockRenewDuration, "'maxAutoLockRenewDuration' cannot be null.");
        if (maxAutoLockRenewDuration.isNegative() || maxAutoLockRenewDuration.isZero()) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "'maxLockRenewalDuration' should not be zero or negative."));
        }
        this.maxAutoLockRenewal = maxAutoLockRenewDuration;

    }

    @Override
    public void subscribe(CoreSubscriber<? super ServiceBusReceivedMessage> actual) {
        LockRenewSubscriber newLockRenewSubscriber = new LockRenewSubscriber(actual, maxAutoLockRenewal,
            messageLockContainer, onRenewLock);
        if (!lockRenewSubscriber.compareAndSet(null, newLockRenewSubscriber)) {
            newLockRenewSubscriber.dispose();
            logger.error("Already subscribed once.");
            return;
        }
        source.subscribe(lockRenewSubscriber.get());
    }

    /**
     * Receives messages from to upstream, pushes them downstream and start lock renewal.
     */
    final class LockRenewSubscriber extends BaseSubscriber<ServiceBusReceivedMessage> {
        private final ClientLogger logger = new ClientLogger(LockRenewSubscriber.class);

        private final Function<String, Mono<OffsetDateTime>> onRenewLock;
        private final Duration maxAutoLockRenewal;
        private final LockContainer<LockRenewalOperation> messageLockContainer;
        private final CoreSubscriber<? super ServiceBusReceivedMessage> actual;

        LockRenewSubscriber(CoreSubscriber<? super ServiceBusReceivedMessage> actual, Duration maxAutoLockRenewDuration,
            LockContainer<LockRenewalOperation> messageLockContainer,
            Function<String, Mono<OffsetDateTime>> onRenewLock) {
            this.onRenewLock = Objects.requireNonNull(onRenewLock, "'onRenewLock' cannot be null.");
            this.actual = actual;
            this.messageLockContainer = Objects.requireNonNull(messageLockContainer,
                "'messageLockContainer' cannot be null.");
            Objects.requireNonNull(maxAutoLockRenewDuration, "'maxAutoLockRenewDuration' cannot be null.");
            this.maxAutoLockRenewal = maxAutoLockRenewDuration;
        }
        /**
         * On an initial subscription, will take the first work item, and request that amount of work for it.
         * @param subscription Subscription for upstream.
         */
        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            Objects.requireNonNull(subscription, "'subscription' cannot be null.");
            actual.onSubscribe(subscription);
        }

        /**
         * When upstream has completed emitting messages.
         */
        @Override
        public void hookOnComplete() {
            logger.info("Upstream has completed.");
            actual.onComplete();
        }

        @Override
        protected void hookOnError(Throwable throwable) {
            logger.info("Errors occurred upstream.", throwable);
            actual.onError(throwable);
            dispose();
        }

        @Override
        protected void hookOnNext(ServiceBusReceivedMessage message) {
            final String lockToken = message.getLockToken();
            final OffsetDateTime lockedUntil = message.getLockedUntil();
            final LockRenewalOperation renewOperation;

            if (Objects.isNull(lockToken)) {
                logger.info("Unexpected, LockToken is not present in message [{}].", message.getSequenceNumber());
                return;
            } else if (Objects.isNull(lockedUntil)) {
                logger.info("Unexpected, lockedUntil is not present in message [{}].", message.getSequenceNumber());
                return;
            }

            final Function<String, Mono<OffsetDateTime>> onRenewLockUpdateMessage = onRenewLock.andThen(updated ->
                updated.map(newLockedUntil -> {
                    message.setLockedUntil(newLockedUntil);
                    return newLockedUntil;
                }));

            renewOperation = new LockRenewalOperation(lockToken, maxAutoLockRenewal, false,
                onRenewLockUpdateMessage, lockedUntil);

            try {
                messageLockContainer.addOrUpdate(lockToken, OffsetDateTime.now().plus(maxAutoLockRenewal),
                    renewOperation);
            } catch (Exception e) {
                logger.error("Exception occurred while updating lockContainer for token [{}].", lockToken, e);
            }

            try {
                actual.onNext(message);
            } catch (Exception e) {
                logger.error("Exception occurred while handling downstream onNext operation.", e);
                onError(e);
            } finally {
                renewOperation.close();
                messageLockContainer.remove(lockToken);
            }
        }

        @Override
        public Context currentContext() {
            return actual.currentContext();
        }
    }
}
