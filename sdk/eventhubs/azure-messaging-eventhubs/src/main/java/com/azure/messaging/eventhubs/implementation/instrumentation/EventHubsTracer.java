// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.StartSpanOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracingLink;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import static com.azure.core.util.tracing.Tracer.ENTITY_PATH_KEY;
import static com.azure.core.util.tracing.Tracer.HOST_NAME_KEY;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;

public class EventHubsTracer {
    private static final AutoCloseable NOOP_AUTOCLOSEABLE = () -> {
    };

    public static final String REACTOR_PARENT_TRACE_CONTEXT_KEY = "otel-context-key";
    public static final String TRACEPARENT_KEY = "traceparent";
    public static final String DIAGNOSTIC_ID_KEY = "Diagnostic-Id";
    public static final String MESSAGE_ENQUEUED_TIME_ATTRIBUTE_NAME = "messaging.eventhubs.message.enqueued_time";
    public static final String MESSAGING_BATCH_SIZE_ATTRIBUTE_NAME = "messaging.batch.message_count";
    private static final String MESSAGING_SYSTEM_ATTRIBUTE_NAME = "messaging.system";
    private static final String MESSAGING_OPERATION_ATTRIBUTE_NAME = "messaging.operation";

    private static final ClientLogger LOGGER = new ClientLogger(EventHubsTracer.class);

    private static final boolean IS_TRACING_DISABLED = Configuration.getGlobalConfiguration().get(Configuration.PROPERTY_AZURE_TRACING_DISABLED, false);
    protected final Tracer tracer;
    private final String fullyQualifiedName;
    private final String entityName;

    public EventHubsTracer(Tracer tracer, String fullyQualifiedName, String entityName) {
        this.tracer = IS_TRACING_DISABLED ? null : tracer;
        this.fullyQualifiedName = Objects.requireNonNull(fullyQualifiedName, "'fullyQualifiedName' cannot be null");
        this.entityName = Objects.requireNonNull(entityName, "'entityPath' cannot be null");
    }

    public boolean isEnabled() {
        return tracer != null;// && tracer.isEnabled();
    }

    public Context startSpan(String spanName, StartSpanOptions startOptions, Context context) {
        return isEnabled() ? tracer.start(spanName, startOptions, context) : context;
    }

    public <T> Mono<T> traceMono(Mono<T> publisher, String spanName) {
        if (isEnabled()) {
            return publisher
                .doOnEach(signal -> {
                    if (signal.isOnComplete() || signal.isOnError()) {
                        Context span = signal.getContextView().getOrDefault(REACTOR_PARENT_TRACE_CONTEXT_KEY, Context.NONE);
                        endSpan(signal.getThrowable(), span, null);
                    }
                })
                .contextWrite(reactor.util.context.Context.of(REACTOR_PARENT_TRACE_CONTEXT_KEY,
                    tracer.start(spanName, createStartOption(SpanKind.CLIENT, null), Context.NONE)));
        }

        return publisher;
    }

    public void endSpan(Throwable throwable, Context span, AutoCloseable scope) {
        if (isEnabled()) {
            String errorCondition = null;
            if (throwable instanceof AmqpException) {
                AmqpException exception = (AmqpException) throwable;
                errorCondition = exception.getErrorCondition().getErrorCondition();
            }

            try {
                if (scope != null) {
                    scope.close();
                }
            } catch (Exception e) {
                LOGGER.warning("Can't close scope", e);
            } finally {
                tracer.end(errorCondition, throwable, span);
            }
        }
    }

    /**
     * Used in ServiceBusMessageBatch.tryAddMessage() to start tracing for to-be-sent out messages.
     */
    public void reportMessageSpan(EventData eventData, Context eventContext) {
        if (!isEnabled() || eventContext == null || eventContext.getData(SPAN_CONTEXT_KEY).isPresent()) {
            // if message has context (in case of retries), don't start a message span or add a new context
            return;
        }

        String traceparent = EventHubsTracer.getTraceparent(eventData.getProperties());
        if (traceparent != null) {
            // if message has context (in case of retries) or if user supplied it, don't start a message span or add a new context
            return;
        }

        // Starting the span makes the sampling decision (nothing is logged at this time)
        StartSpanOptions startOptions = createStartOption(SpanKind.PRODUCER, null);

        Context eventSpanContext = tracer.start("EventHubs.message", startOptions, eventContext);

        Exception exception = null;
        String error = null;
        if (canModifyApplicationProperties(eventData.getProperties())) {
            try {
                tracer.injectContext((key, value) -> {
                    eventData.getProperties().put(key, value);
                    if (TRACEPARENT_KEY.equals(key)) {
                        eventData.getProperties().put(DIAGNOSTIC_ID_KEY, value);
                    }
                }, eventSpanContext);
            } catch (RuntimeException ex) {
                // it might happen that unmodifiable map is something that we
                // didn't account for in canModifyApplicationProperties method
                // if it happens, let's not break everything, but log a warning
                LOGGER.warning("Failed to inject context into EventData", ex);
                exception = ex;
            }
        } else {
            error = "failed to inject context into EventData";
        }
        tracer.end(error, exception, eventSpanContext);

        Optional<Object> spanContext = eventSpanContext.getData(SPAN_CONTEXT_KEY);
        if (spanContext.isPresent()) {
            eventData.addContext(SPAN_CONTEXT_KEY, spanContext.get());
        }
    }

