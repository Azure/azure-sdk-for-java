// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel.tracing;

import io.clientcore.core.implementation.instrumentation.otel.FallbackInvoker;
import io.clientcore.core.implementation.instrumentation.otel.OTelAttributeKey;
import io.clientcore.core.implementation.instrumentation.otel.OTelContext;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;

import static io.clientcore.core.implementation.instrumentation.AttributeKeys.ERROR_TYPE_KEY;
import static io.clientcore.core.implementation.instrumentation.otel.OTelContext.markCoreSpan;
import static io.clientcore.core.implementation.instrumentation.otel.tracing.OTelSpanContext.INVALID_OTEL_SPAN_CONTEXT;

/**
 * OpenTelemetry implementation of {@link Span}.
 */
@SuppressWarnings("unchecked")
public class OTelSpan implements Span {
    private static final ClientLogger LOGGER = new ClientLogger(OTelSpan.class);
    private static final Object ERROR_TYPE_ATTRIBUTE_KEY;
    private static final TracingScope NOOP_SCOPE = () -> {
    };
    private final Object otelSpan;
    private final Object otelContext;
    private final boolean isRecording;
    private final SpanKind spanKind;
    private String errorType;
    private OTelSpanContext spanContext;

    static {
        Object errorTypeAttributeKey = null;

        if (OTelInitializer.isInitialized()) {
            try {
                errorTypeAttributeKey = OTelAttributeKey.getKey(ERROR_TYPE_KEY, "");
            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        ERROR_TYPE_ATTRIBUTE_KEY = errorTypeAttributeKey;
    }

    OTelSpan(Object otelSpan, Object otelParentContext, SpanKind spanKind) {
        this.otelSpan = otelSpan;
        this.isRecording = otelSpan != null
            && FallbackInvoker.call(LOGGER, false, () -> ((io.opentelemetry.api.trace.Span) otelSpan).isRecording());
        this.spanKind = spanKind;

        Object contextWithSpan = otelSpan != null ? storeInContext(otelSpan, otelParentContext) : otelParentContext;
        this.otelContext = markCoreSpan(contextWithSpan, this);
    }

    private OTelSpan(Object otelSpan, Object otelContext) {
        this.otelSpan = otelSpan;
        this.isRecording = false;
        this.spanKind = null;
        this.otelContext = otelContext;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OTelSpan setAttribute(String key, Object value) {
        if (isInitialized() && isRecording) {
            Object otelKey = OTelAttributeKey.getKey(key, value);
            if (otelKey != null) {
                FallbackInvoker.run(LOGGER, () -> ((io.opentelemetry.api.trace.Span) otelSpan)
                    .setAttribute((AttributeKey<Object>) otelKey, OTelAttributeKey.castAttributeValue(value)));
            }
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span setError(String errorType) {
        this.errorType = errorType;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end(Throwable throwable) {
        endSpan(throwable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void end() {
        endSpan(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isRecording() {
        return isRecording;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TracingScope makeCurrent() {
        return isInitialized() ? wrapOTelScope(OTelContext.makeCurrent(otelContext)) : NOOP_SCOPE;
    }

    /**
     * {@inheritDoc}
     */
    public InstrumentationContext getInstrumentationContext() {
        if (spanContext != null) {
            return spanContext;
        }

        spanContext = isInitialized()
            ? new OTelSpanContext(FallbackInvoker.call(LOGGER, INVALID_OTEL_SPAN_CONTEXT,
                () -> ((io.opentelemetry.api.trace.Span) otelSpan).getSpanContext()), otelContext)
            : OTelSpanContext.getInvalid();

        return spanContext;
    }

    /**
     * Gets the span kind.
     * @return the span kind
     */
    public SpanKind getSpanKind() {
        return spanKind;
    }

    static OTelSpan createPropagatingSpan(OTelSpanContext spanContext) {
        Object span = wrapSpanContext(spanContext.getOtelSpanContext());
        return new OTelSpan(span, spanContext.getOtelContext());
    }

    static Object createPropagatingSpan(Object otelContext) {
        assert otelContext instanceof Context;

        Object span = fromOTelContext(otelContext);

        Object spanContext = FallbackInvoker.call(LOGGER, INVALID_OTEL_SPAN_CONTEXT,
            () -> ((io.opentelemetry.api.trace.Span) span).getSpanContext());
        assert spanContext instanceof SpanContext;

        return wrapSpanContext(spanContext);
    }

    static Object fromOTelContext(Object otelContext) {
        assert otelContext instanceof Context;

        Object span = FallbackInvoker.call(LOGGER, null,
            () -> io.opentelemetry.api.trace.Span.fromContext((Context) otelContext));
        assert span instanceof io.opentelemetry.api.trace.Span;

        return span;
    }

    /**
     * Wraps an OpenTelemetry span context in a propagating span.
     * @param otelSpanContext the OpenTelemetry span context
     * @return the propagating span
     */
    public static Object wrapSpanContext(Object otelSpanContext) {
        return FallbackInvoker.call(LOGGER, null,
            () -> io.opentelemetry.api.trace.Span.wrap((SpanContext) otelSpanContext));
    }

    static Object getSpanContext(Object otelSpan) {
        assert otelSpan instanceof io.opentelemetry.api.trace.Span;

        Object spanContext = FallbackInvoker.call(LOGGER, INVALID_OTEL_SPAN_CONTEXT,
            () -> ((io.opentelemetry.api.trace.Span) otelSpan).getSpanContext());
        assert spanContext instanceof SpanContext;

        return spanContext;
    }

    /**
     * Stores the given span in the given context.
     * @param otelSpan the OpenTelemetry span
     * @param otelContext the OpenTelemetry context
     * @return the updated context
     */
    public static Object storeInContext(Object otelSpan, Object otelContext) {
        Object updatedContext = FallbackInvoker.call(LOGGER, null,
            () -> ((io.opentelemetry.api.trace.Span) otelSpan).storeInContext((Context) otelContext));

        return updatedContext != null ? updatedContext : otelContext;
    }

    private void endSpan(Throwable throwable) {
        if (isInitialized()) {
            if (errorType != null || throwable != null) {

                String errorTypeStr = errorType != null ? errorType : throwable.getClass().getCanonicalName();
                FallbackInvoker.run(LOGGER, () -> ((io.opentelemetry.api.trace.Span) otelSpan)
                    .setAttribute((AttributeKey<Object>) ERROR_TYPE_ATTRIBUTE_KEY, errorTypeStr));

                String description = throwable == null ? null : throwable.getMessage();
                FallbackInvoker.run(LOGGER, () -> {
                    io.opentelemetry.api.trace.Span span = (io.opentelemetry.api.trace.Span) otelSpan;
                    if (description == null) {
                        span.setStatus(StatusCode.ERROR);
                    } else {
                        span.setStatus(StatusCode.ERROR, description);
                    }
                });
            }

            FallbackInvoker.run(LOGGER, () -> ((io.opentelemetry.api.trace.Span) otelSpan).end());
        }
    }

    private static TracingScope wrapOTelScope(AutoCloseable otelScope) {
        return () -> {
            try {
                otelScope.close();
            } catch (Exception e) {
                OTelInitializer.runtimeError(LOGGER, e);
            }
        };
    }

    private boolean isInitialized() {
        return otelSpan != null && OTelInitializer.isInitialized();
    }
}
