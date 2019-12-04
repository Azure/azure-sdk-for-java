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
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Builder;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Tracer;

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
    private static final Tracer TRACER = OpenTelemetry.getTracerFactory().get("Azure-OpenTelemetry");

    // standard attributes with AMQP request
    static final String COMPONENT = "component";
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

        Builder spanBuilder = getSpanBuilder(spanName, context);
        Span span = spanBuilder.startSpan();

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
        Builder spanBuilder;

        switch (processKind) {
            case SEND:
                spanBuilder = getSpanBuilder(spanName, context);
                span = spanBuilder.setSpanKind(Span.Kind.PRODUCER).startSpan();
                if (span.isRecording()) {
                    // If span is sampled in, add additional request attributes
                    addSpanRequestAttributes(span, context, spanName);
                }
                return context.addData(PARENT_SPAN_KEY, span);
            case MESSAGE:
                spanBuilder = getSpanBuilder(spanName, context);
                span = spanBuilder.startSpan();
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
        final Span span = getOrDefault(context, PARENT_SPAN_KEY, null, Span.class);
        if (span == null) {
            return;
        }

        if (span.isRecording()) {
            span.setStatus(HttpTraceUtil.parseResponseStatus(responseCode, throwable));
        }

        span.end();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String key, String value, Context context) {
        if (CoreUtils.isNullOrEmpty(value)) {
            logger.warning("Failed to set span attribute since value is null or empty.");
            return;
        }

        final Span span = getOrDefault(context, PARENT_SPAN_KEY, null, Span.class);
        if (span != null) {
            span.setAttribute(key, AttributeValue.stringAttributeValue(value));
        } else {
            logger.warning("Failed to find span to add attribute.");
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
        final Span span = getOrDefault(context, PARENT_SPAN_KEY, null, Span.class);
        if (span == null) {
            logger.warning("Failed to find span to end it.");
            return;
        }

        if (span.isRecording()) {
            span.setStatus(AmqpTraceUtil.parseStatusMessage(statusMessage, throwable));
        }

        span.end();
    }

    @Override
    public void addLink(Context context) {
        final Builder spanBuilder = getOrDefault(context, SPAN_BUILDER_KEY, null, Builder.class);
        if (spanBuilder == null) {
            logger.warning("Failed to find spanBuilder to link it.");
            return;
        }

        final SpanContext spanContext = getOrDefault(context, SPAN_CONTEXT_KEY, null, SpanContext.class);
        if (spanContext == null) {
            logger.warning("Failed to find span context to link it.");
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
            Builder spanBuilder = getSpanBuilder(spanName, context);
            span = spanBuilder.setSpanKind(Span.Kind.SERVER).startSpan();
        }
        return context.addData(PARENT_SPAN_KEY, span).addData("scope", TRACER.withSpan(span));
    }

    /**
     * Creates a {@link Builder} to create and start a new child {@link Span} with parent being the remote and
     * designated by the {@link SpanContext}.
     *
     * @param spanName The name of the returned Span.
     * @param spanContext The remote parent context of the returned Span.
     * @return A {@link Span} with parent being the remote {@link Span} designated by the {@link SpanContext}.
     */
    private static Span startSpanWithRemoteParent(String spanName, SpanContext spanContext) {
        Builder spanBuilder = TRACER.spanBuilder(spanName).setParent(spanContext);
        spanBuilder.setSpanKind(Span.Kind.SERVER);
        return spanBuilder.startSpan();
    }

    /**
     * Extracts the {@link SpanContext trace identifiers} and the {@link SpanContext} of the current tracing span as
     * text and returns in a {@link Context} object.
     *
     * @param span The current tracing span.
     * @return The {@link Context} containing the {@link SpanContext} and trace-parent of the
     * current span.
     */
    private static Context setContextData(Span span) {
        SpanContext spanContext = span.getContext();
        final String traceparent = AmqpPropagationFormatUtil.getDiagnosticId(spanContext);
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
        span.setAttribute(COMPONENT, AttributeValue.stringAttributeValue(parseComponentValue(spanName)));
        span.setAttribute(
            MESSAGE_BUS_DESTINATION,
            AttributeValue.stringAttributeValue(getOrDefault(context, ENTITY_PATH_KEY, "", String.class)));
        span.setAttribute(
            PEER_ENDPOINT,
            AttributeValue.stringAttributeValue(getOrDefault(context, HOST_NAME_KEY, "", String.class)));
    }

    /**
     * Extracts the component name from the given span name.
     *
     * @param spanName The spanName containing the component name i.e spanName = "Azure.eventhubs.send"
     * @return The component name contained in the context i.e "eventhubs"
     */
    private static String parseComponentValue(String spanName) {
        if (spanName != null && !spanName.isEmpty()) {
            int componentNameStartIndex = spanName.indexOf(".");
            int componentNameEndIndex = spanName.lastIndexOf(".");
            if (componentNameStartIndex != -1 && componentNameEndIndex != -1) {
                return spanName.substring(componentNameStartIndex + 1, componentNameEndIndex);
            }
        }
        return "";
    }

    /**
     * Returns a {@link Builder} to create and start a new child {@link Span} with parent being
     * the designated {@code Span}.
     *
     * @param spanName The name of the returned Span.
     * @param context The context containing the span and the span name.
     * @return A {@code Span.Builder} to create and start a new {@code Span}.
     */
    private Builder getSpanBuilder(String spanName, Context context) {
        Span parentSpan =  getOrDefault(context, PARENT_SPAN_KEY, null, Span.class);
        String spanNameKey =  getOrDefault(context, USER_SPAN_NAME_KEY, null, String.class);

        if (spanNameKey == null) {
            spanNameKey = spanName;
        }
        if (parentSpan == null) {
            parentSpan = TRACER.getCurrentSpan();
        }
        return TRACER.spanBuilder(spanNameKey).setParent(parentSpan);
    }

    /**
     * Returns the value of the specified key from the context.
     *
     * @param key The name of the attribute that needs to be extracted from the {@code Context}.
     * @param defaultValue the value to return in data not found.
     * @param clazz clazz the type of raw class to find data for.
     * @param context The context containing the specified key.
     *
     * @return The T type of raw class object
     */
    @SuppressWarnings("unchecked")
    private <T> T getOrDefault(Context context, String key, T defaultValue, Class<T> clazz) {
        final Optional<Object> optional = context.getData(key);
        final Object result = optional.filter(value -> clazz.isAssignableFrom(value.getClass())).orElseGet(() -> {
            logger.warning("Could not extract key '{}' of type '{}' from context.", key, clazz);
            return defaultValue;
        });

        return (T) result;
    }
}
