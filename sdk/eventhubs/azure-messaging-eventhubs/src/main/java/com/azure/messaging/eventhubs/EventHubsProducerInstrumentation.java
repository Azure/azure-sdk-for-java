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
import com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationScope;
import com.azure.messaging.eventhubs.implementation.instrumentation.OperationName;
import reactor.core.publisher.Mono;


import java.util.function.BiConsumer;

import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_BATCH_MESSAGE_COUNT;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_DESTINATION_PARTITION_ID;

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

        BiConsumer<EventHubsMetricsProvider, InstrumentationScope> reportMetricsCallback = (m, s) ->
             m.reportBatchSend(batch.getCount(), batch.getPartitionId(), s);


        return Mono.using(
                () -> new InstrumentationScope(tracer, meter, reportMetricsCallback)
                        .setSpan(startPublishSpanWithLinks(batch, Context.NONE)),
                scope -> publisher
                    .doOnError(scope::setError)
                    .doOnCancel(scope::setCancelled),
                InstrumentationScope::close);
    }

    public EventHubsTracer getTracer() {
        return tracer;
    }

    private Context startPublishSpanWithLinks(EventDataBatch batch, Context context) {
        if (!tracer.isEnabled()) {
            return context;
        }

        StartSpanOptions startOptions = tracer.createStartOptions(SpanKind.CLIENT, OperationName.SEND, null);
        if (batch != null) {
            startOptions.setAttribute(MESSAGING_BATCH_MESSAGE_COUNT, batch.getCount());
            if (batch.getPartitionId() != null) {
                startOptions.setAttribute(MESSAGING_DESTINATION_PARTITION_ID, batch.getPartitionId());
            }
            for (EventData event : batch.getEvents()) {
                startOptions.addLink(tracer.createProducerLink(event.getProperties(), event.getContext()));
            }
        }

        return tracer.startSpan(OperationName.SEND, startOptions, context);
    }

    private boolean isEnabled() {
        return tracer.isEnabled() || meter.isEnabled();
    }
}
