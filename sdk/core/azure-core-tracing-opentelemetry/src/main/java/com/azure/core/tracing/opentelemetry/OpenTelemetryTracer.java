// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.tracing.opentelemetry.implementation.AmqpPropagationFormatUtil;
import com.azure.core.tracing.opentelemetry.implementation.AmqpTraceUtil;
import com.azure.core.tracing.opentelemetry.implementation.HttpTraceUtil;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.util.tracing.StartSpanOptions;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Basic tracing implementation class for use with REST and AMQP Service Clients to create {@link Span} and in-process
 * context propagation. Singleton OpenTelemetry tracer capable of starting and exporting spans.
 * <p>
 * This helper class supports W3C distributed tracing protocol and injects SpanContext into the outgoing HTTP and AMQP
 * requests.
 */
public class OpenTelemetryTracer implements com.azure.core.util.tracing.Tracer {
    private final Tracer tracer;

    /**
     * Creates new {@link OpenTelemetryTracer} using default global tracer -
     * {@link GlobalOpenTelemetry#getTracer(String)}
     *
     */
    public OpenTelemetryTracer() {
        this(GlobalOpenTelemetry.getTracer("Azure-OpenTelemetry"));
    }

    /**
     * Creates new {@link OpenTelemetryTracer} that wraps {@link io.opentelemetry.api.trace.Tracer}.
     * Use it for tests.
     *
     * @param tracer {@link io.opentelemetry.api.trace.Tracer} instance.
     */
    OpenTelemetryTracer(Tracer tracer) {
        this.tracer = tracer;
    }

    static final String AZ_NAMESPACE_KEY = "az.namespace";

    // standard attributes with AMQP request
    static final String MESSAGE_BUS_DESTINATION = "message_bus.destination";
    static final String PEER_ENDPOINT = "peer.address";

    private final ClientLogger logger = new ClientLogger(OpenTelemetryTracer.class);
    private static final AutoCloseable NOOP_CLOSEABLE = () -> { };