    private static boolean canModifyApplicationProperties(Map<String, Object> applicationProperties) {
        return !applicationProperties.getClass().getSimpleName().equals("UnmodifiableMap");
    }

    public TracingLink createLink(Map<String, Object> applicationProperties, Instant enqueuedTime, Context eventContext) {
        Context link = Context.NONE;
        Optional<Object> linkContext = eventContext.getData(SPAN_CONTEXT_KEY);
        if (linkContext.isPresent()) {
            if (linkContext.get() instanceof Context) {
                link = (Context) linkContext.get();
            } else {
                LOGGER.verbose("Unexpected type under 'span-context' key - {}", linkContext.get().getClass());
            }
        } else {
            link = extractContext(applicationProperties);
        }

        Map<String, Object> linkAttributes = null;
        if (enqueuedTime != null) {
            linkAttributes = Collections.singletonMap(MESSAGE_ENQUEUED_TIME_ATTRIBUTE_NAME, enqueuedTime.atOffset(ZoneOffset.UTC).toEpochSecond());
        }

        return new TracingLink(link, linkAttributes);
    }

    public Context extractContext(Map<String, Object> applicationProperties) {
        return tracer.extractContext(key ->  {
            if (TRACEPARENT_KEY.equals(key)) {
                return getTraceparent(applicationProperties);
            } else {
                Object value = applicationProperties.get(key);
                if (value != null) {
                    return value.toString();
                }
            }
            return null;
        });
    }

    public AutoCloseable makeSpanCurrent(Context context) {
        return isEnabled() ? tracer.makeSpanCurrent(context) : NOOP_AUTOCLOSEABLE;
    }

    public Context startProcessSpan(String name, EventData event, Context parent) {
        if (isEnabled()) {
            StartSpanOptions startOptions = createStartOption(SpanKind.CONSUMER, OperationName.PROCESS)
                .setRemoteParent(extractContext(event.getProperties()));

            Instant enqueuedTime = event.getEnqueuedTime();
            if (enqueuedTime != null) {
                startOptions.setAttribute(MESSAGE_ENQUEUED_TIME_ATTRIBUTE_NAME, enqueuedTime.atOffset(ZoneOffset.UTC).toEpochSecond());
            }

            return tracer.start(name, startOptions, parent);
        }

        return parent;
    }

    public Context startProcessSpan(String name, List<EventData> events, Context parent) {
        if (isEnabled() && events != null) {
            StartSpanOptions startOptions = createStartOption(SpanKind.CONSUMER, OperationName.PROCESS);
            startOptions.setAttribute(MESSAGING_BATCH_SIZE_ATTRIBUTE_NAME, events.size());
            for (EventData event : events) {
                startOptions.addLink(createLink(event.getProperties(), event.getEnqueuedTime(), Context.NONE));
            }

            return tracer.start(name, startOptions, parent);
        }

        return parent;
    }

    public Flux<PartitionEvent> reportSyncReceiveSpan(String name, Instant startTime, Flux<PartitionEvent> events, Context parent) {
        if (isEnabled()) {
            final StartSpanOptions startOptions = createStartOption(SpanKind.CLIENT, OperationName.RECEIVE)
                .setStartTimestamp(startTime);

            return events.doOnEach(signal -> {
                if (signal.hasValue()) {
                    EventData data = signal.get().getData();
                    startOptions.addLink(createLink(data.getProperties(), data.getEnqueuedTime(), Context.NONE));
                } else if (signal.isOnComplete() || signal.isOnError()) {
                    int batchSize = startOptions.getLinks() == null ? 0 : startOptions.getLinks().size();
                    startOptions.setAttribute(MESSAGING_BATCH_SIZE_ATTRIBUTE_NAME, batchSize);

                    Context span = tracer.start(name, startOptions, parent);
                    tracer.end(null, signal.getThrowable(), span);
                }
            });
        }

        return events;
    }

    private static String getTraceparent(Map<String, Object> applicationProperties) {
        Object diagnosticId = applicationProperties.get(DIAGNOSTIC_ID_KEY);
        if (diagnosticId == null) {
            diagnosticId = applicationProperties.get(TRACEPARENT_KEY);
        }

        return diagnosticId == null ? null : diagnosticId.toString();
    }

    public StartSpanOptions createStartOption(SpanKind kind, OperationName operationName) {
        StartSpanOptions startOptions =  new StartSpanOptions(kind)
            .setAttribute(MESSAGING_SYSTEM_ATTRIBUTE_NAME, "eventhubs")
            .setAttribute(ENTITY_PATH_KEY, entityName)
            .setAttribute(HOST_NAME_KEY, fullyQualifiedName);

        if (operationName != null) {
            startOptions.setAttribute(MESSAGING_OPERATION_ATTRIBUTE_NAME, operationName.toString());
        }

        return startOptions;
    }

    public enum OperationName {
        PUBLISH("publish"),
        RECEIVE("receive"),
        PROCESS("process");

        private final String operationName;
        OperationName(String operationName) {
            this.operationName = operationName;
        }

        @Override
        public String toString() {
            return operationName;
        }
    }
}
