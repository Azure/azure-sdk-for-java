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
        final Span span = getSpan(context);
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

        final Span span = getSpan(context);
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
        final Span span = getSpan(context);
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
        //Noop - deprecated
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context extractContext(String diagnosticId, Context context) {
        return AmqpPropagationFormatUtil.extractContext(diagnosticId, context);
    }

    /**
     * Starts a new child {@link Span} with parent being the remote and uses the {@link
     * Span} is in the current Context, to return an object that represents that scope.
     * <p>The scope is exited when the returned object is closed.</p>
     *
     * @param spanName The name of the returned Span.
     * @param context The {@link com.azure.core.util.Context} containing the {@link
     * SpanContext}.
     * @return The returned {@link Span} and the scope in a {@link com.azure.core.util.Context}
     * object.
     */
    private Context startScopedSpan(String spanName, Context context) {
        Objects.requireNonNull(context, "'context' cannot be null.");
        Span span;
        SpanContext spanContext = getSpanContext(context);
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
     * @return A {@link Span} with parent being the remote {@link Span}
     * designated by the {@link SpanContext}.
     */
    private static Span startSpanWithRemoteParent(String spanName, SpanContext spanContext) {
        Builder spanBuilder = TRACER.spanBuilder(spanName).setParent(spanContext);
        spanBuilder.setSpanKind(Span.Kind.SERVER);
        return spanBuilder.startSpan();
    }

    /**
     * Extracts the {@link SpanContext trace identifiers} and the {@link
     * SpanContext} of the current tracing span as text and returns in a {@link
     * com.azure.core.util.Context} object.
     *
     * @param span The current tracing span.
     * @return The {@link com.azure.core.util.Context} containing the {@link SpanContext} and
     * trace-parent of the current span.
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
            AttributeValue.stringAttributeValue(getRequestKeyAttribute(context, ENTITY_PATH_KEY)));
        span.setAttribute(
            PEER_ENDPOINT,
            AttributeValue.stringAttributeValue(getRequestKeyAttribute(context, HOST_NAME_KEY)));
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
     * Extracts request attributes from the given {@code context} and provided key.
     *
     * @param context The context containing the specified attribute key.
     * @param key The name of the attribute that needs to be extracted from the {@code Context}.
     * @return The value for the provided key contained in the context.
     */
    private String getRequestKeyAttribute(Context context, String key) {
        final Object value = getOptionalObject(context, key);
        if (!(value instanceof String)) {
            logger.warning("Could not extract {}. Data in context for key {} is not of type String.", key);
            return "";
        }

        return value.toString();
    }

    /**
     * Extracts a {@link Span} from the given {@code context}.
     *
     * @param context The context containing the span.
     * @return The {@link Span} contained in the context, and {@code null} if it does not.
     */
    private Span getSpan(Context context) {
        final Object value = getOptionalObject(context, PARENT_SPAN_KEY);
        if (!(value instanceof Span)) {
            logger.warning("Could not extract span. Data in context for key {} is not of type Span.", PARENT_SPAN_KEY);
            return null;
        }

        return (Span) value;
    }

    /**
     * Extracts the span name from the given {@code context}.
     *
     * @param context The context containing the span name.
     * @return The span name contained in the context, and {@code null} if it does not.
     */
    private String getSpanName(Context context) {
        final Object value = getOptionalObject(context, USER_SPAN_NAME_KEY);
        if (!(value instanceof String)) {
            logger.warning("Could not extract span name. Data in context for key {} is not of type String.",
                USER_SPAN_NAME_KEY);
            return null;
        }

        return value.toString();
    }

    /**
     * Extracts a {@link SpanContext} from the given {@code context}.
     *
     * @param context The context containing the span context.
     * @return The {@link SpanContext} contained in the context, and {@code null} if it does not.
     */
    private SpanContext getSpanContext(Context context) {
        final Object value = getOptionalObject(context, SPAN_CONTEXT_KEY);
        if (!(value instanceof SpanContext)) {
            logger.warning("Could not extract span context. Data is in context for key {} not of type SpanContext.",
                SPAN_CONTEXT_KEY);
            return null;
        }

        return (SpanContext) value;
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
        Span parentSpan = getSpan(context);
        String spanNameKey = getSpanName(context);

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
     * @param context The context containing the specified key.
     * @param key The name of the attribute that needs to be extracted from the {@code Context}.
     * @return The value for the provided key contained in the context.
     */
    private Object getOptionalObject(Context context, String key) {
        final Optional<Object> optionalObject = context.getData(key);
        if (!optionalObject.isPresent()) {
            logger.warning("Failed to find {} in the context.", key);
            return null;
        }
        return optionalObject.get();
    }
}
