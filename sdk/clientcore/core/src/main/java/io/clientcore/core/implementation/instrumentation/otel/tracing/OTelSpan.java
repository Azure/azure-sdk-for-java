// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel.tracing;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.instrumentation.otel.FallbackInvoker;
import io.clientcore.core.implementation.instrumentation.otel.OTelAttributeKey;
import io.clientcore.core.implementation.instrumentation.otel.OTelContext;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.instrumentation.AttributeKeys.ERROR_TYPE_KEY;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.ATTRIBUTE_KEY_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.CONTEXT_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.SPAN_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.SPAN_CONTEXT_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.STATUS_CODE_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelContext.markCoreSpan;
import static io.clientcore.core.implementation.instrumentation.otel.tracing.OTelSpanContext.INVALID_OTEL_SPAN_CONTEXT;

/**
 * OpenTelemetry implementation of {@link Span}.
 */
public class OTelSpan implements Span {
    private static final ClientLogger LOGGER = new ClientLogger(OTelSpan.class);
    static final OTelSpan NOOP_SPAN;
    private static final Object ERROR_TYPE_ATTRIBUTE_KEY;
    private static final TracingScope NOOP_SCOPE = () -> {
    };
    private static final FallbackInvoker SET_ATTRIBUTE_INVOKER;
    private static final FallbackInvoker SET_STATUS_INVOKER;
    private static final FallbackInvoker END_INVOKER;
    private static final FallbackInvoker GET_SPAN_CONTEXT_INVOKER;
    private static final FallbackInvoker IS_RECORDING_INVOKER;
    private static final FallbackInvoker STORE_IN_CONTEXT_INVOKER;
    private static final FallbackInvoker FROM_CONTEXT_INVOKER;
    private static final FallbackInvoker WRAP_INVOKER;
    private static final Object ERROR_STATUS_CODE;
    private final Object otelSpan;
    private final Object otelContext;
    private final boolean isRecording;
    private final SpanKind spanKind;
    private String errorType;
    private OTelSpanContext spanContext;

