// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opencensus;

import com.azure.core.util.tracing.ProcessKind;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.tracing.opencensus.implementation.AmqpPropagationFormatUtil;
import com.azure.core.tracing.opencensus.implementation.AmqpTraceUtil;
import com.azure.core.tracing.opencensus.implementation.HttpTraceUtil;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;
import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;

import java.util.Objects;
import java.util.Optional;

import static io.opencensus.trace.Link.Type.PARENT_LINKED_SPAN;

/**
 * Basic tracing implementation class for use with REST and AMQP Service Clients to create {@link Span} and in-process
 * context propagation. Singleton OpenCensus tracer capable of starting and exporting spans.
 *
 * <p>
 * This helper class supports W3C distributed tracing protocol and injects SpanContext into the outgoing HTTP
 * and AMQP requests.
 */
public class OpenCensusTracer implements com.azure.core.util.tracing.Tracer {
    private static final Tracer TRACER = Tracing.getTracer();
    /**
     * Key for {@link Context} which indicates that the context contains OpenCensus span data. This span will be used
     * as the parent span for all spans the SDK creates.
     * <p>
     * If no span data is listed when the SDK creates its first span, this span key will be used as the parent span.
     */
    public static final String OPENCENSUS_SPAN_KEY = "opencensus-span";

    /**
     * Key for {@link Context} which indicates that the context contains the name for the OpenCensus spans that are
     * created.
     * <p>
     * If no span name is listed when the span is created it will default to using the calling method's name.
     */
    public static final String OPENCENSUS_SPAN_NAME_KEY = "opencensus-span-name";

    /**
     * Key for {@link Context} which indicates that the context contains an entity path.
     */
    public static final String ENTITY_PATH = "entity-path";

    /**
     * Key for {@link Context} which indicates that the context contains the hostname.
     */
    public static final String HOST_NAME = "hostname";

    /**
     * Key for {@link Context} which indicates that the context contains a message span context.
     */
    public static final String SPAN_CONTEXT = "span-context";

    /**
     * Key for {@link Context} which indicates that the context contains a "Diagnostic Id" for the service call.
     */
    public static final String DIAGNOSTIC_ID_KEY = "diagnostic-id";

    // standard attributes with AMQP request
    private static final String COMPONENT = "component";
    private static final String MESSAGE_BUS_DESTINATION = "message_bus.destination";
    private static final String PEER_ENDPOINT = "peer.address";

