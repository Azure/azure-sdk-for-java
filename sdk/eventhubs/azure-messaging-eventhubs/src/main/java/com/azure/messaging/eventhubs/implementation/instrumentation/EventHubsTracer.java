// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.eventhubs.implementation.instrumentation;

import com.azure.core.amqp.exception.AmqpException;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.SpanKind;
import com.azure.core.util.tracing.Tracer;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.models.PartitionEvent;
import org.apache.qpid.proton.message.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;

import static com.azure.core.util.tracing.Tracer.DIAGNOSTIC_ID_KEY;
import static com.azure.core.util.tracing.Tracer.MESSAGE_ENQUEUED_TIME;
import static com.azure.core.util.tracing.Tracer.SPAN_CONTEXT_KEY;
import static com.azure.messaging.eventhubs.implementation.ClientConstants.AZ_NAMESPACE_VALUE;

public class EventHubsTracer {
    private static final AutoCloseable NOOP_AUTOCLOSEABLE = () -> {
    };

    public static final String REACTOR_PARENT_TRACE_CONTEXT_KEY = "otel-context-key";
    public static final String TRACEPARENT_KEY = "traceparent";
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

    public static Tracer getDefaultTracer() {
        Iterable<Tracer> tracers = ServiceLoader.load(Tracer.class);
        Iterator<Tracer> it = tracers.iterator();
        return it.hasNext() ? it.next() : null;
    }

    public boolean isEnabled() {
        return tracer != null;
    }

    public Context startSpan(String spanName, Context context, ProcessKind kind) {
        return tracer == null ? context : tracer.start(spanName, context, kind);
    }

    public <T> Mono<T> traceMono(Mono<T> publisher, String spanName) {
        if (tracer != null) {
            return publisher
                .doOnEach(signal -> {
                    if (signal.isOnComplete() || signal.isOnError()) {
                        Context span = signal.getContextView().getOrDefault(REACTOR_PARENT_TRACE_CONTEXT_KEY, Context.NONE);
                        endSpan(signal.getThrowable(), span, null);
                    }
                })
                .contextWrite(reactor.util.context.Context.of(REACTOR_PARENT_TRACE_CONTEXT_KEY,
                    tracer.start(spanName, setAttributes(Context.NONE), ProcessKind.SEND)));
        }

        return publisher;
    }