    static {
        ReflectiveInvoker setAttributeInvoker = null;
        ReflectiveInvoker setStatusInvoker = null;
        ReflectiveInvoker endInvoker = null;
        ReflectiveInvoker getSpanContextInvoker = null;
        ReflectiveInvoker isRecordingInvoker = null;
        ReflectiveInvoker storeInContextInvoker = null;
        ReflectiveInvoker fromContextInvoker = null;
        ReflectiveInvoker wrapInvoker = null;

        Object errorStatusCode = null;
        OTelSpan noopSpan = null;
        Object errorTypeAttributeKey = null;

        if (OTelInitializer.isInitialized()) {
            try {
                setAttributeInvoker = getMethodInvoker(SPAN_CLASS,
                    SPAN_CLASS.getMethod("setAttribute", ATTRIBUTE_KEY_CLASS, Object.class));

                setStatusInvoker
                    = getMethodInvoker(SPAN_CLASS, SPAN_CLASS.getMethod("setStatus", STATUS_CODE_CLASS, String.class));
                endInvoker = getMethodInvoker(SPAN_CLASS, SPAN_CLASS.getMethod("end"));

                isRecordingInvoker = getMethodInvoker(SPAN_CLASS, SPAN_CLASS.getMethod("isRecording"));

                getSpanContextInvoker = getMethodInvoker(SPAN_CLASS, SPAN_CLASS.getMethod("getSpanContext"));

                storeInContextInvoker
                    = getMethodInvoker(SPAN_CLASS, SPAN_CLASS.getMethod("storeInContext", CONTEXT_CLASS));
                fromContextInvoker = getMethodInvoker(SPAN_CLASS, SPAN_CLASS.getMethod("fromContext", CONTEXT_CLASS));

                wrapInvoker = getMethodInvoker(SPAN_CLASS, SPAN_CLASS.getMethod("wrap", SPAN_CONTEXT_CLASS));
                errorStatusCode = STATUS_CODE_CLASS.getField("ERROR").get(null);

                ReflectiveInvoker getInvalidInvoker = getMethodInvoker(SPAN_CLASS, SPAN_CLASS.getMethod("getInvalid"));

                Object invalidSpan = getInvalidInvoker.invoke();
                Object rootContext = OTelContext.getCurrent();

                noopSpan = new OTelSpan(invalidSpan, rootContext);
                errorTypeAttributeKey = OTelAttributeKey.getKey(ERROR_TYPE_KEY, "");
            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        SET_ATTRIBUTE_INVOKER = new FallbackInvoker(setAttributeInvoker, LOGGER);
        SET_STATUS_INVOKER = new FallbackInvoker(setStatusInvoker, LOGGER);
        END_INVOKER = new FallbackInvoker(endInvoker, LOGGER);
        GET_SPAN_CONTEXT_INVOKER = new FallbackInvoker(getSpanContextInvoker, INVALID_OTEL_SPAN_CONTEXT, LOGGER);
        IS_RECORDING_INVOKER = new FallbackInvoker(isRecordingInvoker, false, LOGGER);
        STORE_IN_CONTEXT_INVOKER = new FallbackInvoker(storeInContextInvoker, LOGGER);
        FROM_CONTEXT_INVOKER = new FallbackInvoker(fromContextInvoker, LOGGER);
        WRAP_INVOKER = new FallbackInvoker(wrapInvoker, LOGGER);
        NOOP_SPAN = noopSpan;

        ERROR_STATUS_CODE = errorStatusCode;
        ERROR_TYPE_ATTRIBUTE_KEY = errorTypeAttributeKey;
    }

    OTelSpan(Object otelSpan, Object otelParentContext, SpanKind spanKind) {
        this.otelSpan = otelSpan;
        this.isRecording = otelSpan != null && (boolean) IS_RECORDING_INVOKER.invoke(otelSpan);
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
                SET_ATTRIBUTE_INVOKER.invoke(otelSpan, otelKey, OTelAttributeKey.castAttributeValue(value));
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
            ? new OTelSpanContext(GET_SPAN_CONTEXT_INVOKER.invoke(otelSpan), otelContext)
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

    Object getOtelSpan() {
        return otelSpan;
    }

    static OTelSpan createPropagatingSpan(OTelSpanContext spanContext) {
        Object span = wrapSpanContext(spanContext.getOtelSpanContext());
        return new OTelSpan(span, spanContext.getOtelContext());
    }

    static Object createPropagatingSpan(Object otelContext) {
        assert CONTEXT_CLASS.isInstance(otelContext);

        Object span = fromOTelContext(otelContext);

        Object spanContext = GET_SPAN_CONTEXT_INVOKER.invoke(span);
        assert SPAN_CONTEXT_CLASS.isInstance(spanContext);

        return wrapSpanContext(spanContext);
    }

    static Object fromOTelContext(Object otelContext) {
        assert CONTEXT_CLASS.isInstance(otelContext);

        Object span = FROM_CONTEXT_INVOKER.invoke(otelContext);
        assert SPAN_CLASS.isInstance(span);

        return span;
    }

    /**
     * Wraps an OpenTelemetry span context in a propagating span.
     * @param otelSpanContext the OpenTelemetry span context
     * @return the propagating span
     */
    public static Object wrapSpanContext(Object otelSpanContext) {
        return WRAP_INVOKER.invoke(otelSpanContext);
    }

    static Object getSpanContext(Object otelSpan) {
        assert SPAN_CLASS.isInstance(otelSpan);

        Object spanContext = GET_SPAN_CONTEXT_INVOKER.invoke(otelSpan);
        assert SPAN_CONTEXT_CLASS.isInstance(spanContext);

        return spanContext;
    }

    /**
     * Stores the given span in the given context.
     * @param otelSpan the OpenTelemetry span
     * @param otelContext the OpenTelemetry context
     * @return the updated context
     */
    public static Object storeInContext(Object otelSpan, Object otelContext) {
        Object updatedContext = STORE_IN_CONTEXT_INVOKER.invoke(otelSpan, otelContext);

        return updatedContext != null ? updatedContext : otelContext;
    }

    private void endSpan(Throwable throwable) {
        if (isInitialized()) {
            if (errorType != null || throwable != null) {

                String errorTypeStr = errorType != null ? errorType : throwable.getClass().getCanonicalName();
                SET_ATTRIBUTE_INVOKER.invoke(otelSpan, ERROR_TYPE_ATTRIBUTE_KEY, errorTypeStr);

                SET_STATUS_INVOKER.invoke(otelSpan, ERROR_STATUS_CODE,
                    throwable == null ? null : throwable.getMessage());
            }

            END_INVOKER.invoke(otelSpan);
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