    /**
     * {@inheritDoc}
     */
    @Override
    public Context start(String spanName, Context context) {
        Objects.requireNonNull(spanName, "'spanName' cannot be null.");
        SpanBuilder spanBuilder = createSpanBuilder(spanName, null, SpanKind.INTERNAL, null, context);

        return startSpanInternal(spanBuilder, null, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context start(String spanName, StartSpanOptions options, Context context) {
        Objects.requireNonNull(options, "'options' cannot be null.");
        SpanBuilder spanBuilder = createSpanBuilder(spanName, null, convertToOtelKind(options.getSpanKind()),
            options.getAttributes(), context);

        return startSpanInternal(spanBuilder, null, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context start(String spanName, Context context, ProcessKind processKind) {
        Objects.requireNonNull(spanName, "'spanName' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");
        Objects.requireNonNull(processKind, "'processKind' cannot be null.");

        SpanBuilder spanBuilder;
        switch (processKind) {
            case SEND:
                // use previously created span builder from the LINK process.
                spanBuilder = getOrNull(context, SPAN_BUILDER_KEY, SpanBuilder.class);
                if (spanBuilder == null) {
                    return context;
                }
                return startSpanInternal(spanBuilder, this::addMessagingAttributes, context);
            case MESSAGE:
                spanBuilder = createSpanBuilder(spanName, null, SpanKind.PRODUCER, null, context);
                context = startSpanInternal(spanBuilder, this::addMessagingAttributes, context);
                return setDiagnosticId(context);
            case PROCESS:
                SpanContext remoteParentContext = getOrNull(context, SPAN_CONTEXT_KEY, SpanContext.class);
                spanBuilder = createSpanBuilder(spanName, remoteParentContext, SpanKind.CONSUMER, null, context);
                context = startSpanInternal(spanBuilder, this::addMessagingAttributes, context);

                // TODO (limolkova) we should do this in the EventHub/ServiceBus SDK instead to make sure scope is
                //  closed in the same thread where it was started to prevent leaking the context.
                return context.addData(SCOPE_KEY, makeSpanCurrent(context));
            default:
                return context;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end(int responseCode, Throwable throwable, Context context) {
        Objects.requireNonNull(context, "'context' cannot be null.");
        final Span span = getSpanOrNull(context);
        if (span == null) {
            return;
        }

        if (span.isRecording()) {
            HttpTraceUtil.setSpanStatus(span, responseCode, throwable);
        }
        span.end();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String key, String value, Context context) {
        Objects.requireNonNull(context, "'context' cannot be null");
        if (CoreUtils.isNullOrEmpty(value)) {
            logger.verbose("Failed to set span attribute since value is null or empty.");
            return;
        }

        final Span span = getSpanOrNull(context);
        if (span == null) {
            return;
        }

        if (span.isRecording()) {
            span.setAttribute(key, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context setSpanName(String spanName, Context context) {
        return context.addData(USER_SPAN_NAME_KEY, spanName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end(String statusMessage, Throwable throwable, Context context) {
        Span span = getSpanOrNull(context);

        if (span == null) {
            return;
        }

        if (span.isRecording()) {
            span = AmqpTraceUtil.parseStatusMessage(span, statusMessage, throwable);
        }

        span.end();

        // TODO (limolkova) remove once ServiceBus/EventHub start making span current explicitly.
        endScope(context);
    }

    @Override
    public void addLink(Context context) {
        final SpanBuilder spanBuilder = getOrNull(context, SPAN_BUILDER_KEY, SpanBuilder.class);
        if (spanBuilder == null) {
            return;
        }

        final SpanContext spanContext = getOrNull(context, SPAN_CONTEXT_KEY, SpanContext.class);
        if (spanContext == null) {
            return;
        }
        spanBuilder.addLink(spanContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context extractContext(String diagnosticId, Context context) {
        return AmqpPropagationFormatUtil.extractContext(diagnosticId, context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context getSharedSpanBuilder(String spanName, Context context) {
        // this is used to create messaging send spanBuilder, and it's a CLIENT span
        return context.addData(SPAN_BUILDER_KEY, createSpanBuilder(spanName, null, SpanKind.CLIENT, null, context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AutoCloseable makeSpanCurrent(Context context) {
        io.opentelemetry.context.Context traceContext = getTraceContextOrDefault(context, null);
        if (traceContext == null) {
            logger.verbose("There is no OpenTelemetry Context on the context, cannot make it current");
            return NOOP_CLOSEABLE;
        }
        return traceContext.makeCurrent();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("deprecation")
    public void addEvent(String eventName, Map<String, Object> traceEventAttributes, OffsetDateTime timestamp) {
        addEvent(eventName, traceEventAttributes, timestamp, new Context(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.current()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEvent(String eventName, Map<String, Object> traceEventAttributes, OffsetDateTime timestamp, Context context) {
        Objects.requireNonNull(eventName, "'eventName' cannot be null.");
        Span currentSpan = getSpanOrNull(context);

        if (currentSpan == null) {
            logger.verbose("There is no OpenTelemetry Span or Context on the context, cannot add event");
            return;
        }

        if (timestamp == null) {
            currentSpan.addEvent(
                eventName,
                traceEventAttributes == null ? Attributes.empty() : convertToOtelAttributes(traceEventAttributes));
        } else {
            currentSpan.addEvent(
                eventName,
                traceEventAttributes == null ? Attributes.empty() : convertToOtelAttributes(traceEventAttributes),
                timestamp.toInstant()
            );
        }
    }

    /**
     * Returns a {@link SpanBuilder} to create and start a new child {@link Span} with parent being the designated
     * {@link Span}.
     *
     * @param spanBuilder SpanBuilder for the span. Must be created before calling this method
     * @param setAttributes Callback to populate attributes for the span.
     * @return A {@link Context} with created {@link Span}.
     */
    private Context startSpanInternal(SpanBuilder spanBuilder,
        java.util.function.BiConsumer<Span, Context> setAttributes,
        Context context) {
        Objects.requireNonNull(spanBuilder, "'spanBuilder' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");

        Span span = spanBuilder.startSpan();
        if (span.isRecording()) {
            // If span is sampled in, add additional attributes

            String tracingNamespace = getOrNull(context, AZ_TRACING_NAMESPACE_KEY, String.class);
            if (tracingNamespace != null) {
                span.setAttribute(AZ_NAMESPACE_KEY, tracingNamespace);
            }

            if (setAttributes != null) {
                setAttributes.accept(span, context);
            }
        }

        return context.addData(PARENT_TRACE_CONTEXT_KEY, getTraceContextOrDefault(context, io.opentelemetry.context.Context.current()).with(span));
    }

    /**
     * Returns a {@link SpanBuilder} to create and start a new child {@link Span} with parent being the designated
     * {@link Span}.
     *
     * @param spanName The name of the returned Span.
     * @param remoteParentContext Remote parent context if any, or {@code null} otherwise.
     * @param spanKind Kind of the span to create.
     * @param beforeSaplingAttributes Optional attributes available when span starts and important for sampling.
     * @param context The context containing the span and the span name.
     * @return A {@link SpanBuilder} to create and start a new {@link Span}.
     */
    @SuppressWarnings("unchecked")
    private SpanBuilder createSpanBuilder(String spanName,
        SpanContext remoteParentContext,
        SpanKind spanKind,
        Map<String, Object> beforeSaplingAttributes,
        Context context) {
        String spanNameKey = getOrNull(context, USER_SPAN_NAME_KEY, String.class);

        if (spanNameKey == null) {
            spanNameKey = spanName;
        }

        SpanBuilder spanBuilder = tracer.spanBuilder(spanNameKey)
            .setSpanKind(spanKind);

        io.opentelemetry.context.Context parentContext =  getTraceContextOrDefault(context, io.opentelemetry.context.Context.current());
        // if remote parent is provided, it has higher priority
        if (remoteParentContext != null) {
            spanBuilder.setParent(parentContext.with(Span.wrap(remoteParentContext)));
        } else {
            spanBuilder.setParent(parentContext);
        }

        // if some attributes are provided, set them
        if (!CoreUtils.isNullOrEmpty(beforeSaplingAttributes)) {
            Attributes otelAttributes = convertToOtelAttributes(beforeSaplingAttributes);
            otelAttributes.forEach(
                (key, value) -> spanBuilder.setAttribute((AttributeKey<Object>) key, value));
        }

        return spanBuilder;
    }

    /**
     * Ends current scope on the context.
     *
     * @param context Context instance with the scope to end.
     */
    private void endScope(Context context) {
        Scope scope = getOrNull(context, SCOPE_KEY, Scope.class);
        if (scope != null) {
            scope.close();
        }
    }

    /*
     * Converts our SpanKind to OpenTelemetry SpanKind.
     */
    private SpanKind convertToOtelKind(com.azure.core.util.tracing.SpanKind kind) {
        switch (kind) {
            case CLIENT:
                return SpanKind.CLIENT;

            case SERVER:
                return SpanKind.SERVER;

            case CONSUMER:
                return SpanKind.CONSUMER;

            case PRODUCER:
                return SpanKind.PRODUCER;

            default:
                return SpanKind.INTERNAL;
        }
    }

    /**
     * Maps span/event properties to OpenTelemetry attributes.
     *
     * @param attributes the attributes provided by the client SDK's.
     * @return the OpenTelemetry typed {@link Attributes}.
     */
    private Attributes convertToOtelAttributes(Map<String, Object> attributes) {
        AttributesBuilder attributesBuilder = Attributes.builder();
        attributes.forEach((key, value) -> {
            if (value instanceof Boolean) {
                attributesBuilder.put(key, (boolean) value);
            } else if (value instanceof String) {
                attributesBuilder.put(key, String.valueOf(value));
            } else if (value instanceof Double) {
                attributesBuilder.put(key, (Double) value);
            } else if (value instanceof Long) {
                attributesBuilder.put(key, (Long) value);
            } else if (value instanceof String[]) {
                attributesBuilder.put(key, (String[]) value);
            } else if (value instanceof long[]) {
                attributesBuilder.put(key, (long[]) value);
            } else if (value instanceof double[]) {
                attributesBuilder.put(key, (double[]) value);
            } else if (value instanceof boolean[]) {
                attributesBuilder.put(key, (boolean[]) value);
            } else {
                logger.warning("Could not populate attribute with key '{}', type is not supported.");
            }
        });
        return attributesBuilder.build();
    }

    /**
     * Extracts the {@link SpanContext trace identifiers} and the {@link SpanContext} of the current tracing span as
     * text and returns in a {@link Context} object.
     *
     * @param context The context with current tracing span describing unique message context.
     * @return The {@link Context} containing the {@link SpanContext} and trace-parent of the current span.
     */
    private Context setDiagnosticId(Context context) {
        Span span = getSpanOrNull(context);
        if (span == null) {
            return context;
        }

        SpanContext spanContext = span.getSpanContext();
        if (spanContext.isValid()) {
            final String traceparent = AmqpPropagationFormatUtil.getDiagnosticId(spanContext);
            if (traceparent == null) {
                return context;
            }
            return context.addData(DIAGNOSTIC_ID_KEY, traceparent).addData(SPAN_CONTEXT_KEY, spanContext);
        }

        return context;
    }

    /**
     * Extracts request attributes from the given {@link Context} and adds it to the started span.
     *
     * @param span The span to which request attributes are to be added.
     * @param context The context containing the request attributes.
     */
    private void addMessagingAttributes(Span span, Context context) {
        Objects.requireNonNull(span, "'span' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");

        String entityPath = getOrNull(context, ENTITY_PATH_KEY, String.class);
        if (entityPath != null) {
            span.setAttribute(MESSAGE_BUS_DESTINATION, entityPath);
        }
        String hostName = getOrNull(context, HOST_NAME_KEY, String.class);
        if (hostName != null) {
            span.setAttribute(PEER_ENDPOINT, hostName);
        }
        Long messageEnqueuedTime = getOrNull(context, MESSAGE_ENQUEUED_TIME, Long.class);
        if (messageEnqueuedTime != null) {
            span.setAttribute(MESSAGE_ENQUEUED_TIME, messageEnqueuedTime);
        }
    }

    /**
     * Returns the value of the specified key from the context.
     *
     * @param key The name of the attribute that needs to be extracted from the {@link Context}.
     * @param clazz clazz the type of raw class to find data for.
     * @param context The context containing the specified key.
     * @return The T type of raw class object
     */
    @SuppressWarnings("unchecked")
    private <T> T getOrNull(Context context, String key, Class<T> clazz) {
        final Optional<Object> optional = context.getData(key);
        final Object result = optional.filter(value -> clazz.isAssignableFrom(value.getClass())).orElseGet(() -> {
            logger.verbose("Could not extract key '{}' of type '{}' from context.", key, clazz);
            return null;
        });

        return (T) result;
    }


    /**
     * Returns OpenTelemetry trace context from given com.azure.core.Context under PARENT_TRACE_CONTEXT_KEY
     * or PARENT_SPAN_KEY (for backward-compatibility) or default value.
     */
    @SuppressWarnings("deprecation")
    private io.opentelemetry.context.Context getTraceContextOrDefault(Context azContext, io.opentelemetry.context.Context defaultContext) {
        io.opentelemetry.context.Context traceContext = getOrNull(azContext,
            PARENT_TRACE_CONTEXT_KEY,
            io.opentelemetry.context.Context.class);

        if (traceContext == null) {
            Span parentSpan = getOrNull(azContext,
                PARENT_SPAN_KEY,
                Span.class);

            if (parentSpan != null) {
                traceContext = io.opentelemetry.context.Context.current().with(parentSpan);
            }
        }
        return traceContext == null ? defaultContext : traceContext;
    }

    /**
     * Returns OpenTelemetry trace context from given com.azure.core.Context under PARENT_TRACE_CONTEXT_KEY
     * or PARENT_SPAN_KEY (for backward-compatibility)
     */
    @SuppressWarnings("deprecation")
    private Span getSpanOrNull(Context azContext) {
        io.opentelemetry.context.Context traceContext = getOrNull(azContext,
            PARENT_TRACE_CONTEXT_KEY,
            io.opentelemetry.context.Context.class);

        if (traceContext == null) {
            Span parentSpan = getOrNull(azContext,
                PARENT_SPAN_KEY,
                Span.class);

            if (parentSpan != null) {
                return parentSpan;
            }
        }

        return traceContext == null ? null : Span.fromContext(traceContext);
    }
}
