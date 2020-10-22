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
import java.util.function.Function;

/**
 * Flux operator that auto-completes or auto-abandons messages when control is returned successfully.
 */
final class FluxAutoComplete extends FluxOperator<ServiceBusReceivedMessageContext, ServiceBusReceivedMessageContext> {
    private final Function<ServiceBusReceivedMessageContext, Mono<Void>> onComplete;
    private final Function<ServiceBusReceivedMessageContext, Mono<Void>> onAbandon;
    private final ClientLogger logger = new ClientLogger(FluxAutoComplete.class);

    FluxAutoComplete(Flux<? extends ServiceBusReceivedMessageContext> upstream,
        Function<ServiceBusReceivedMessageContext, Mono<Void>> onComplete,
        Function<ServiceBusReceivedMessageContext, Mono<Void>> onAbandon) {
        super(upstream);
        this.onComplete = Objects.requireNonNull(onComplete, "'onComplete' cannot be null.");
        this.onAbandon = Objects.requireNonNull(onAbandon, "'onAbandon' cannot be null.");
    }

    /**
     * Invoked when a downstream subscriber is interested in objects published from this operator.
     *
     * @param coreSubscriber The subscriber interested in the published items.
     */
    @Override
    public void subscribe(CoreSubscriber<? super ServiceBusReceivedMessageContext> coreSubscriber) {
        Objects.requireNonNull(coreSubscriber, "'coreSubscriber' cannot be null.");

        final AutoCompleteSubscriber subscriber =
            new AutoCompleteSubscriber(coreSubscriber, onComplete, onAbandon, logger);

        source.subscribe(subscriber);
    }

    static final class AutoCompleteSubscriber extends BaseSubscriber<ServiceBusReceivedMessageContext> {
        private final CoreSubscriber<? super ServiceBusReceivedMessageContext> downstream;
        private final Function<ServiceBusReceivedMessageContext, Mono<Void>> onComplete;
        private final Function<ServiceBusReceivedMessageContext, Mono<Void>> onAbandon;
        private final ClientLogger logger;

        AutoCompleteSubscriber(CoreSubscriber<? super ServiceBusReceivedMessageContext> downstream,
            Function<ServiceBusReceivedMessageContext, Mono<Void>> onComplete,
            Function<ServiceBusReceivedMessageContext, Mono<Void>> onAbandon, ClientLogger logger) {
            this.downstream = downstream;
            this.onComplete = onComplete;
            this.onAbandon = onAbandon;
            this.logger = logger;
        }

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            logger.info("Subscription received. Subscribing downstream. {}", subscription);
            downstream.onSubscribe(this);
            requestUnbounded();
        }

        @Override
        protected void hookOnNext(ServiceBusReceivedMessageContext value) {
            final ServiceBusReceivedMessage message = value.getMessage();
            final String sequenceNumber = message != null ? String.valueOf(message.getSequenceNumber()) : "n/a";

            logger.verbose("Passing message downstream. sequenceNumber[{}]", sequenceNumber);

            try {
                downstream.onNext(value);
                applyWithCatch(onComplete, value, "complete");
            } catch (Exception e) {
                logger.error("Error occurred processing message. Abandoning. sequenceNumber[{}]",
                    sequenceNumber, e);

                applyWithCatch(onAbandon, value, "abandon");
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
         * @param message received message to apply function to.
         * @param operation The operation name.
         */
        private void applyWithCatch(Function<ServiceBusReceivedMessageContext, Mono<Void>> function,
            ServiceBusReceivedMessageContext message, String operation) {
            try {
                function.apply(message).block();
            } catch (Exception e) {
                logger.warning("Unable to '{}' message.", operation, e);
                onError(e);
            }
        }
    }
}
