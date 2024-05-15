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
final class TracingFluxOperator<T> extends BaseSubscriber<T> {

    public static <T> Flux<T> create(Flux<T> upstream, ServiceBusReceiverInstrumentation instrumentation) {
        if (!instrumentation.isEnabled() && instrumentation.isAsyncReceiverInstrumentation()) {
            return upstream;
        }

        return new FluxOperator<T, T>(upstream) {
            @SuppressWarnings("unchecked")
            @Override
            public void subscribe(CoreSubscriber<? super T> actual) {
                Objects.requireNonNull(actual, "'actual' cannot be null.");
                source.subscribe(new TracingFluxOperator<T>(actual, (msg, handler) -> {
                    if (msg instanceof Message) {
                        instrumentation.instrumentProcess((Message) msg, ReceiverKind.ASYNC_RECEIVER,
                            (Function<Message, Throwable>) handler);
                    } else if (msg instanceof ServiceBusReceivedMessage) {
                        instrumentation.instrumentProcess((ServiceBusReceivedMessage) msg, ReceiverKind.ASYNC_RECEIVER,
                            (Function<ServiceBusReceivedMessage, Throwable>) handler);
                    }
                }));
            }
        };
    }

    private final CoreSubscriber<? super T> downstream;
    private final BiConsumer<T, Function<T, Throwable>> instrumentation;
    private TracingFluxOperator(CoreSubscriber<? super T> downstream, BiConsumer<T, Function<T, Throwable>> instrumentation) {
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

