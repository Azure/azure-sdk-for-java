// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs;

import com.azure.core.util.Context;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsMetricsProvider;
import com.azure.messaging.eventhubs.implementation.instrumentation.EventHubsTracer;
import com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationScope;
import com.azure.messaging.eventhubs.implementation.instrumentation.OperationName;
import reactor.core.publisher.Mono;

import static com.azure.core.util.tracing.SpanKind.CLIENT;
import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_BATCH_MESSAGE_COUNT;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_DESTINATION_PARTITION_ID;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.SEND;

class EventHubsProducerInstrumentation {
    private final EventHubsTracer tracer;
    private final EventHubsMetricsProvider meter;
    EventHubsProducerInstrumentation(Tracer tracer, Meter meter, String fullyQualifiedName, String entityName) {
        this.tracer = new EventHubsTracer(tracer, fullyQualifiedName, entityName, null);
        this.meter = new EventHubsMetricsProvider(meter, fullyQualifiedName, entityName, null);
    }

    <T> Mono<T> sendBatch(Mono<T> publisher, EventDataBatch batch) {
        if (!isEnabled()) {
            return publisher;
        }

        return Mono.using(
                () -> new InstrumentationScope(tracer, meter,
                            (m, s) -> m.reportBatchSend(batch.getCount(), batch.getPartitionId(), s))
                        .setSpan(startPublishSpanWithLinks(batch)),
                scope -> publisher
                    .doOnError(scope::setError)
                    .doOnCancel(scope::setCancelled),
                InstrumentationScope::close);
    }

    public EventHubsTracer getTracer() {
        return tracer;
    }

    public <T> Mono<T> instrumentMono(Mono<T> publisher, OperationName operationName, String partitionId) {
        if (!isEnabled()) {
            return publisher;
        }

        return Mono.using(
            () -> new InstrumentationScope(tracer, meter, (m, s) -> m.reportGenericOperationDuration(operationName, partitionId, s))
                .setSpan(tracer.startGenericOperationSpan(operationName, partitionId, Context.NONE)),
            scope -> publisher
                .doOnError(scope::setError)
                .doOnCancel(scope::setCancelled)
                .contextWrite(c -> c.put(PARENT_TRACE_CONTEXT_KEY, scope.getSpan())),
            InstrumentationScope::close);
    }

    private Context startPublishSpanWithLinks(EventDataBatch batch) {
        if (!tracer.isEnabled()) {
            return Context.NONE;
        }

        StartSpanOptions startOptions = tracer.createStartOptions(CLIENT, SEND, null);
        if (batch != null) {
            startOptions.setAttribute(MESSAGING_BATCH_MESSAGE_COUNT, batch.getCount());
            if (batch.getPartitionId() != null) {
                startOptions.setAttribute(MESSAGING_DESTINATION_PARTITION_ID, batch.getPartitionId());
            }
            for (EventData event : batch.getEvents()) {
                startOptions.addLink(tracer.createProducerLink(event.getProperties(), event.getContext()));
            }
        }

        return tracer.startSpan(SEND, startOptions, Context.NONE);
    }

    private boolean isEnabled() {
        return tracer.isEnabled() || meter.isEnabled();
    }
}
