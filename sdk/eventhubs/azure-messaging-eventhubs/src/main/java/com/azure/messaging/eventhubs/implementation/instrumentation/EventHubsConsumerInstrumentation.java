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
import org.apache.qpid.proton.amqp.messaging.ApplicationProperties;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

import static com.azure.core.amqp.AmqpMessageConstant.ENQUEUED_TIME_UTC_ANNOTATION_NAME;
import static com.azure.core.util.tracing.SpanKind.CLIENT;
import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;
import static com.azure.messaging.eventhubs.implementation.instrumentation.InstrumentationUtils.MESSAGING_BATCH_MESSAGE_COUNT;
import static com.azure.messaging.eventhubs.implementation.instrumentation.OperationName.RECEIVE;

public final class EventHubsConsumerInstrumentation {
    private static final Symbol ENQUEUED_TIME_UTC_ANNOTATION_NAME_SYMBOL
        = Symbol.valueOf(ENQUEUED_TIME_UTC_ANNOTATION_NAME.getValue());
    private static final InstrumentationScope NOOP_SCOPE = new InstrumentationScope(null, null, null);
    private final EventHubsTracer tracer;
    private final EventHubsMetricsProvider meter;
    private final boolean isSync;

    public EventHubsConsumerInstrumentation(Tracer tracer, Meter meter, String fullyQualifiedName, String entityName,
        String consumerGroup, boolean isSyncConsumer) {
        this.tracer = new EventHubsTracer(tracer, fullyQualifiedName, entityName, consumerGroup);
        this.meter = new EventHubsMetricsProvider(meter, fullyQualifiedName, entityName, consumerGroup);
        this.isSync = isSyncConsumer;
    }

    public EventHubsTracer getTracer() {
        return tracer;
    }

    public InstrumentationScope
        createScope(BiConsumer<EventHubsMetricsProvider, InstrumentationScope> reportMetricsCallback) {
        return isEnabled() ? new InstrumentationScope(tracer, meter, reportMetricsCallback) : NOOP_SCOPE;
    }

    public InstrumentationScope startAsyncConsume(Message message, String partitionId) {
        if (!isEnabled()) {
            return NOOP_SCOPE;
        }

        InstrumentationScope scope = createScope((m, s) -> {
            if (!isSync) {
                m.reportProcess(1, partitionId, s);
            }
        });
        Instant enqueuedTime = MessageUtils.getEnqueuedTime(message.getMessageAnnotations().getValue(),
            ENQUEUED_TIME_UTC_ANNOTATION_NAME_SYMBOL);
        if (!isSync) {
            ApplicationProperties properties = message.getApplicationProperties();
            scope
                .setSpan(tracer.startProcessSpan(properties == null ? null : properties.getValue(), enqueuedTime,
                    partitionId))
                .makeSpanCurrent();
        }

        if (enqueuedTime != null) {
            meter.reportLag(enqueuedTime, partitionId, scope);
        }
        return scope;
    }

    public Flux<PartitionEvent> syncReceive(Flux<PartitionEvent> events, String partitionId) {
        if (!isEnabled()) {
            return events;
        }

        return Flux.deferContextual(context -> {
            Context parent = EventHubsTracer.getParent(context);
            // The messaging receive span is created at completion so it can link all delivered messages.
            StartSpanOptions options = tracer.isEnabled()
                ? tracer.createStartOptions(CLIENT, RECEIVE, partitionId).setStartTimestamp(Instant.now())
                : null;
            Context logical = tracer.startAmqpSpan("EventHubs.receive", "receive", CLIENT, parent);
            Context wait = tracer.startAmqpSpan("EventHubs.receive.wait", "receive", CLIENT, logical);
            return Flux.using(() -> createScope((m, s) -> {
                m.reportReceive(s.getEventCount(), partitionId, s);
                m.reportOperation(RECEIVE, s.getEventCount(), s);
            }), scope -> events.doOnNext(event -> {
                scope.addEvents(1);
                if (options != null) {
                    EventData data = event.getData();
                    options.addLink(tracer.createLink(data.getProperties(), data.getEnqueuedTime()));
                }
            })
                .doOnError(scope::setError)
                .doOnCancel(scope::setCancelled)
                .contextWrite(c -> c.put(PARENT_TRACE_CONTEXT_KEY, wait)), scope -> {
                    tracer.endAmqpSpan(wait, scope.getOutcome());
                    if (options != null) {
                        options.setAttribute(MESSAGING_BATCH_MESSAGE_COUNT, scope.getEventCount());
                        scope.setSpan(tracer.startSpan(RECEIVE, options, logical));
                    }
                    scope.close();
                    tracer.setEventCount(logical, scope.getEventCount());
                    tracer.endAmqpSpan(logical, scope.getOutcome());
                });
        });
    }