    private final ClientLogger logger = new ClientLogger(OpenCensusTracer.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Context start(String methodName, Context context) {
        Objects.requireNonNull(methodName, "'methodName' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");

        SpanBuilder spanBuilder = getSpanBuilder(methodName, context);
        Span span = spanBuilder.startSpan();

        return context.addData(OPENCENSUS_SPAN_KEY, span);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context start(String spanName, Context context, ProcessKind processKind) {
        Objects.requireNonNull(spanName, "'methodName' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");
        Objects.requireNonNull(processKind, "'processKind' cannot be null.");

        Span span;
        SpanBuilder spanBuilder;

        switch (processKind) {
            case SEND:
                spanBuilder = getSpanBuilder(spanName, context);
                span = spanBuilder.setSpanKind(Span.Kind.CLIENT).startSpan();
                if (span.getOptions().contains(Span.Options.RECORD_EVENTS)) {
                    // If span is sampled in, add additional request attributes
                    addSpanRequestAttributes(span, context, spanName);
                }
                return context.addData(OPENCENSUS_SPAN_KEY, span);
            case RECEIVE:
                spanBuilder = getSpanBuilder(spanName, context);
                span = spanBuilder.startSpan();
                // Add diagnostic Id and traceheaders to Context
                context = setContextData(span);
                return context.addData(OPENCENSUS_SPAN_KEY, span);
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

        if (span.getOptions().contains(Options.RECORD_EVENTS)) {
            span.setStatus(HttpTraceUtil.parseResponseStatus(responseCode, throwable));
        }

        span.end();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setAttribute(String key, String value, Context context) {
        if (ImplUtils.isNullOrEmpty(value)) {
            logger.info("Failed to set span attribute since value is null or empty.");
            return;
        }

        final Span span = getSpan(context);
        if (span != null) {
            span.putAttribute(key, AttributeValue.stringAttributeValue(value));
        } else {
            logger.warning("Failed to find span to add attribute.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Context setSpanName(String spanName, Context context) {
        return context.addData(OPENCENSUS_SPAN_NAME_KEY, spanName);
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

        if (span.getOptions().contains(Options.RECORD_EVENTS)) {
            span.setStatus(AmqpTraceUtil.parseStatusMessage(statusMessage, throwable));
        }

        span.end();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addLink(Context context) {
        final Span span = getSpan(context);
        if (span == null) {
            logger.warning("Failed to find span to link it.");
            return;
        }

        final SpanContext spanContext = getSpanContext(context);
        if (spanContext == null) {
            logger.warning("Failed to find span context to link it.");
            return;
        }
        // TODO: Needs to be updated with Open Telemetry support to addLink using Span Context before span is started
        // and no link type is needed.
        span.addLink(Link.fromSpanContext(spanContext, PARENT_LINKED_SPAN));
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
        Objects.requireNonNull(spanName, "'spanName' cannot be null.");
        Objects.requireNonNull(context, "'context' cannot be null.");
        Span span;
        SpanContext spanContext = getSpanContext(context);
        if (spanContext != null) {
            span = startSpanWithRemoteParent(spanName, spanContext);
        } else {
            SpanBuilder spanBuilder = getSpanBuilder(spanName, context);
            span = spanBuilder.setSpanKind(Span.Kind.SERVER).startSpan();
        }
        return context.addData(OPENCENSUS_SPAN_KEY, span).addData("scope", TRACER.withSpan(span));
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
        SpanBuilder spanBuilder = TRACER.spanBuilderWithRemoteParent(spanName, spanContext);
        spanBuilder.setSpanKind(Span.Kind.SERVER);
        return spanBuilder.startSpan();
    }

    /**
     * Extracts the {@link SpanContext trace identifiers} and the {@link SpanContext} of the current tracing span as
     * text and returns in a {@link Context} object.
     *
     * @param span The current tracing span.
     * @return The {@link Context} containing the {@link SpanContext} and traceparent of the current span.
     */
    private Context setContextData(Span span) {
        SpanContext spanContext = span.getContext();
        final String traceparent = AmqpPropagationFormatUtil.getDiagnosticId(spanContext);
        return new Context(DIAGNOSTIC_ID_KEY, traceparent).addData(SPAN_CONTEXT, spanContext);
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

        span.putAttribute(COMPONENT, AttributeValue.stringAttributeValue(parseComponentValue(spanName)));
        span.putAttribute(
            MESSAGE_BUS_DESTINATION,
            AttributeValue.stringAttributeValue(getRequestKeyAttribute(context, ENTITY_PATH)));
        span.putAttribute(
            PEER_ENDPOINT,
            AttributeValue.stringAttributeValue(getRequestKeyAttribute(context, HOST_NAME)));
    }

    /**
     * Extracts component name from the given span name.
     *
     * @param spanName The spanName containing the component name.
     * @return The component name contained in the context.
     */
    private static String parseComponentValue(String spanName) {
        return spanName.substring(spanName.indexOf(".") + 1, spanName.lastIndexOf("."));
    }

    /**
     * Extracts request attributes from the given {@code context} and provided key.
     *
     * @param context The context containing the entity path.
     * @param key The name of the attribute that needs to be extracted from the {@code Context}.
     * @return The value for the provided key contained in the context.
     */
    private String getRequestKeyAttribute(Context context, String key) {
        final Optional<Object> optionalObject = context.getData(key);

        if (!optionalObject.isPresent()) {
            logger.warning("Failed to find {} in the context.", key);
            return "";
        }

        final Object value = optionalObject.get();
        if (!(value instanceof String)) {
            logger.warning("Could not extract {}. Data is not of type String. Actual class: {}",
                key,
                value.getClass());
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
        final Optional<Object> spanOptional = context.getData(OPENCENSUS_SPAN_KEY);
        if (!spanOptional.isPresent()) {
            logger.warning("Failed to find span in the context.");
            return null;
        }

        final Object value = spanOptional.get();
        if (!(value instanceof Span)) {
            logger.warning("Could not extract span. Data in {} is not of type Span. Actual class: {}",
                OPENCENSUS_SPAN_KEY,
                value.getClass());
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
        final Optional<Object> spanNameOptional = context.getData(OPENCENSUS_SPAN_NAME_KEY);
        if (!spanNameOptional.isPresent()) {
            logger.warning("Failed to find span name in the context.");
            return null;
        }

        final Object value = spanNameOptional.get();
        if (!(value instanceof String)) {
            logger.warning("Could not extract span name. Data in {} is not of type String. Actual class: {}",
                OPENCENSUS_SPAN_NAME_KEY,
                value.getClass());
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
        final Optional<Object> spanContextOptional = context.getData(SPAN_CONTEXT);
        if (!spanContextOptional.isPresent()) {
            logger.warning("Failed to find span context in the context.");
            return null;
        }

        final Object value = spanContextOptional.get();
        if (!(value instanceof SpanContext)) {
            logger.warning("Could not extract span context. Data is not of type SpanContext. Actual class: {}",
                value.getClass());
            return null;
        }

        return (SpanContext) value;
    }

    /**
     * Returns a {@link SpanBuilder} to create and start a new child {@link Span} with parent being the designated
     * {@code Span}.
     *
     * @param spanName The name of the returned Span.
     * @param context The context containing the span and the span name.
     * @return A {@code SpanBuilder} to create and start a new {@code Span}.
     */
    private SpanBuilder getSpanBuilder(String spanName, Context context) {
        Span parentSpan = getSpan(context) == null ? TRACER.getCurrentSpan() : getSpan(context);
        String spanNameKey = getSpanName(context) == null ? spanName : getSpanName(context);
        SpanBuilder spanBuilder = TRACER.spanBuilderWithExplicitParent(spanNameKey, parentSpan);
        return spanBuilder;
    }
}
