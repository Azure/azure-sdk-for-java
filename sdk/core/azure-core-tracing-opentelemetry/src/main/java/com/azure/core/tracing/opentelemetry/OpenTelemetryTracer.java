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
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Basic tracing implementation class for use with REST and AMQP Service Clients to create {@link Span} and in-process
 * context propagation. Singleton OpenTelemetry tracer capable of starting and exporting spans.
 *
 * <p>
 * This helper class supports W3C distributed tracing protocol and injects SpanContext into the outgoing HTTP and AMQP
 * requests.
 */
public class OpenTelemetryTracer implements com.azure.core.util.tracing.Tracer {
    private final Tracer tracer = GlobalOpenTelemetry.getTracer("Azure-OpenTelemetry");

    // standard attributes with AMQP request
    static final String AZ_NAMESPACE_KEY = "az.namespace";
    static final String MESSAGE_BUS_DESTINATION = "message_bus.destination";
    static final String PEER_ENDPOINT = "peer.address";

    private final ClientLogger logger = new ClientLogger(OpenTelemetryTracer.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Context start(String spanName, Context context) {
        Objects.requireNonNull(spanName, "'spanName' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");

        SpanBuilder spanBuilder = getSpanBuilder(spanName, context);
        Span span = spanBuilder.startSpan();
        if (span.isRecording()) {
            String tracingNamespace = getOrDefault(context, AZ_TRACING_NAMESPACE_KEY, null, String.class);
            if (tracingNamespace != null) {
                span.setAttribute(AZ_NAMESPACE_KEY, tracingNamespace);
            }
        }
        return context.addData(PARENT_SPAN_KEY, span);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context start(String spanName, Context context, ProcessKind processKind) {
        Objects.requireNonNull(spanName, "'spanName' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");
        Objects.requireNonNull(processKind, "'processKind' cannot be null.");

        Span span;
        SpanBuilder spanBuilder;

        switch (processKind) {
            case SEND:
                // use previously created span builder from the LINK process.
                spanBuilder = getOrDefault(context, SPAN_BUILDER_KEY, null, SpanBuilder.class);
                if (spanBuilder == null) {
                    return Context.NONE;
                }
                span = spanBuilder.setSpanKind(SpanKind.CLIENT).startSpan();
                if (span.isRecording()) {
                    // If span is sampled in, add additional request attributes
                    addSpanRequestAttributes(span, context, spanName);
                }
                return context.addData(PARENT_SPAN_KEY, span);
            case MESSAGE:
                spanBuilder = getSpanBuilder(spanName, context);
                span = spanBuilder.setSpanKind(SpanKind.PRODUCER).startSpan();
                if (span.isRecording()) {
                    // If span is sampled in, add additional request attributes
                    addSpanRequestAttributes(span, context, spanName);
                }
                // Add diagnostic Id and trace-headers to Context
                context = setContextData(span);
                return context.addData(PARENT_SPAN_KEY, span);
            case PROCESS:
                return startScopedSpan(spanName, context);
            default:
                return Context.NONE;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end(int responseCode, Throwable throwable, Context context) {
        Objects.requireNonNull(context, "'context' cannot be null.");
        Span span = getOrDefault(context, PARENT_SPAN_KEY, null, Span.class);
        if (span == null) {
            return;
        }

        if (span.isRecording()) {
            span = HttpTraceUtil.setSpanStatus(span, responseCode, throwable);
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

        final Span span = getOrDefault(context, PARENT_SPAN_KEY, null, Span.class);
        if (span != null) {
            span.setAttribute(key, value);
        } else {
            logger.verbose("Failed to find span to add attribute.");
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
        Span span = getOrDefault(context, PARENT_SPAN_KEY, null, Span.class);
        if (span == null) {
            logger.verbose("Failed to find span to end it.");
            return;
        }

        if (span.isRecording()) {
            span = AmqpTraceUtil.parseStatusMessage(span, statusMessage, throwable);
        }

        span.end();
    }

    @Override
    public void addLink(Context context) {
        final SpanBuilder spanBuilder = getOrDefault(context, SPAN_BUILDER_KEY, null, SpanBuilder.class);
        if (spanBuilder == null) {
            logger.verbose("Failed to find spanBuilder to link it.");
            return;
        }

        final SpanContext spanContext = getOrDefault(context, SPAN_CONTEXT_KEY, null, SpanContext.class);
        if (spanContext == null) {
            logger.verbose("Failed to find span context to link it.");
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

    @Override
    public Context getSharedSpanBuilder(String spanName, Context context) {
        return context.addData(SPAN_BUILDER_KEY, getSpanBuilder(spanName, context));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("deprecation")
    public void addEvent(String eventName, Map<String, Object> traceEventAttributes, OffsetDateTime timestamp) {
        addEvent(eventName, traceEventAttributes, timestamp, new Context(PARENT_SPAN_KEY, Span.current()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addEvent(String eventName, Map<String, Object> traceEventAttributes, OffsetDateTime timestamp,
                         Context context) {
        Objects.requireNonNull(eventName, "'eventName' cannot be null.");

        Span currentSpan = getOrDefault(context, PARENT_SPAN_KEY, null, Span.class);
        if (currentSpan == null) {
            logger.verbose("Failed to find a starting span to associate the {} with.", eventName);
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
     * Maps span/event properties to OpenTelemetry attributes.
     *
     * @param attributes the attributes provided by the client SDK's.
     * @return the OpenTelemetry typed {@Link Attributes}.
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
            }
        });
        return attributesBuilder.build();
    }

    /**
     * Starts a new child {@link Span} with parent being the remote and uses the {@link Span} is in the current Context,
     * to return an object that represents that scope.
     * <p>The scope is exited when the returned object is closed.</p>
     *
     * @param spanName The name of the returned Span.
     * @param context The {@link Context} containing the {@link SpanContext}.
     * @return The returned {@link Span} and the scope in a {@link Context} object.
     */
    private Context startScopedSpan(String spanName, Context context) {
        Objects.requireNonNull(context, "'context' cannot be null.");
        Span span;
        SpanContext spanContext = getOrDefault(context, SPAN_CONTEXT_KEY, null, SpanContext.class);
        if (spanContext != null) {
            span = startSpanWithRemoteParent(spanName, spanContext);
        } else {
            SpanBuilder spanBuilder = getSpanBuilder(spanName, context);
            span = spanBuilder.setSpanKind(SpanKind.CONSUMER).startSpan();
        }
        if (span.isRecording()) {
            // If span is sampled in, add additional request attributes
            addSpanRequestAttributes(span, context, spanName);
        }
        return context.addData(PARENT_SPAN_KEY, span).addData("scope", span.makeCurrent());
    }

    /**
     * Creates a {@link SpanBuilder} to create and start a new child {@link Span} with parent being the remote and
     * designated by the {@link SpanContext}.
     *
     * @param spanName The name of the returned Span.
     * @param spanContext The remote parent context of the returned Span.
     * @return A {@link Span} with parent being the remote {@link Span} designated by the {@link SpanContext}.
     */
    private Span startSpanWithRemoteParent(String spanName, SpanContext spanContext) {
        SpanBuilder spanBuilder = tracer.spanBuilder(spanName)
            .setParent(io.opentelemetry.context.Context.root().with(Span.wrap(spanContext)));

        spanBuilder.setSpanKind(SpanKind.CONSUMER);
        return spanBuilder.startSpan();
    }

    /**
     * Extracts the {@link SpanContext trace identifiers} and the {@link SpanContext} of the current tracing span as
     * text and returns in a {@link Context} object.
     *
     * @param span The current tracing span.
     * @return The {@link Context} containing the {@link SpanContext} and trace-parent of the current span.
     */
    private static Context setContextData(Span span) {
        SpanContext spanContext = span.getSpanContext();
        final String traceparent = AmqpPropagationFormatUtil.getDiagnosticId(spanContext);
        if (traceparent == null) {
            return Context.NONE;
        }
        return new Context(DIAGNOSTIC_ID_KEY, traceparent).addData(SPAN_CONTEXT_KEY, spanContext);
    }

    /**
     * Extracts request attributes from the given {@code context} and adds it to the started span.
     *
     * @param span The span to which request attributes are to be added.
     * @param context The context containing the request attributes.
     * @param spanName The name of the returned Span containing the component value.
     */
    private void addSpanRequestAttributes(Span span, Context context, String spanName) {
        Objects.requireNonNull(span, "'span' cannot be null.");
        String entityPath = getOrDefault(context, ENTITY_PATH_KEY, null, String.class);
        if (entityPath != null) {
            span.setAttribute(MESSAGE_BUS_DESTINATION, entityPath);
        }
        String hostName = getOrDefault(context, HOST_NAME_KEY, null, String.class);
        if (hostName != null) {
            span.setAttribute(PEER_ENDPOINT, hostName);
        }
        Long messageEnqueuedTime = getOrDefault(context, MESSAGE_ENQUEUED_TIME, null, Long.class);
        if (messageEnqueuedTime != null) {
            span.setAttribute(MESSAGE_ENQUEUED_TIME, messageEnqueuedTime);
        }
        String tracingNamespace = getOrDefault(context, AZ_TRACING_NAMESPACE_KEY, null, String.class);
        if (tracingNamespace != null) {
            span.setAttribute(AZ_NAMESPACE_KEY, tracingNamespace);
        }
    }

    /**
     * Returns a {@link SpanBuilder} to create and start a new child {@link Span} with parent
     * being the designated {@code Span}.
     *
     * @param spanName The name of the returned Span.
     * @param context The context containing the span and the span name.
     * @return A {@code Span.SpanBuilder} to create and start a new {@code Span}.
     */
    private SpanBuilder getSpanBuilder(String spanName, Context context) {
        Span parentSpan = getOrDefault(context, PARENT_SPAN_KEY, null, Span.class);
        String spanNameKey = getOrDefault(context, USER_SPAN_NAME_KEY, null, String.class);

        if (spanNameKey == null) {
            spanNameKey = spanName;
        }
        if (parentSpan == null) {
            parentSpan = Span.current();
        }
        return tracer.spanBuilder(spanNameKey)
            .setParent(io.opentelemetry.context.Context.current().with(parentSpan));
    }

    /**
     * Returns the value of the specified key from the context.
     *
     * @param key The name of the attribute that needs to be extracted from the {@code Context}.
     * @param defaultValue the value to return in data not found.
     * @param clazz clazz the type of raw class to find data for.
     * @param context The context containing the specified key.
     * @return The T type of raw class object
     */
    @SuppressWarnings("unchecked")
    private <T> T getOrDefault(Context context, String key, T defaultValue, Class<T> clazz) {
        final Optional<Object> optional = context.getData(key);
        final Object result = optional.filter(value -> clazz.isAssignableFrom(value.getClass())).orElseGet(() -> {
            logger.verbose("Could not extract key '{}' of type '{}' from context.", key, clazz);
            return defaultValue;
        });

        return (T) result;
    }
}
