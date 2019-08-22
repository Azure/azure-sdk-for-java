// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tracing.opentelemetry;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.implementation.util.ImplUtils;
import com.azure.core.util.Context;

import io.opencensus.trace.Span;
import io.opencensus.trace.Span.Options;
import io.opencensus.trace.SpanBuilder;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;
import io.opencensus.trace.SpanContext;
import io.opencensus.trace.AttributeValue;
import io.opencensus.trace.Link;
import sun.security.jgss.spnego.SpNegoContext;

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
        Span span;
        if (!context.getData(OPENTELEMETRY_SPAN_KEY).isPresent()) {
            SpanBuilder spanBuilder = TRACER.spanBuilderWithRemoteParent(spanName, (SpanContext) context.getData(SPAN_CONTEXT).get());
            span = spanBuilder.startSpan();
        } else {
            Span parentSpan = (Span) context.getData(OPENTELEMETRY_SPAN_KEY).orElse(TRACER.getCurrentSpan());
            String spanNameKey = (String) context.getData(OPENTELEMETRY_SPAN_NAME_KEY).orElse(spanName);

            SpanBuilder spanBuilder = TRACER.spanBuilderWithExplicitParent(spanNameKey, parentSpan);
            span = spanBuilder.startSpan();
            if (context.getData(ENTITY_PATH).isPresent()) {
                // If span is sampled in, add additional TRACING attributes
                if (span.getOptions().contains(Span.Options.RECORD_EVENTS)) {
                    addSpanRequestAttributes(span, context, spanName);
                }
            } else {
                return setContextData(span);
            }
        }
        return context.addData(OPENTELEMETRY_SPAN_KEY, span);
    }

    private Context setContextData(Span span) {

        final String traceparent = DiagnosticIdConversionUtil.getDiagnosticId(span.getContext());
        Context parentContext = new Context(DIAGNOSTIC_ID, traceparent).addData(OPENTELEMETRY_SPAN_KEY, span).addData(SPAN_CONTEXT, span.getContext());
        return parentContext;
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
        Optional<Object> spanOptional = eventContext.getData(OPENTELEMETRY_SPAN_KEY); // TODO: need to confirm this

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
        return AmqpPropagationFormat.extractContext(diagnosticId);
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
