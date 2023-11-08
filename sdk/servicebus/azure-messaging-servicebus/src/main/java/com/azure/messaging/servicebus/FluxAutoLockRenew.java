// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.LockContainer;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
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
import java.util.function.Consumer;
import java.util.function.Function;

import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.LOCK_TOKEN_KEY;
import static com.azure.messaging.servicebus.implementation.ServiceBusConstants.SEQUENCE_NUMBER_KEY;

/**
 * Receives messages from to upstream, subscribe lock renewal subscriber.
 */
final class FluxAutoLockRenew extends FluxOperator<ServiceBusMessageContext, ServiceBusMessageContext> {

    private static final ClientLogger LOGGER = new ClientLogger(FluxAutoLockRenew.class);

    private final Function<String, Mono<OffsetDateTime>> onRenewLock;
    private final LockContainer<LockRenewalOperation> messageLockContainer;
    private final ReceiverOptions receivingOptions;
    private final ServiceBusTracer tracer;

    /**
     * Build a {@link FluxOperator} wrapper around the passed parent {@link Publisher}
     * @param source the {@link Publisher} to decorate
     *
     * @throws NullPointerException If {@code onRenewLock}, {@code messageLockContainer},
     * {@code ReceiverOptions} or {@code maxAutoLockRenewDuration} is null.
     *
     * @throws IllegalArgumentException If receiverOptions.maxLockRenewalDuration is zero or negative.
     */
    FluxAutoLockRenew(
        Flux<? extends ServiceBusMessageContext> source, ReceiverOptions receiverOptions,
        LockContainer<LockRenewalOperation> messageLockContainer, Function<String, Mono<OffsetDateTime>> onRenewLock,
        ServiceBusTracer tracer) {
        super(source);
        this.receivingOptions = Objects.requireNonNull(receiverOptions, "'receiverOptions' cannot be null.");
        this.onRenewLock = Objects.requireNonNull(onRenewLock, "'onRenewLock' cannot be null.");
        this.messageLockContainer = Objects.requireNonNull(messageLockContainer,
            "'messageLockContainer' cannot be null.");
        this.tracer = Objects.requireNonNull(tracer, "'tracer' cannot be null.");
        Duration maxAutoLockRenewDuration = receiverOptions.getMaxLockRenewDuration();
        Objects.requireNonNull(maxAutoLockRenewDuration,
            "'receivingOptions.maxAutoLockRenewDuration' cannot be null.");

        if (maxAutoLockRenewDuration.isNegative() || maxAutoLockRenewDuration.isZero()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "'receivingOptions.maxLockRenewalDuration' should not be zero or negative."));
        }
    }

    @Override
    public void subscribe(CoreSubscriber<? super ServiceBusMessageContext> coreSubscriber) {
        Objects.requireNonNull(coreSubscriber, "'coreSubscriber' cannot be null.");

        final LockRenewSubscriber newLockRenewSubscriber = new LockRenewSubscriber(coreSubscriber,
            receivingOptions.getMaxLockRenewDuration(), messageLockContainer, onRenewLock,
            receivingOptions.isEnableAutoComplete(), tracer);

        source.subscribe(newLockRenewSubscriber);
    }

    /**
     * Receives messages from to upstream, pushes them downstream and start lock renewal.
     */
    static final class LockRenewSubscriber extends BaseSubscriber<ServiceBusMessageContext> {
        private static final Consumer<ServiceBusMessageContext> LOCK_RENEW_NO_OP = messageContext -> { };

        private static final ClientLogger LOGGER = new ClientLogger(LockRenewSubscriber.class);

        private final Function<String, Mono<OffsetDateTime>> onRenewLock;
        private final Duration maxAutoLockRenewal;
        private final LockContainer<LockRenewalOperation> messageLockContainer;
        private final CoreSubscriber<? super ServiceBusMessageContext> actual;
        private final boolean isAutoCompleteEnabled;
        private final ServiceBusTracer tracer;

        LockRenewSubscriber(CoreSubscriber<? super ServiceBusMessageContext> actual,
            Duration maxAutoLockRenewDuration, LockContainer<LockRenewalOperation> messageLockContainer,
            Function<String, Mono<OffsetDateTime>> onRenewLock, boolean isAutoCompleteEnabled,
            ServiceBusTracer tracer) {
            this.onRenewLock = Objects.requireNonNull(onRenewLock, "'onRenewLock' cannot be null.");
            this.actual = Objects.requireNonNull(actual, "'downstream' cannot be null.");
            this.messageLockContainer = Objects.requireNonNull(messageLockContainer,
                "'messageLockContainer' cannot be null.");
            this.maxAutoLockRenewal = Objects.requireNonNull(maxAutoLockRenewDuration,
                "'maxAutoLockRenewDuration' cannot be null.");
            this.isAutoCompleteEnabled = isAutoCompleteEnabled;
            this.tracer = tracer;
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
            LOGGER.verbose("Upstream has completed.");
            actual.onComplete();
        }

        @Override
        protected void hookOnError(Throwable throwable) {
            LOGGER.error("Errors occurred upstream.", throwable);
            actual.onError(throwable);
        }

        @Override
        protected void hookOnNext(ServiceBusMessageContext messageContext) {
            final ServiceBusReceivedMessage message = messageContext.getMessage();

            final Consumer<ServiceBusMessageContext> lockCleanup;

            if (message != null) {
                final String lockToken = message.getLockToken();
                final OffsetDateTime lockedUntil = message.getLockedUntil();
                final LockRenewalOperation renewOperation;

                if (Objects.isNull(lockToken)) {
                    LOGGER.atWarning()
                        .addKeyValue(SEQUENCE_NUMBER_KEY, message.getSequenceNumber())
                        .log("Unexpected, LockToken is not present in message.");
                    return;
                } else if (Objects.isNull(lockedUntil)) {
                    LOGGER.atWarning()
                        .addKeyValue(SEQUENCE_NUMBER_KEY, message.getSequenceNumber())
                        .log("Unexpected, lockedUntil is not present in message.");
                    return;
                }

                final Function<String, Mono<OffsetDateTime>> onRenewLockUpdateMessage = onRenewLock.andThen(updated ->
                    tracer.traceRenewMessageLock(updated.map(newLockedUntil -> {
                        message.setLockedUntil(newLockedUntil);
                        return newLockedUntil;
                    }), message));

                renewOperation = new LockRenewalOperation(lockToken, maxAutoLockRenewal, false,
                    onRenewLockUpdateMessage, lockedUntil);

                try {
                    messageLockContainer.addOrUpdate(lockToken, OffsetDateTime.now().plus(maxAutoLockRenewal),
                        renewOperation);
                } catch (Exception e) {
                    LOGGER.atInfo()
                        .addKeyValue(LOCK_TOKEN_KEY, lockToken)
                        .log("Exception occurred while updating lockContainer.", e);
                }

                lockCleanup = context -> {
                    renewOperation.close();
                    messageLockContainer.remove(context.getMessage().getLockToken());
                };

            } else {
                lockCleanup = LOCK_RENEW_NO_OP;
            }

            try {
                actual.onNext(messageContext);
            } catch (Exception e) {
                LOGGER.info("Exception occurred while handling downstream onNext operation.", e);
            } finally {
                if (isAutoCompleteEnabled) {
                    lockCleanup.accept(messageContext);
                }
            }
        }

        @Override
        public Context currentContext() {
            return actual.currentContext();
        }
    }
}
