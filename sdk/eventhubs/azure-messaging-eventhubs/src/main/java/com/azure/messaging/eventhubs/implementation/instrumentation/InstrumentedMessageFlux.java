// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

import com.azure.core.amqp.implementation.MessageFlux;
import org.apache.qpid.proton.message.Message;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;

import java.util.Objects;

/**
 * Flux operator that instruments receive callbacks on async client
 */
public final class InstrumentedMessageFlux extends FluxOperator<Message, Message> {
    private final EventHubsConsumerInstrumentation instrumentation;
    private final String partitionId;

    private InstrumentedMessageFlux(MessageFlux upstream, String partitionId, EventHubsConsumerInstrumentation instrumentation) {
        super(upstream);
        this.instrumentation = instrumentation;
        this.partitionId = partitionId;
    }

    public static Flux<Message> instrument(MessageFlux source, String partitionId, EventHubsConsumerInstrumentation instrumentation) {
        if (instrumentation.isEnabled()) {
            return new InstrumentedMessageFlux(source, partitionId, instrumentation);
        }

        return source;
    }

    @Override
    public void subscribe(CoreSubscriber<? super Message> coreSubscriber) {
        Objects.requireNonNull(coreSubscriber, "'coreSubscriber' cannot be null.");

        source.subscribe(new TracingSubscriber(coreSubscriber, partitionId, instrumentation));
    }

    private static class TracingSubscriber extends BaseSubscriber<Message> {

        private final CoreSubscriber<? super Message> downstream;
        private final EventHubsConsumerInstrumentation instrumentation;
        private final String partitionId;

        TracingSubscriber(CoreSubscriber<? super Message> downstream, String partitionId, EventHubsConsumerInstrumentation instrumentation) {
            this.downstream = downstream;
            this.instrumentation = instrumentation;
            this.partitionId = partitionId;
        }

        @Override
        public reactor.util.context.Context currentContext() {
            return downstream.currentContext();
        }

        @Override
        protected void hookOnSubscribe(Subscription subscription) {
            downstream.onSubscribe(this);
        }

        @Override
        protected void hookOnNext(Message message) {
            InstrumentationScope scope = instrumentation.startAsyncConsume(message, partitionId);
            try {
                downstream.onNext(message);
            } catch (Exception e) {
                scope.setError(e);
                throw e;
            } finally {
                scope.close();
            }
        }

        @Override
        protected void hookOnError(Throwable throwable) {
            downstream.onError(throwable);
        }

        @Override
        protected void hookOnComplete() {
            downstream.onComplete();
        }
    }
}
