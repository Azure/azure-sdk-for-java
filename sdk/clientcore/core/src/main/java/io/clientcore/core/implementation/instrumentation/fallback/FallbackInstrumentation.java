// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.implementation.instrumentation.NoopMeter;
import io.clientcore.core.implementation.instrumentation.SdkInstrumentationOptionsAccessHelper;
import io.clientcore.core.instrumentation.Instrumentation;
import io.clientcore.core.instrumentation.InstrumentationAttributes;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import io.clientcore.core.instrumentation.SdkInstrumentationOptions;
import io.clientcore.core.instrumentation.metrics.Meter;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanBuilder;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;

import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static io.clientcore.core.implementation.instrumentation.AttributeKeys.SERVER_ADDRESS_KEY;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.SERVER_PORT_KEY;
import static io.clientcore.core.implementation.instrumentation.InstrumentationUtils.UNKNOWN_LIBRARY_OPTIONS;

/**
 * Fallback implementation of {@link Instrumentation} which implements basic correlation and context propagation
 * and, when enabled, records traces as logs.
 */
public class FallbackInstrumentation implements Instrumentation {
    public static final FallbackInstrumentation DEFAULT_INSTANCE
        = new FallbackInstrumentation(null, UNKNOWN_LIBRARY_OPTIONS, null, -1);

    private final boolean allowNestedSpans;
    private final boolean isTracingEnabled;
    private final FallbackTracer tracer;
    private final String serviceHost;
    private final int servicePort;

    /**
     * Creates a new instance of {@link FallbackInstrumentation}.
     * @param instrumentationOptions the application instrumentation options
     * @param sdkOptions the library instrumentation options
     * @param host the service host
     * @param port the service port
     */
    public FallbackInstrumentation(InstrumentationOptions instrumentationOptions, SdkInstrumentationOptions sdkOptions,
        String host, int port) {
        this.allowNestedSpans
            = sdkOptions != null && SdkInstrumentationOptionsAccessHelper.isSpanSuppressionDisabled(sdkOptions);
        this.isTracingEnabled = instrumentationOptions == null || instrumentationOptions.isTracingEnabled();
        this.tracer = new FallbackTracer(instrumentationOptions, sdkOptions);
        this.serviceHost = host;
        this.servicePort = port;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tracer getTracer() {
        return tracer;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Meter getMeter() {
        // We don't provide fallback metrics support. This might change in the future.
        // Some challenges:
        // - metric aggregation is complicated
        // - having metrics reported in logs is not very useful
        return NoopMeter.INSTANCE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InstrumentationAttributes createAttributes(Map<String, Object> attributes) {
        return new FallbackAttributes(attributes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TraceContextPropagator getW3CTraceContextPropagator() {
        return FallbackContextPropagator.W3C_TRACE_CONTEXT_PROPAGATOR;
    }

    @Override
    public <TResponse> TResponse instrumentWithResponse(String operationName, RequestContext requestContext,
        Function<RequestContext, TResponse> operation) {
        Objects.requireNonNull(operationName, "'operationName' cannot be null");
        Objects.requireNonNull(operation, "'operation' cannot be null");

        requestContext = requestContext == null ? RequestContext.none() : requestContext;
        InstrumentationContext context = requestContext.getInstrumentationContext();

        if (!shouldInstrument(SpanKind.CLIENT, context)) {
            return operation.apply(requestContext);
        }

        SpanBuilder builder
            = tracer.spanBuilder(operationName, SpanKind.CLIENT, context).setAttribute(SERVER_ADDRESS_KEY, serviceHost);

        if (servicePort > 0) {
            builder.setAttribute(SERVER_PORT_KEY, servicePort);
        }

        Span span = builder.startSpan();

        RequestContext childContext
            = requestContext.toBuilder().setInstrumentationContext(span.getInstrumentationContext()).build();
        TracingScope scope = span.makeCurrent();
        try {
            TResponse response = operation.apply(childContext);
            span.end();
            return response;
        } catch (RuntimeException t) {
            span.end(t);
            throw t;
        } finally {
            scope.close();
        }
    }

    /**
     * Creates a new instance of {@link InstrumentationContext} from the given object.
     * It recognizes {@link FallbackSpanContext}, {@link FallbackSpan}, and generic {@link InstrumentationContext}
     * as a source and converts them to {@link FallbackSpanContext}.
     * @param context the context object to convert
     * @return the instance of {@link InstrumentationContext} which is invalid if the context is not recognized
     * @param <T> the type of the context object
     */
    public <T> InstrumentationContext createInstrumentationContext(T context) {
        if (context instanceof InstrumentationContext) {
            return FallbackSpanContext.fromInstrumentationContext((InstrumentationContext) context);
        } else if (context instanceof FallbackSpan) {
            return ((FallbackSpan) context).getInstrumentationContext();
        } else {
            return FallbackSpanContext.INVALID;
        }
    }

    private boolean shouldInstrument(SpanKind spanKind, InstrumentationContext context) {
        if (!isTracingEnabled) {
            return false;
        }

        if (allowNestedSpans) {
            return true;
        }

        return spanKind != tryGetSpanKind(context);
    }

    /**
     * Retrieves the span kind from the given context if and only if the context is a {@link FallbackSpanContext}
     * i.e. was created by this instrumentation.
     * @param context the context to get the span kind from
     * @return the span kind or {@code null} if the context is not recognized
     */
    private SpanKind tryGetSpanKind(InstrumentationContext context) {
        if (context instanceof FallbackSpanContext) {
            Span span = context.getSpan();
            if (span instanceof FallbackSpan) {
                return ((FallbackSpan) span).getSpanKind();
            }
        }
        return null;
    }
}
