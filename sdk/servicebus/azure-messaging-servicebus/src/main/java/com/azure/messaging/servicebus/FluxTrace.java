// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.messaging.servicebus.implementation.ServiceBusReceiverTracer;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxOperator;

import java.util.Objects;

import static com.azure.messaging.servicebus.implementation.ServiceBusReceiverTracer.PROCESSING_ERROR_CONTEXT_KEY;

/**
 * Flux operator that traces receive and process calls
 */
final class FluxTrace extends FluxOperator<ServiceBusMessageContext, ServiceBusMessageContext> {
    private static final ClientLogger LOGGER = new ClientLogger(FluxTrace.class);
    private final ServiceBusReceiverTracer tracer;

    FluxTrace(Flux<? extends ServiceBusMessageContext> upstream, ServiceBusReceiverTracer tracer) {
        super(upstream);
        this.tracer = tracer;
    }

    @Override
    public void subscribe(CoreSubscriber<? super ServiceBusMessageContext> coreSubscriber) {
        Objects.requireNonNull(coreSubscriber, "'coreSubscriber' cannot be null.");

        source.subscribe(new TracingSubscriber(coreSubscriber, tracer));
    }

    private static class TracingSubscriber extends BaseSubscriber<ServiceBusMessageContext> {

        private final CoreSubscriber<? super ServiceBusMessageContext> downstream;
        private final ServiceBusReceiverTracer tracer;
        TracingSubscriber(CoreSubscriber<? super ServiceBusMessageContext> downstream, ServiceBusReceiverTracer tracer) {
            this.downstream = downstream;
            this.tracer = tracer;
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
            if (tracer == null || tracer.isSync()) {
                downstream.onNext(message);
                return;
            }

            Throwable exception = null;
            Context span = tracer.startProcessSpan("ServiceBus.process", message.getMessage(), Context.NONE);
            AutoCloseable scope = tracer.makeSpanCurrent(span);

            try {
                downstream.onNext(message);
            } catch (Throwable t) {
                exception = t;
            } finally {
                Object downstreamErrorObj = currentContext().getOrDefault(PROCESSING_ERROR_CONTEXT_KEY, null);
                if (downstreamErrorObj instanceof Exception) {
                    exception = (Exception) downstreamErrorObj;
                }

                try {
                    if (scope != null) {
                        scope.close();
                    }
                } catch (Exception e) {
                    throw LOGGER.logExceptionAsError(new RuntimeException(e));
                } finally {
                    tracer.endSpan(exception, span);
                }
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
