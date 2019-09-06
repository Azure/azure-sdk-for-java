// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

<<<<<<< HEAD:sdk/tracing/azure-core-tracing-opencensus/src/main/java/com/azure/core/tracing/opencensus/OpenCensusTracer.java
package com.azure.tracing.opentelemetry;
=======
package com.azure.core.tracing.opencensus;
>>>>>>> Updating everything to use OpenCensus as that is what the package uses:sdk/tracing/azure-core-tracing-opencensus/src/main/java/com/azure/core/tracing/opencensus/OpenCensusTracer.java

import com.azure.core.implementation.tracing.ProcessKind;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.tracing.opencensus.implementation.AmqpPropagationFormatUtil;
import com.azure.core.tracing.opencensus.implementation.AmqpTraceUtil;
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

import java.util.Locale;
import java.util.Optional;

import static io.opencensus.trace.Link.Type.PARENT_LINKED_SPAN;

/**
 * OpenCensus span
 */
<<<<<<< HEAD:sdk/tracing/azure-core-tracing-opencensus/src/main/java/com/azure/core/tracing/opencensus/OpenCensusTracer.java
public class OpenTelemetryTracer implements com.azure.core.util.tracing.Tracer {
    // Singleton OpenTelemetry tracer capable of starting and exporting spans.
=======
public class OpenCensusTracer implements com.azure.core.implementation.tracing.Tracer {
    // Singleton OpenCensus tracer capable of starting and exporting spans.
>>>>>>> Updating everything to use OpenCensus as that is what the package uses:sdk/tracing/azure-core-tracing-opencensus/src/main/java/com/azure/core/tracing/opencensus/OpenCensusTracer.java
    private static final Tracer TRACER = Tracing.getTracer();

    // standard attributes with AMQP call information
    private static final String COMPONENT = "component";
    private static final String MESSAGE_BUS_DESTINATION = "message_bus.destination";
    private static final String PEER_ENDPOINT = "peer.address";

    private final ClientLogger logger = new ClientLogger(OpenCensusTracer.class);

    @Override
    public Context start(String methodName, Context context) {
        Span parentSpan = (Span) context.getData(OPENTELEMETRY_SPAN_KEY).orElse(TRACER.getCurrentSpan());
        String spanName = (String) context.getData(OPENTELEMETRY_SPAN_NAME_KEY).orElse(methodName);

        SpanBuilder spanBuilder = TRACER.spanBuilderWithExplicitParent(spanName, parentSpan);
        Span span = spanBuilder.startSpan();

        return context.addData(OPENTELEMETRY_SPAN_KEY, span);
    }

    @Override
    public Context start(String spanName, Context context, ProcessKind processKind) {
        Span span;
        SpanBuilder spanBuilder;
        switch (processKind) {
            case SEND:
                spanBuilder = startSpanWithExplicitParent(spanName, context);
                span = spanBuilder.setSpanKind(Span.Kind.CLIENT).startSpan();
                if (span.getOptions().contains(Span.Options.RECORD_EVENTS)) {
                    // If span is sampled in, add additional TRACING attributes
                    addSpanRequestAttributes(span, context, spanName);
                }
                return context.addData(OPENCENSUS_SPAN_KEY, span);
            case RECEIVE:
                spanBuilder = startSpanWithExplicitParent(spanName, context);
                span = spanBuilder.startSpan();
                // Add diagnostic Id to Context
                context = setContextData(span);
                return context.addData(OPENCENSUS_SPAN_KEY, span);
            case PROCESS:
                return startScopedSpan(spanName, context);
            default:
                return Context.NONE;
        }
    }

    @Override
    public void end(int responseCode, Throwable throwable, Context context) {
        final Span span = getSpan(context);
        if (span == null) {
            return;
        }

        if (span.getOptions().contains(Options.RECORD_EVENTS)) {
            span.setStatus(HttpTraceUtil.parseResponseStatus(responseCode, throwable));
        }

        span.end();
    }

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

    @Override
    public Context setSpanName(String spanName, Context context) {
        return context.addData(OPENTELEMETRY_SPAN_NAME_KEY, spanName);
    }

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

