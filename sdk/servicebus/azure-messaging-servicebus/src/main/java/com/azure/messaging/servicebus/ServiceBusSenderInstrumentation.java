// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus;

import com.azure.core.util.Context;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusMeter;
import com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.azure.messaging.servicebus.implementation.instrumentation.ServiceBusTracer.REACTOR_PARENT_TRACE_CONTEXT_KEY;

/**
 * Contains convenience methods to instrument specific sender calls with traces and metrics.
 */
class ServiceBusSenderInstrumentation {
    private final ServiceBusMeter meter;
    private final ServiceBusTracer tracer;

    ServiceBusSenderInstrumentation(Tracer tracer, Meter meter, String fullyQualifiedName, String entityPath) {
        this.tracer = new ServiceBusTracer(tracer, fullyQualifiedName, entityPath);
        this.meter = new ServiceBusMeter(meter, fullyQualifiedName, entityPath, null);
    }

    ServiceBusTracer getTracer() {
        return tracer;
    }

    /**
     * Traces send call and reports send count metric.
     */
    <T> Mono<T> instrumentSendBatch(String spanName, Mono<T> publisher, List<ServiceBusMessage> batch) {
        if (!tracer.isEnabled() && !meter.isBatchSendEnabled()) {
            return publisher;
        }

        if (tracer.isEnabled()) {
            return publisher
                .doOnEach(signal -> {
                    Context span = signal.getContextView().getOrDefault(REACTOR_PARENT_TRACE_CONTEXT_KEY, Context.NONE);
                    meter.reportBatchSend(batch.size(), signal.getThrowable(), span);
                    tracer.endSpan(signal.getThrowable(), span, null);
                })
                .contextWrite(reactor.util.context.Context.of(REACTOR_PARENT_TRACE_CONTEXT_KEY, tracer.startSpanWithLinks(spanName, batch,
                    ServiceBusMessage::getContext, Context.NONE)));
        } else {
            return publisher
                .doOnEach(signal -> {
                    meter.reportBatchSend(batch.size(), signal.getThrowable(), Context.NONE);
                });
        }
    }
}
