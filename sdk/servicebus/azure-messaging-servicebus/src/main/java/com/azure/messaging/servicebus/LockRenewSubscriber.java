// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.LockContainer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Function;

/**
 * Receives messages from to upstream, pushes them downstream and start lock renewal.
 */
final class LockRenewSubscriber extends BaseSubscriber<ServiceBusReceivedMessage> {
    private static final OffsetDateTime COMPLETED_RENEW_TIME = OffsetDateTime.now();
    private static final LockRenewalOperation COMPLETED_ONE = new LockRenewalOperation("lock-no-renew",
        Duration.ZERO, false, (id) -> Mono.empty(), COMPLETED_RENEW_TIME);

    private final ClientLogger logger = new ClientLogger(LockRenewSubscriber.class);

    private final Function<String, Mono<OffsetDateTime>> onRenewLock;
    private final boolean isAutoRenewLock;
    private final Duration maxAutoLockRenewal;
    private final LockContainer<LockRenewalOperation> messageLockContainer;
    private final CoreSubscriber<? super ServiceBusReceivedMessage> actual;

    private volatile Subscription subscription;
    private static final AtomicReferenceFieldUpdater<LockRenewSubscriber, Subscription> UPSTREAM =
        AtomicReferenceFieldUpdater.newUpdater(LockRenewSubscriber.class, Subscription.class,
            "subscription");

    LockRenewSubscriber(CoreSubscriber<? super ServiceBusReceivedMessage> actual, boolean autoLockRenewal,
        Duration maxAutoLockRenewDuration, LockContainer<LockRenewalOperation> messageLockContainer,
        Function<String, Mono<OffsetDateTime>> onRenewLock) {
        this.onRenewLock = Objects.requireNonNull(onRenewLock, "'onRenewLock' cannot be null.");
        this.actual = actual;
        this.messageLockContainer = Objects.requireNonNull(messageLockContainer,
            "'messageLockContainer' cannot be null.");

        this.maxAutoLockRenewal = Objects.requireNonNull(maxAutoLockRenewDuration,
            "'maxAutoLockRenewDuration' cannot be null.");
        this.isAutoRenewLock = autoLockRenewal;

    }
    /**
     * On an initial subscription, will take the first work item, and request that amount of work for it.
     * @param subscription Subscription for upstream.
     */
    @Override
    protected void hookOnSubscribe(Subscription subscription) {
        Objects.requireNonNull(subscription, "'subscription' cannot be null.");
        if (Operators.setOnce(UPSTREAM, this, subscription)) {
            this.subscription = subscription;
            actual.onSubscribe(subscription);
        } else {
            logger.error("Already subscribed once.");
        }
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
        logger.error("Errors occurred upstream.");
        actual.onError(throwable);
        dispose();
    }

    @Override
    protected void hookOnCancel() {
        logger.error("Upstream cancelled.");
        subscription.cancel();
    }

    @Override
    protected void hookOnNext(ServiceBusReceivedMessage message) {
        final String lockToken = message.getLockToken();
        final OffsetDateTime lockedUntil = message.getLockedUntil();
        final LockRenewalOperation renewOperation;

        if (isAutoRenewLock && !Objects.isNull(lockedUntil) && !Objects.isNull(lockToken)) {

            renewOperation = new LockRenewalOperation(lockToken, maxAutoLockRenewal, false, onRenewLock,
                lockedUntil);
            messageLockContainer.addOrUpdate(lockToken, OffsetDateTime.now().plus(maxAutoLockRenewal), renewOperation);
        } else {

            renewOperation = COMPLETED_ONE;
        }

        try {
            actual.onNext(message);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception occurred while handling downstream onNext operation.", e);
            onError(e);
        } finally {
            renewOperation.close();
        }
    }

}
