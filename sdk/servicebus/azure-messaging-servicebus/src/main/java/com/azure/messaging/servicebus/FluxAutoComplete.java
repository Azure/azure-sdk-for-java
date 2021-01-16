// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.logging.ClientLogger;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.Objects;
import java.util.concurrent.Semaphore;
import java.util.function.Function;

/**
 * Flux operator that auto-completes or auto-abandons messages when control is returned successfully.
 */
final class FluxAutoComplete extends FluxOperator<ServiceBusMessageContext, ServiceBusMessageContext> {
    private final Semaphore completionLock;
    private final Function<ServiceBusMessageContext, Mono<Void>> onComplete;
    private final Function<ServiceBusMessageContext, Mono<Void>> onAbandon;
    private final ClientLogger logger = new ClientLogger(FluxAutoComplete.class);

    FluxAutoComplete(Flux<? extends ServiceBusMessageContext> upstream, Semaphore completionLock,
                     Function<ServiceBusMessageContext, Mono<Void>> onComplete,
                     Function<ServiceBusMessageContext, Mono<Void>> onAbandon) {
        super(upstream);
        this.completionLock = completionLock;
        this.onComplete = Objects.requireNonNull(onComplete, "'onComplete' cannot be null.");
        this.onAbandon = Objects.requireNonNull(onAbandon, "'onAbandon' cannot be null.");
    }

    /**
     * Invoked when a downstream subscriber is interested in objects published from this operator.
     *
     * @param coreSubscriber The subscriber interested in the published items.
     */
    @Override
    public void subscribe(CoreSubscriber<? super ServiceBusMessageContext> coreSubscriber) {
        Objects.requireNonNull(coreSubscriber, "'coreSubscriber' cannot be null.");

        final AutoCompleteSubscriber subscriber =
            new AutoCompleteSubscriber(coreSubscriber, completionLock, onComplete, onAbandon, logger);

        source.subscribe(subscriber);
    }

    static final class AutoCompleteSubscriber extends BaseSubscriber<ServiceBusMessageContext> {
        private final CoreSubscriber<? super ServiceBusMessageContext> downstream;
        private final Function<ServiceBusMessageContext, Mono<Void>> onComplete;
        private final Function<ServiceBusMessageContext, Mono<Void>> onAbandon;
        private final Semaphore semaphore;
        private final ClientLogger logger;

        AutoCompleteSubscriber(CoreSubscriber<? super ServiceBusMessageContext> downstream,
                               Semaphore completionLock,
                               Function<ServiceBusMessageContext, Mono<Void>> onComplete,
                               Function<ServiceBusMessageContext, Mono<Void>> onAbandon, ClientLogger logger) {
            this.downstream = downstream;
            this.onComplete = onComplete;
            this.onAbandon = onAbandon;
            this.semaphore = completionLock;
            this.logger = logger;
        }

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            logger.info("Subscription received. Subscribing downstream. {}", subscription);
            downstream.onSubscribe(this);
        }

        @Override
        protected void hookOnNext(ServiceBusMessageContext value) {
            final ServiceBusReceivedMessage message = value.getMessage();
            final String sequenceNumber = message != null ? String.valueOf(message.getSequenceNumber()) : "n/a";

            logger.verbose("ON NEXT: Passing message downstream. sequenceNumber[{}]", sequenceNumber);
            try {
                semaphore.acquire();
            } catch (InterruptedException e) {
                logger.info("Unable to acquire semaphore.", e);
            }

            try {
                downstream.onNext(value);
                applyWithCatch(onComplete, value, "complete");
            } catch (Exception e) {
                logger.error("Error occurred processing message. Abandoning. sequenceNumber[{}]",
                    sequenceNumber, e);

                applyWithCatch(onAbandon, value, "abandon");
            } finally {
                logger.verbose("ON NEXT: Finished. sequenceNumber[{}]", sequenceNumber);
                semaphore.release();
            }
        }

        /**
         * On an error, will pass the exception downstream.
         *
         * @param throwable Error to pass downstream.
         */
        @Override
        protected void hookOnError(Throwable throwable) {
            logger.error("Error occurred. Passing downstream.", throwable);
            downstream.onError(throwable);
        }

        /**
         * On the completion. Will pass the complete signal downstream.
         */
        @Override
        protected void hookOnComplete() {
            downstream.onComplete();
        }

        @Override
        public Context currentContext() {
            return downstream.currentContext();
        }

        /**
         * Applies a function and catches then logs and closes any exceptions.
         *
         * @param function Function to apply.
         * @param context received message to apply function to.
         * @param operation The operation name.
         */
        private void applyWithCatch(Function<ServiceBusMessageContext, Mono<Void>> function,
            ServiceBusMessageContext context, String operation) {

            // Do not settle the message again if it has already been settled.
            if (context.getMessage() != null && context.getMessage().isSettled()) {
                return;
            }

            try {
                function.apply(context).block();
            } catch (Exception e) {
                logger.warning("Unable to '{}' message.", operation, e);

                // On an error, we'll stop requesting from upstream and pass the error downstream.
                upstream().cancel();
                onError(e);
            }
        }
    }
}
