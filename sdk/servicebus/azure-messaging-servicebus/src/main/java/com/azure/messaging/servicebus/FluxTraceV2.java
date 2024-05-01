// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.messaging.servicebus.implementation.instrumentation.ReceiverKind;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusReceiverInstrumentation;
import org.apache.qpid.proton.message.Message;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * Flux operator that traces receive and process calls
 */
final class FluxTraceV2 {
    public static FluxOperator<ServiceBusReceivedMessage, ServiceBusReceivedMessage> createForReceivedMessage(Flux<? extends ServiceBusReceivedMessage> upstream, ServiceBusReceiverInstrumentation instrumentation) {
        return new FluxOperator<ServiceBusReceivedMessage, ServiceBusReceivedMessage>(upstream) {
            @Override
            public void subscribe(CoreSubscriber<? super ServiceBusReceivedMessage> actual) {
                Objects.requireNonNull(actual, "'actual' cannot be null.");
                source.subscribe(new TracingSubscriber<ServiceBusReceivedMessage>(actual, (msg, handler) -> instrumentation.instrumentProcess(msg, ReceiverKind.ASYNC_RECEIVER, handler)));
            }
        };
    }

    public static FluxOperator<Message, Message> createForMessage(Flux<? extends Message> upstream, ServiceBusReceiverInstrumentation instrumentation) {
        return new FluxOperator<Message, Message>(upstream) {
            @Override
            public void subscribe(CoreSubscriber<? super Message> actual) {
                Objects.requireNonNull(actual, "'actual' cannot be null.");
                source.subscribe(new TracingSubscriber<Message>(actual, (msg, handler) -> instrumentation.instrumentProcess(msg, ReceiverKind.ASYNC_RECEIVER, handler)));
            }
        };
    }

    private static class TracingSubscriber<T> extends BaseSubscriber<T> {

        private final CoreSubscriber<? super T> downstream;
        private final BiConsumer<T, Function<T, Throwable>> instrumentation;
        TracingSubscriber(CoreSubscriber<? super T> downstream, BiConsumer<T, Function<T, Throwable>> instrumentation) {
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
        protected void hookOnNext(T message) {
            instrumentation.accept(message, msg -> {
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