    public void endSpan(Throwable throwable, Context span, AutoCloseable scope) {
        if (tracer != null) {
            String errorCondition = "success";
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
        if (tracer == null || eventContext == null || eventContext.getData(SPAN_CONTEXT_KEY).isPresent()) {
            // if message has context (in case of retries), don't start a message span or add a new context
            return;
        }

        String traceparent = EventHubsTracer.getTraceparent(eventData.getProperties());
        if (traceparent != null) {
            // if message has context (in case of retries) or if user supplied it, don't start a message span or add a new context
            return;
        }

        // Starting the span makes the sampling decision (nothing is logged at this time)
        Context newMessageContext = setAttributes(eventContext);

        Context eventSpanContext = tracer.start("EventHubs.message", newMessageContext, ProcessKind.MESSAGE);
        Optional<Object> traceparentOpt = eventSpanContext.getData(DIAGNOSTIC_ID_KEY);

        Exception exception = null;
        if (traceparentOpt.isPresent() && canModifyApplicationProperties(eventData.getProperties())) {
            try {
                eventData.getProperties().put(DIAGNOSTIC_ID_KEY, traceparentOpt.get().toString());
                eventData.getProperties().put(TRACEPARENT_KEY, traceparentOpt.get().toString());
            } catch (RuntimeException ex) {
                // it might happen that unmodifiable map is something that we
                // didn't account for in canModifyApplicationProperties method
                // if it happens, let's not break everything, but log a warning
                LOGGER.logExceptionAsWarning(ex);
                exception = ex;
            }
        }
        tracer.end(null, exception, eventSpanContext);

        Optional<Object> spanContext = eventSpanContext.getData(SPAN_CONTEXT_KEY);
        if (spanContext.isPresent()) {
            eventData.addContext(SPAN_CONTEXT_KEY, spanContext.get());
        }
    }

    private boolean canModifyApplicationProperties(Map<String, Object> applicationProperties) {
        return applicationProperties.getClass().getSimpleName().equals("UnmodifiableMap");
    }

    public Context getBuilder(String spanName, Context context) {
        return tracer == null ? context : setAttributes(tracer.getSharedSpanBuilder(spanName, context));
    }

    public void addLink(Map<String, Object> applicationProperties, Instant enqueuedTime, Context spanBuilder, Context eventContext) {
        if (tracer != null) {
            Optional<Object> linkContext = eventContext.getData(SPAN_CONTEXT_KEY);
            if (!linkContext.isPresent()) {
                String traceparent = getTraceparent(applicationProperties);
                Context link = traceparent == null ? Context.NONE : tracer.extractContext(traceparent, Context.NONE);
                linkContext = link.getData(SPAN_CONTEXT_KEY);
            }

            if (enqueuedTime != null) {
                spanBuilder = spanBuilder.addData(MESSAGE_ENQUEUED_TIME, enqueuedTime.atOffset(ZoneOffset.UTC).toEpochSecond());
            }

            if (linkContext.isPresent()) {
                tracer.addLink(spanBuilder.addData(SPAN_CONTEXT_KEY, linkContext.get()));
            }
        }
    }

    public AutoCloseable makeSpanCurrent(Context context) {
        return tracer == null ? NOOP_AUTOCLOSEABLE : tracer.makeSpanCurrent(context);
    }

    public Context setParentAndAttributes(Message message, Instant enqueuedTime, Context context) {
        if (tracer != null) {
            if (enqueuedTime != null) {
                context = context.addData(MESSAGE_ENQUEUED_TIME, enqueuedTime.atOffset(ZoneOffset.UTC).toEpochSecond());
            }

            if (message.getApplicationProperties() != null) {
                context = getParent(message.getApplicationProperties().getValue(), context);
            }
            return setAttributes(context);
        }

        return context;
    }

    public Context startProcessSpan(String name, EventData event, Context parent) {
        if (tracer != null) {
            Context context = parent;
            Instant enqueuedTime = event.getEnqueuedTime();
            if (enqueuedTime != null) {
                context = parent.addData(MESSAGE_ENQUEUED_TIME, enqueuedTime.atOffset(ZoneOffset.UTC).toEpochSecond());
            }

            context = getParent(event.getProperties(), context);
            return tracer.start(name, setAttributes(context), ProcessKind.PROCESS);
        }

        return parent;
    }

    public Context startProcessSpan(String name, List<EventData> events, Context parent) {
        if (tracer != null) {
            Context context = parent.addData("span-kind", SpanKind.CONSUMER);
            Context spanBuilder = getBuilder(name, setAttributes(context));
            if (events != null) {
                for (EventData event : events) {
                    addLink(event.getProperties(), event.getEnqueuedTime(), spanBuilder, Context.NONE);
                }
            }

            return tracer.start(name, spanBuilder, ProcessKind.PROCESS);
        }

        return parent;
    }

    public Flux<PartitionEvent> reportSyncReceiveSpan(String name, Instant startTime, Flux<PartitionEvent> events, Context parent) {
        if (tracer != null) {
            List<PartitionEvent> eventsList = new ArrayList<>();
            return events.doOnEach(signal -> {
                if (signal.isOnNext()) {
                    eventsList.add(signal.get());
                } else if (signal.isOnComplete()) {
                    Context spanBuilder = getBuilder(name, setAttributes(parent.addData("span-start-time", startTime)));
                    for (PartitionEvent event : eventsList) {
                        addLink(event.getData().getProperties(), event.getData().getEnqueuedTime(), spanBuilder, Context.NONE);
                    }

                    // TODO (lmolkova) refactor tracing - ProcessKind.SEND is just a client span
                    Context span = tracer.start(name, spanBuilder, ProcessKind.SEND);
                    endSpan(null, span, null);
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

    private Context getParent(Map<String, Object> properties, Context context) {
        if (properties == null) {
            return context;
        }

        String traceparent = getTraceparent(properties);
        return traceparent == null ? context : tracer.extractContext(traceparent, context);
    }

    private Context setAttributes(Context context) {
        return context
            .addData(Tracer.ENTITY_PATH_KEY, entityName)
            .addData(Tracer.HOST_NAME_KEY, fullyQualifiedName)
            .addData(Tracer.AZ_TRACING_NAMESPACE_KEY, AZ_NAMESPACE_VALUE);
    }
}
