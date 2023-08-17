// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.Context;
import com.azure.messaging.servicebus.implementation.instrumentation.ReceiverKind;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;

import java.util.Objects;

/**
 * Flux operator that traces receive and process calls
 */
final class FluxTraceV2 extends FluxOperator<ServiceBusReceivedMessage, ServiceBusReceivedMessage> {
    private final ServiceBusReceiverInstrumentation instrumentation;

    FluxTraceV2(Flux<? extends ServiceBusReceivedMessage> upstream, ServiceBusReceiverInstrumentation instrumentation) {
        super(upstream);
        this.instrumentation = instrumentation;
    }

    @Override
    public void subscribe(CoreSubscriber<? super ServiceBusReceivedMessage> coreSubscriber) {
        Objects.requireNonNull(coreSubscriber, "'coreSubscriber' cannot be null.");

        source.subscribe(new TracingSubscriber(coreSubscriber, instrumentation));
    }

    private static class TracingSubscriber extends BaseSubscriber<ServiceBusReceivedMessage> {

        private final CoreSubscriber<? super ServiceBusReceivedMessage> downstream;
        private final ServiceBusReceiverInstrumentation instrumentation;
        TracingSubscriber(CoreSubscriber<? super ServiceBusReceivedMessage> downstream, ServiceBusReceiverInstrumentation instrumentation) {
            this.downstream = downstream;
            this.instrumentation = instrumentation;
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
        protected void hookOnNext(ServiceBusReceivedMessage message) {
            instrumentation.instrumentProcess(message, ReceiverKind.ASYNC_RECEIVER, msg -> {
                downstream.onNext(msg);
                return null;
            });
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
