// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.LockContainer;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;
import reactor.core.publisher.Mono;
import org.reactivestreams.Publisher;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Receives messages from to upstream, subscribe lock renewal subscriber.
 */
final class ServiceBusMessageRenewOperator extends FluxOperator<ServiceBusReceivedMessage, ServiceBusReceivedMessage> {

    private final ClientLogger logger = new ClientLogger(ServiceBusMessageRenewOperator.class);

    private final Function<String, Mono<OffsetDateTime>> onRenewLock;
    private final Duration maxAutoLockRenewal;
    private final LockContainer<LockRenewalOperation> messageLockContainer;
    private final AtomicReference<LockRenewSubscriber> lockRenewSubscriber = new AtomicReference<>();

    /**
     * Build a {@link FluxOperator} wrapper around the passed parent {@link Publisher}
     * @param source the {@link Publisher} to decorate
     */
    ServiceBusMessageRenewOperator(
        Flux<? extends ServiceBusReceivedMessage> source, Duration maxAutoLockRenewDuration,
        LockContainer<LockRenewalOperation> messageLockContainer, Function<String, Mono<OffsetDateTime>> onRenewLock) {
        super(source);

        this.onRenewLock = Objects.requireNonNull(onRenewLock, "'onRenewLock' cannot be null.");
        this.messageLockContainer = Objects.requireNonNull(messageLockContainer,
            "'messageLockContainer' cannot be null.");

        this.maxAutoLockRenewal = Objects.requireNonNull(maxAutoLockRenewDuration,
            "'maxAutoLockRenewDuration' cannot be null.");

    }

    @Override
    public void subscribe(CoreSubscriber<? super ServiceBusReceivedMessage> actual) {
        LockRenewSubscriber newLockRenewSubscriber = new LockRenewSubscriber(actual, maxAutoLockRenewal,
            messageLockContainer, onRenewLock);
        if (!lockRenewSubscriber.compareAndSet(null, newLockRenewSubscriber)) {
            newLockRenewSubscriber.dispose();
            logger.error("Already subscribed once.");
        }
        source.subscribe(lockRenewSubscriber.get());
    }
}
