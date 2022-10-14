// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.Context;
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
final class FluxTrace extends FluxOperator<ServiceBusMessageContext, ServiceBusMessageContext> {
    static final String PROCESS_ERROR_KEY = "process-error";
    private final ServiceBusReceiverInstrumentation instrumentation;

    FluxTrace(Flux<? extends ServiceBusMessageContext> upstream, ServiceBusReceiverInstrumentation instrumentation) {
        super(upstream);
        this.instrumentation = instrumentation;
    }

    @Override
    public void subscribe(CoreSubscriber<? super ServiceBusMessageContext> coreSubscriber) {
        Objects.requireNonNull(coreSubscriber, "'coreSubscriber' cannot be null.");

        source.subscribe(new TracingSubscriber(coreSubscriber, instrumentation));
    }

    private static class TracingSubscriber extends BaseSubscriber<ServiceBusMessageContext> {

        private final CoreSubscriber<? super ServiceBusMessageContext> downstream;
        private final ServiceBusReceiverInstrumentation instrumentation;
        private final ServiceBusTracer tracer;
        TracingSubscriber(CoreSubscriber<? super ServiceBusMessageContext> downstream, ServiceBusReceiverInstrumentation instrumentation) {
            this.downstream = downstream;
            this.instrumentation = instrumentation;
            this.tracer = instrumentation.getTracer();
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
        protected void hookOnNext(ServiceBusMessageContext message) {
            Throwable exception = null;
            Context span = instrumentation.instrumentProcess("ServiceBus.process", message.getMessage(), Context.NONE);
            AutoCloseable scope = tracer.makeSpanCurrent(span);

            try {
                downstream.onNext(message);
            } catch (Throwable t) {
                exception = t;
            } finally {
                Context context = message.getMessage().getContext();
                if (context != null) {
                    Object processorException = context.getData(PROCESS_ERROR_KEY).orElse(null);
                    if (processorException instanceof Throwable) {
                        exception = (Throwable) processorException;
                    }
                }
                tracer.endSpan(exception, span, scope);
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
