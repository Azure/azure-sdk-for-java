// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.Context;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsMetricsProvider;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer;
import reactor.core.publisher.Mono;

import static com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer.REACTOR_PARENT_TRACE_CONTEXT_KEY;

class EventHubsProducerInstrumentation {

    private final EventHubsTracer tracer;
    private final EventHubsMetricsProvider meter;
    EventHubsProducerInstrumentation(Tracer tracer, Meter meter, String fullyQualifiedName, String entityName) {
        this.tracer = new EventHubsTracer(tracer, fullyQualifiedName, entityName);
        this.meter = new EventHubsMetricsProvider(meter, fullyQualifiedName, entityName, null);
    }

    <T> Mono<T> onSendBatch(Mono<T> publisher, EventDataBatch batch, String spanName) {
        if (!tracer.isEnabled() && !meter.isSendCountEnabled()) {
            return publisher;
        }

        if (tracer.isEnabled()) {
            return publisher
                .doOnEach(signal -> {
                    if (signal.isOnComplete() || signal.isOnError()) {
                        Context span = signal.getContextView().getOrDefault(REACTOR_PARENT_TRACE_CONTEXT_KEY, Context.NONE);
                        meter.reportBatchSend(batch.getCount(), batch.getPartitionId(), signal.getThrowable(), span);
                        tracer.endSpan(signal.getThrowable(), span, null);
                    }
                })
                .contextWrite(reactor.util.context.Context.of(REACTOR_PARENT_TRACE_CONTEXT_KEY, startSpanWithLinks(spanName, batch, Context.NONE)));
        } else {
            return publisher
                .doOnEach(signal -> {
                    if (signal.isOnComplete() || signal.isOnError()) {
                        meter.reportBatchSend(batch.getCount(), batch.getPartitionId(), signal.getThrowable(), Context.NONE);
                    }
                });
        }
    }

    public EventHubsTracer getTracer() {
        return tracer;
    }

    private Context startSpanWithLinks(String name, EventDataBatch batch, Context context) {
        StartSpanOptions startOptions = tracer.createStartOption(SpanKind.CLIENT);
        if (batch != null) {
            for (EventData event : batch.getEvents()) {
                startOptions.addLink(tracer.createLink(event.getProperties(), null, event.getContext()));
            }
        }

        return tracer.startSpan(name, startOptions, context);
    }
}
