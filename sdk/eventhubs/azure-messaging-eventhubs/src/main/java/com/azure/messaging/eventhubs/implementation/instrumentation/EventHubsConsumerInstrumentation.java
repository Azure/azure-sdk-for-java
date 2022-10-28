// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

import com.azure.core.util.Context;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.implementation.MessageUtils;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.message.Message;

import java.time.Instant;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;

public class EventHubsConsumerInstrumentation {
    private static final Symbol ENQUEUED_TIME_UTC_ANNOTATION_NAME_SYMBOL = Symbol.valueOf(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue());
    private final EventHubsTracer tracer;
    private final EventHubsMetricsProvider meter;
    private final boolean isSync;

    public EventHubsConsumerInstrumentation(Tracer tracer, Meter meter, String fullyQualifiedName, String entityName, String consumerGroup, boolean isSyncConsumer) {
        this.tracer = new EventHubsTracer(tracer, fullyQualifiedName, entityName);
        this.meter = new EventHubsMetricsProvider(meter, fullyQualifiedName, entityName, consumerGroup);
        this.isSync = isSyncConsumer;
    }

    public EventHubsTracer getTracer() {
        return tracer;
    }

    public Context asyncConsume(String spanName, Message message, String partitionId, Context parent) {
        if (!meter.isConsumerLagEnabled() && !tracer.isEnabled()) {
            return parent;
        }

        Instant enqueuedTime = MessageUtils.getEnqueuedTime(message.getMessageAnnotations().getValue(), ENQUEUED_TIME_UTC_ANNOTATION_NAME_SYMBOL);
        Context child = parent;
        if (tracer.isEnabled() && !isSync) {
            child = tracer.startSpan(spanName, tracer.setParentAndAttributes(message, enqueuedTime, parent), ProcessKind.PROCESS);
        }

        meter.reportReceive(enqueuedTime, partitionId, child);

        return child;
    }
}
