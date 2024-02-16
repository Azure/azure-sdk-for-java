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


import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_BATCH_MESSAGE_COUNT;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_EVENTHUBS_DESTINATION_PARTITION_ID;

class EventHubsProducerInstrumentation {
    private final EventHubsTracer tracer;
    private final EventHubsMetricsProvider meter;
    EventHubsProducerInstrumentation(Tracer tracer, Meter meter, String fullyQualifiedName, String entityName) {
        this.tracer = new EventHubsTracer(tracer, fullyQualifiedName, entityName, null);
        this.meter = new EventHubsMetricsProvider(meter, fullyQualifiedName, entityName, null);
    }

    <T> Mono<T> onSendBatch(Mono<T> publisher, EventDataBatch batch) {
        if (!isEnabled()) {
            return publisher;
        }

        return Mono.using(
                () -> new InstrumentationScope(tracer, meter).recordStartTime().setSpan(startPublishSpanWithLinks(batch, Context.NONE)),
                ctx -> publisher
                    .doOnError(ctx::setError)
                    .doOnCancel(ctx::setCancelled),
                ctx -> {
                    meter.reportBatchSend(batch.getCount(), batch.getPartitionId(), ctx);
                    ctx.close();
                });
    }

    public EventHubsTracer getTracer() {
        return tracer;
    }

    private Context startPublishSpanWithLinks(EventDataBatch batch, Context context) {
        if (!tracer.isEnabled()) {
            return context;
        }

        StartSpanOptions startOptions = tracer.createStartOption(SpanKind.CLIENT, OperationName.PUBLISH, null);
        if (batch != null) {
            startOptions.setAttribute(MESSAGING_BATCH_MESSAGE_COUNT, batch.getCount());
            if (batch.getPartitionId() != null) {
                startOptions.setAttribute(MESSAGING_EVENTHUBS_DESTINATION_PARTITION_ID, batch.getPartitionId());
            }
            for (EventData event : batch.getEvents()) {
                startOptions.addLink(tracer.createProducerLink(event.getProperties(), event.getContext()));
            }
        }

        return tracer.startSpan(OperationName.PUBLISH, startOptions, context);
    }

    private boolean isEnabled() {
        return tracer.isEnabled() || meter.isEnabled();
    }
}