    public Flux<PartitionEvent> clientReceive(Flux<PartitionEvent> events) {
        // A synchronous client measures its bounded receive windows. A processor wraps its partition pump instead.
        return isSync ? events : streamReceive(events);
    }

    public Flux<PartitionEvent> streamReceive(Flux<PartitionEvent> events) {
        if (!isEnabled()) {
            return events;
        }
        return Flux.deferContextual(context -> {
            Context parent = EventHubsTracer.getParent(context);
            Context logical = tracer.startAmqpSpan("EventHubs.receive", "receive", CLIENT, parent);
            // A continuous subscription can run for days. Measure the initial wait once per partition subscription.
            AtomicReference<Context> firstEvent
                = new AtomicReference<>(tracer.startAmqpSpan("EventHubs.receive.wait", "receive", CLIENT, logical));
            return Flux.using(() -> createScope((m, s) -> m.reportOperation(RECEIVE, 0, s)).setSpan(logical),
                scope -> events.doOnNext(event -> {
                    scope.addEvents(1);
                    if (firstEvent.get() != null) {
                        endFirstWait(firstEvent, "success");
                    }
                    meter.reportStreamDelivery(scope);
                })
                    .doOnError(scope::setError)
                    .doOnCancel(scope::setCancelled)
                    .contextWrite(c -> c.put(PARENT_TRACE_CONTEXT_KEY, logical)),
                scope -> {
                    // take(), processor shutdown and ownership changes normally cancel a partition stream.
                    String spanOutcome = scope.getError() == null ? "success" : "error";
                    endFirstWait(firstEvent, spanOutcome);
                    scope.closeLogical(spanOutcome);
                });
        });
    }

    private void endFirstWait(AtomicReference<Context> wait, String outcome) {
        Context span = wait.getAndSet(null);
        if (span != null) {
            tracer.endAmqpSpan(span, outcome);
        }
    }

    public InstrumentationScope startProcess(EventBatchContext batchContext) {
        if (batchContext.getEvents().isEmpty() || !isEnabled()) {
            return NOOP_SCOPE;
        }

        InstrumentationScope scope = createScope((m, s) -> m.reportProcess(batchContext.getEvents().size(),
            batchContext.getPartitionContext().getPartitionId(), s));

        return scope.setSpan(tracer.startProcessSpan(batchContext)).makeSpanCurrent();
    }

    public InstrumentationScope startProcess(EventContext eventContext) {
        EventData event = eventContext.getEventData();
        if (event == null || !isEnabled()) {
            return NOOP_SCOPE;
        }

        InstrumentationScope scope
            = createScope((m, s) -> m.reportProcess(1, eventContext.getPartitionContext().getPartitionId(), s));

        Context span = tracer.startProcessSpan(event.getProperties(), event.getEnqueuedTime(),
            eventContext.getPartitionContext().getPartitionId());

        return scope.setSpan(span).makeSpanCurrent();
    }

    public <T> Mono<T> instrumentMono(Mono<T> publisher, OperationName operationName, String partitionId) {
        if (!isEnabled()) {
            return publisher;
        }

        return Mono.using(
            () -> createScope((m, s) -> m.reportGenericOperationDuration(operationName, partitionId, s))
                .setSpan(tracer.startGenericOperationSpan(operationName, partitionId, Context.NONE)),
            scope -> publisher.doOnError(scope::setError)
                .doOnCancel(scope::setCancelled)
                .contextWrite(c -> c.put(PARENT_TRACE_CONTEXT_KEY, scope.getSpan())),
            InstrumentationScope::close);
    }

    public boolean isEnabled() {
        return tracer.isEnabled() || meter.isEnabled();
    }
}
