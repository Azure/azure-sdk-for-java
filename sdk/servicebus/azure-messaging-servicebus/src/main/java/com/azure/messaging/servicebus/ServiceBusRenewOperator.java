// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.LockContainer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

/**
 * Operate on messages from to upstream, pushes them downstream  and start lock renewal.
 */
final class ServiceBusRenewOperator extends FluxOperator<ServiceBusReceivedMessage, ServiceBusReceivedMessage>  {

    private final ClientLogger logger = new ClientLogger(ServiceBusRenewOperator.class);

    private final Function<String, Mono<OffsetDateTime>> onRenewLock;
    private final boolean isAutoRenewLock;
    private final Duration maxAutoLockRenewal;
    private final LockContainer<LockRenewalOperation> messageLockContainer;

    /**
     * Build a {@link FluxOperator} wrapper around the passed parent {@link org.reactivestreams.Publisher}
     * @param source the {@link org.reactivestreams.Publisher} to decorate
     */
    ServiceBusRenewOperator(
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
    public void subscribe(CoreSubscriber<? super com.azure.messaging.servicebus.ServiceBusReceivedMessage> actual) {
        source.subscribe(new CoreSubscriber<ServiceBusReceivedMessage>() {

           @Override
            public void onSubscribe(Subscription s) {
                actual.onSubscribe(s);
            }

            @Override
            public void onNext(ServiceBusReceivedMessage message) {

                final long sequenceNumber = message.getSequenceNumber();
                final String lockToken = message.getLockToken();
                LockRenewalOperation renewLockOperation = null;

                if (isAutoRenewLock) {
                    if (CoreUtils.isNullOrEmpty(lockToken)) {
                        throw logger.logExceptionAsError(new IllegalStateException(
                            "Cannot auto-renew message without a lock token on message. Sequence number: " + sequenceNumber));
                    } else if (message.getLockedUntil() ==  null) {
                        throw logger.logExceptionAsError(new IllegalStateException(
                            "Cannot auto-renew message without a lock token until on message. Sequence number: " + sequenceNumber));
                    }

                    renewLockOperation = new LockRenewalOperation(message.getLockToken(), maxAutoLockRenewal, false,
                        onRenewLock, message.getLockedUntil());
                    messageLockContainer.addOrUpdate(lockToken, OffsetDateTime.now().plus(maxAutoLockRenewal),
                        renewLockOperation);
                }

                final AtomicBoolean hasError = new AtomicBoolean();
                try {
                    actual.onNext(message);
                } catch (Exception e) {
                    hasError.set(true);
                    logger.error("Exception occurred while handling downstream onNext operation.", e);

                } finally {
                    if (renewLockOperation != null) {
                        renewLockOperation.close();
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
