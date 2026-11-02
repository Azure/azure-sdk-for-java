// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.servicebus.implementation.instrumentation;

import com.azure.core.util.Context;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.servicebus.ServiceBusMessage;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Contains convenience methods to instrument specific sender calls with traces and metrics.
 */
public final class ServiceBusSenderInstrumentation {
    private final ServiceBusMeter meter;
    private final ServiceBusTracer tracer;

    public ServiceBusSenderInstrumentation(Tracer tracer, Meter meter, String fullyQualifiedName, String entityPath) {
        this.tracer = new ServiceBusTracer(tracer, fullyQualifiedName, entityPath);
        this.meter = new ServiceBusMeter(meter, fullyQualifiedName, entityPath, null);
    }

    public ServiceBusTracer getTracer() {
        return tracer;
    }

    /**
     * Traces send call and reports send count metric.
     */
    public <T> Mono<T> instrumentSendBatch(String spanName, Mono<T> publisher, List<ServiceBusMessage> batch) {
        if (!tracer.isEnabled() && !meter.isBatchSendEnabled()) {
            return publisher;
        }

        if (tracer.isEnabled()) {
            return Mono.defer(() -> {
                Context span
                    = tracer.startSpanWithLinks(spanName, ServiceBusTracer.OperationName.PUBLISH, batch, Context.NONE);
                return publisher.doOnEach(signal -> {
                    meter.reportBatchSend(batch.size(), signal.getThrowable(), false, span);
                    tracer.endSpan(signal.getThrowable(), span, null);
                }).doOnCancel(() -> {
                    meter.reportBatchSend(batch.size(), null, true, span);
                    tracer.cancelSpan(span);
                });
            });
        } else {
            return publisher
                .doOnEach(signal -> meter.reportBatchSend(batch.size(), signal.getThrowable(), false, Context.NONE))
                .doOnCancel(() -> meter.reportBatchSend(batch.size(), null, true, Context.NONE));
        }
    }
}