    @Override
    public void addLink(Context eventContext) {
        final Span span = getSpan(eventContext);
        if (span == null) {
            logger.warning("Failed to find span to link it.");
            return;
        }

        final Optional<Object> spanContextOptional = eventContext.getData(SPAN_CONTEXT);
        if (!spanContextOptional.isPresent()) {
            logger.warning("Failed to find Span context to link it.");
            return;
        } else if (!(spanContextOptional.get() instanceof SpanContext)) {
            logger.warning("Context in event is not of type SpanContext. Actual: {}",
                spanContextOptional.get().getClass());
            return;
        }

        final SpanContext spanContext = (SpanContext) spanContextOptional.get();

        // TODO: Needs to be updated with OpenCensus support to addLink using Span Context before span is started
        // and no link type is needed.
        span.addLink(Link.fromSpanContext(spanContext, PARENT_LINKED_SPAN));
    }

    @Override
    public Context extractContext(String diagnosticId, Context context) {
        return AmqpPropagationFormatUtil.extractContext(diagnosticId, context);
    }

    private Context startScopedSpan(String spanName, Context context) {
        Span span;
        Optional<Object> optionalSpanContext = context.getData(SPAN_CONTEXT);
        if (optionalSpanContext.isPresent() && optionalSpanContext.get() instanceof SpanContext) {
            span = startSpanWithRemoteParent(spanName, (SpanContext) optionalSpanContext.get());
        } else {
            SpanBuilder spanBuilder = startSpanWithExplicitParent(spanName, context);
            span = spanBuilder.setSpanKind(Span.Kind.SERVER).startSpan();
        }
        return context.addData(OPENCENSUS_SPAN_KEY, span).addData("scope", TRACER.withSpan(span));
    }

    private SpanBuilder startSpanWithExplicitParent(String spanName, Context context) {
        Optional<Object> optionalSpanKey = context.getData(OPENCENSUS_SPAN_KEY);
        Optional<Object> optionalSpanNameKey = context.getData(OPENCENSUS_SPAN_NAME_KEY);
        Span parentSpan = null;
        String spanNameKey = null;

        if (optionalSpanKey.get() instanceof Span && optionalSpanNameKey.get() instanceof String) {
            parentSpan = (Span) context.getData(OPENCENSUS_SPAN_KEY).orElse(TRACER.getCurrentSpan());
            spanNameKey = (String) context.getData(OPENCENSUS_SPAN_NAME_KEY).orElse(spanName);
        } else {
            logger.warning(String.format(Locale.US,
                "Parent span type is not of type Span, but type: %s. Failed to add span links.",
                optionalSpanKey.get() != null ? optionalSpanKey.get().getClass() : "null"));
        }

        SpanBuilder spanBuilder = TRACER.spanBuilderWithExplicitParent(spanNameKey, parentSpan);
        return spanBuilder;
    }

    private Span startSpanWithRemoteParent(String spanName, SpanContext spanContext) {
        SpanBuilder spanBuilder = TRACER.spanBuilderWithRemoteParent(spanName, spanContext);
        spanBuilder.setSpanKind(Span.Kind.SERVER);
        return spanBuilder.startSpan();

    }

    private Context setContextData(Span span) {
        final String traceparent = AmqpPropagationFormatUtil.getDiagnosticId(span.getContext());
        Context parentContext = new Context(DIAGNOSTIC_ID_KEY, traceparent).addData(SPAN_CONTEXT, span.getContext());
        return parentContext;
    }

    private static void addSpanRequestAttributes(Span span, Context context, String spanName) {
        if (context.getData(ENTITY_PATH).isPresent() && context.getData(HOST_NAME).isPresent()) {
            span.putAttribute(COMPONENT, AttributeValue.stringAttributeValue(parseComponentValue(spanName)));
            span.putAttribute(
                MESSAGE_BUS_DESTINATION,
                AttributeValue.stringAttributeValue(context.getData(ENTITY_PATH).get().toString()));
            span.putAttribute(
                PEER_ENDPOINT,
                AttributeValue.stringAttributeValue(context.getData(HOST_NAME).get().toString()));
        }
    }

    private static String parseComponentValue(String spanName) {
        return spanName.substring(spanName.indexOf(".") + 1, spanName.lastIndexOf("."));
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
}
