// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

import com.azure.core.util.Context;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.implementation.MessageUtils;
import com.azure.messaging.eventhubs.models.EventBatchContext;
import com.azure.messaging.eventhubs.models.EventContext;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import org.apache.qpid.proton.amqp.Symbol;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;

import java.time.Instant;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.util.tracing.SpanKind.CONSUMER;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_BATCH_MESSAGE_COUNT;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_EVENTHUBS_DESTINATION_PARTITION_ID;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.RECEIVE;

public class EventHubsConsumerInstrumentation {
    private static final Symbol ENQUEUED_TIME_UTC_ANNOTATION_NAME_SYMBOL = Symbol.valueOf(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue());
    private static final InstrumentationScope EMPTY_CONTEXT = new InstrumentationScope(null, null);
    private final EventHubsTracer tracer;
    private final EventHubsMetricsProvider meter;
    private final boolean isSync;

    public EventHubsConsumerInstrumentation(Tracer tracer, Meter meter, String fullyQualifiedName, String entityName, String consumerGroup, boolean isSyncConsumer) {
        this.tracer = new EventHubsTracer(tracer, fullyQualifiedName, entityName, consumerGroup);
        this.meter = new EventHubsMetricsProvider(meter, fullyQualifiedName, entityName, consumerGroup);
        this.isSync = isSyncConsumer;
    }

    public EventHubsTracer getTracer() {
        return tracer;
    }
    public EventHubsMetricsProvider getMeter() {
        return meter;
    }

    public InstrumentationScope createScope() {
        return isEnabled() ? new InstrumentationScope(tracer, meter) : EMPTY_CONTEXT;
    }

    public InstrumentationScope startAsyncConsume(Message message, String partitionId) {
        InstrumentationScope scope = createScope();
        if (!scope.isEnabled()) {
            return scope;
        }

        Instant enqueuedTime = MessageUtils.getEnqueuedTime(message.getMessageAnnotations().getValue(), ENQUEUED_TIME_UTC_ANNOTATION_NAME_SYMBOL);
        if (!isSync) {
            scope.recordStartTime()
                    .setSpan(tracer.startProcessSpan(message.getApplicationProperties().getValue(),
                            enqueuedTime,
                            partitionId,
                            Context.NONE))
                    .makeSpanCurrent();
        }

        if (enqueuedTime != null) {
            meter.reportLag(enqueuedTime, partitionId, scope);
        }
        return scope;
    }

    public void reportProcessMetrics(int batchSize, String partitionId, InstrumentationScope scope) {
        meter.reportProcess(batchSize, partitionId, scope);
    }

    public Flux<PartitionEvent> syncReceive(Flux<PartitionEvent> events, String partitionId) {
        if (!isEnabled()) {
            return events;
        }

        StartSpanOptions startOptions = tracer.isEnabled() ? tracer.createStartOption(CONSUMER, RECEIVE, partitionId) : null;
        Integer[] receivedCount = new Integer[]{0};

        return Flux.using(
                () ->  {
                    InstrumentationScope scope = new InstrumentationScope(tracer, meter);
                    if (startOptions != null) {
                        startOptions.setStartTimestamp(scope.getStartTime());
                    }

                    return scope.recordStartTime();
                },
                scope -> events
                        .doOnNext(partitionEvent -> {
                            if (startOptions != null) {
                                receivedCount[0] = receivedCount[0] + 1;
                                EventData data = partitionEvent.getData();
                                startOptions.addLink(tracer.createLink(data.getProperties(), data.getEnqueuedTime()));
                            }
                        })
                        .doOnError(scope::setError)
                        .doOnCancel(scope::setCancelled),
                scope -> {
                    if (startOptions != null) {
                        startOptions.setAttribute(MESSAGING_BATCH_MESSAGE_COUNT, receivedCount[0]);
                        startOptions.setAttribute(MESSAGING_EVENTHUBS_DESTINATION_PARTITION_ID, partitionId);

                        scope.setSpan(tracer.startSpan(RECEIVE, startOptions, Context.NONE));
                    }
                    meter.reportReceiveDuration(receivedCount[0], partitionId, scope);
                    scope.close();
                });
    }

    public InstrumentationScope startProcess(EventBatchContext batchContext, InstrumentationScope scope) {
        if (batchContext.getEvents().isEmpty() || !scope.isEnabled()) {
            return scope;
        }

        Context span = tracer.startProcessSpan(batchContext, Context.NONE);
        return scope.setSpan(span)
                .makeSpanCurrent();
    }

    public InstrumentationScope startProcess(EventContext eventContext, InstrumentationScope ctx) {
        EventData event = eventContext.getEventData();
        if (event == null || !ctx.isEnabled()) {
            return ctx;
        }

        Context span = tracer.startProcessSpan(event.getProperties(),
            event.getEnqueuedTime(),
            eventContext.getPartitionContext().getPartitionId(),
            Context.NONE);
        return ctx.setSpan(span).makeSpanCurrent();
    }

    boolean isEnabled() {
        return tracer.isEnabled() || meter.isEnabled();
    }
}
