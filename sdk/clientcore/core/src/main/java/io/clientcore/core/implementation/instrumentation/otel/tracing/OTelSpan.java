// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel.tracing;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.instrumentation.otel.FallbackInvoker;
import io.clientcore.core.implementation.instrumentation.otel.OTelAttributeKey;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.util.ClientLogger;

import java.util.Objects;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.ATTRIBUTE_KEY_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.CONTEXT_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.SPAN_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.SPAN_CONTEXT_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.STATUS_CODE_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.tracing.OTelContext.markCoreSpan;
import static io.clientcore.core.implementation.instrumentation.otel.tracing.OTelSpanContext.INVALID_OTEL_SPAN_CONTEXT;

/**
 * OpenTelemetry implementation of {@link Span}.
 */
public class OTelSpan implements Span {
    private static final ClientLogger LOGGER = new ClientLogger(OTelSpan.class);
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
    private String errorType;

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

        ERROR_STATUS_CODE = errorStatusCode;
    }

    OTelSpan(Object otelSpan, Object otelParentContext, SpanKind spanKind) {
        this.otelSpan = otelSpan;
        this.isRecording = otelSpan != null && (boolean) IS_RECORDING_INVOKER.invoke(otelSpan);

        Object contextWithSpan = otelSpan != null ? storeInContext(otelSpan, otelParentContext) : otelParentContext;
        this.otelContext = markCoreSpan(contextWithSpan, spanKind);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public OTelSpan setAttribute(String key, Object value) {
        if (isInitialized() && isRecording) {
            SET_ATTRIBUTE_INVOKER.invoke(otelSpan, OTelAttributeKey.getKey(key, value),
                OTelAttributeKey.castAttributeValue(value));
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
        Objects.requireNonNull(throwable, "'throwable' cannot be null");
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
     * Gets span context.
     *
     * @return the span context.
     */
    public OTelSpanContext getSpanContext() {
        return isInitialized()
            ? new OTelSpanContext(GET_SPAN_CONTEXT_INVOKER.invoke(otelSpan))
            : OTelSpanContext.getInvalid();
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

    static Object createPropagatingSpan(Object otelContext) {
        assert CONTEXT_CLASS.isInstance(otelContext);

        Object span = FROM_CONTEXT_INVOKER.invoke(otelContext);
        assert SPAN_CLASS.isInstance(span);

        Object spanContext = GET_SPAN_CONTEXT_INVOKER.invoke(span);
        assert SPAN_CONTEXT_CLASS.isInstance(spanContext);

        Object propagatingSpan = WRAP_INVOKER.invoke(spanContext);
        assert SPAN_CLASS.isInstance(propagatingSpan);

        return propagatingSpan;
    }

    Object getOtelContext() {
        return otelContext;
    }

    private void endSpan(Throwable throwable) {
        if (isInitialized()) {
            if (errorType != null || throwable != null) {
                setAttribute("error.type", errorType != null ? errorType : throwable.getClass().getCanonicalName());
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

    private static Object storeInContext(Object otelSpan, Object otelContext) {
        Object updatedContext = STORE_IN_CONTEXT_INVOKER.invoke(otelSpan, otelContext);
        return updatedContext != null ? updatedContext : otelContext;
    }

    private boolean isInitialized() {
        return OTelInitializer.isInitialized() && otelSpan != null;
    }
}
