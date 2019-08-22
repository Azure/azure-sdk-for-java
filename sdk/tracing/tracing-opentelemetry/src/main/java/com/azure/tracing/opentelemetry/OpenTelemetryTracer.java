// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tracing.opentelemetry;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;

import com.azure.tracing.opentelemetry.implementation.AmqpPropagationFormatUtil;
import com.azure.tracing.opentelemetry.implementation.AmqpTraceUtil;
import com.azure.tracing.opentelemetry.implementation.HttpTraceUtil;
import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;

import java.util.Optional;

import static io.opencensus.trace.Link.Type.PARENT_LINKED_SPAN;

/**
 * OpenTelemetry span
 */
public class OpenTelemetryTracer implements com.azure.core.implementation.tracing.Tracer {
    // Singleton OpenTelemetry tracer capable of starting and exporting spans.
    private static final Tracer TRACER = Tracing.getTracer();
    private static final String OPENTELEMETRY_SPAN_KEY = com.azure.core.implementation.tracing.Tracer.OPENTELEMETRY_SPAN_KEY;
    private static final String OPENTELEMETRY_SPAN_NAME_KEY = com.azure.core.implementation.tracing.Tracer.OPENTELEMETRY_SPAN_NAME_KEY;
    private static final String DIAGNOSTIC_ID = com.azure.core.implementation.tracing.Tracer.OPENTELEMETRY_DIAGNOSTIC_ID_KEY;
    private static final String ENTITY_PATH = com.azure.core.implementation.tracing.Tracer.OPENTELEMETRY_AMQP_ENTITY_PATH;
    private static final String HOSTNAME = com.azure.core.implementation.tracing.Tracer.OPENTELEMETRY_AMQP_HOST_NAME;
    private static final String SPAN_CONTEXT = com.azure.core.implementation.tracing.Tracer.OPENTELEMETRY_AMQP_EVENT_SPAN_CONTEXT;

    // standard attributes with AMQP call information
    private static final String COMPONENT = "component";
    private static final String MESSAGE_BUS_DESTINATION = "message_bus.destination";
    private static final String PEER_ENDPOINT = "peer.address";

    private final ClientLogger logger = new ClientLogger(OpenTelemetryTracer.class);

    @Override
    public Context start(String spanName, Context context) {
        Span span = startSpanWithExplicitParent(spanName, context);
        if (context.getData(ENTITY_PATH).isPresent() && span.getOptions().contains(Span.Options.RECORD_EVENTS)) {
            // If span is sampled in, add additional TRACING attributes
            addSpanRequestAttributes(span, context, spanName);
        } else {
            // Add diagnostic Id to Context
            context = setContextData(span);
        }
        return context.addData(OPENTELEMETRY_SPAN_KEY, span);
    }

    @Override
    public Context startScopedSpan(String spanName, Context context) {
        Span span;
        if (context.getData(SPAN_CONTEXT).isPresent()) {
            span = startSpanWithRemoteParent(spanName, context);
        } else {
            span = startSpanWithExplicitParent(spanName, context);
        }
        return context.addData(OPENTELEMETRY_SPAN_KEY, span).addData("scope", TRACER.withSpan(span));
    }

    @Override
    public void end(int responseCode, Throwable throwable, Context context) {
        Optional<Object> spanOptional = context.getData(OPENTELEMETRY_SPAN_KEY);
        if (!spanOptional.isPresent()) {
            logger.warning("Failed to find span to end it.");
            return;
        }

        Span span = (Span) spanOptional.get();

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

        Optional<Object> spanOptional = context.getData(OPENTELEMETRY_SPAN_KEY);
        if (spanOptional.isPresent()) {
            Span span = (Span) spanOptional.get();
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
    public void endAmqp(String errorCondition, Context context, Throwable throwable) {
        Optional<Object> spanOptional = context.getData(OPENTELEMETRY_SPAN_KEY);
        if (!spanOptional.isPresent()) {
            logger.warning("Failed to find span to end it.");
            return;
        }

        Span span = (Span) spanOptional.get();

        if (span.getOptions().contains(Options.RECORD_EVENTS)) {
            span.setStatus(AmqpTraceUtil.parseErrorCondition(errorCondition, throwable));
        }

        span.end();
    }

    @Override
    public void addLink(Context eventContext) {
        Optional<Object> spanContextOptional = eventContext.getData(SPAN_CONTEXT);
        Optional<Object> spanOptional = eventContext.getData(OPENTELEMETRY_SPAN_KEY); // TODO: we need this to be the parent span key

        if (!spanOptional.isPresent()) {
            logger.warning("Failed to find span to link it.");
            return;
        }
        SpanContext spanContext = (SpanContext) spanContextOptional.get();
        Span span = (Span) spanOptional.get();

        span.addLink(Link.fromSpanContext(spanContext, PARENT_LINKED_SPAN));
    }

    @Override
    public Context extractContext(String diagnosticId) {
        return AmqpPropagationFormatUtil.extractContext(diagnosticId);
    }

    private Span startSpanWithExplicitParent(String spanName, Context context) {
        Span parentSpan = (Span) context.getData(OPENTELEMETRY_SPAN_KEY).orElse(TRACER.getCurrentSpan());
        String spanNameKey = (String) context.getData(OPENTELEMETRY_SPAN_NAME_KEY).orElse(spanName);

        SpanBuilder spanBuilder = TRACER.spanBuilderWithExplicitParent(spanNameKey, parentSpan);
        return spanBuilder.startSpan();
    }

    private Span startSpanWithRemoteParent(String spanName, Context context) {
        SpanBuilder spanBuilder = TRACER.spanBuilderWithRemoteParent(spanName, (SpanContext) context.getData(SPAN_CONTEXT).get());
        return spanBuilder.startSpan();

    }

    private Context setContextData(Span span) {
        final String traceparent = AmqpPropagationFormatUtil.getDiagnosticId(span.getContext());
        Context parentContext = new Context(DIAGNOSTIC_ID, traceparent).addData(SPAN_CONTEXT, span.getContext());
        return parentContext;
    }

    private static void addSpanRequestAttributes(Span span, Context context, String spanName) {
        span.putAttribute(COMPONENT, AttributeValue.stringAttributeValue(parseComponentValue(spanName)));
        span.putAttribute(MESSAGE_BUS_DESTINATION, AttributeValue.stringAttributeValue(context.getData(ENTITY_PATH).get().toString()));
        span.putAttribute(PEER_ENDPOINT, AttributeValue.stringAttributeValue(context.getData(HOSTNAME).get().toString()));
    }

    private static String parseComponentValue(String spanName) {
        return spanName.substring(spanName.indexOf(".") + 1, spanName.lastIndexOf("."));
    }
}
