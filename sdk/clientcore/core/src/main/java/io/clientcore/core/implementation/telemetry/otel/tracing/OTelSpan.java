// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel.tracing;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.telemetry.otel.OTelAttributeKey;
import io.clientcore.core.implementation.telemetry.otel.OTelInitializer;
import io.clientcore.core.telemetry.Scope;
import io.clientcore.core.telemetry.TelemetryProvider;
import io.clientcore.core.telemetry.tracing.Span;
import io.clientcore.core.telemetry.tracing.SpanContext;
import io.clientcore.core.telemetry.tracing.SpanKind;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.Context;

import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.ATTRIBUTE_KEY_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.CONTEXT_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.SPAN_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.SPAN_CONTEXT_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.STATUS_CODE_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.tracing.OTelContext.markCoreSpan;

public class OTelSpan implements Span {
    private static final ClientLogger LOGGER = new ClientLogger(OTelSpan.class);
    private static final Scope NOOP_SCOPE = () -> {
    };
    private static final ReflectiveInvoker SET_ATTRIBUTE_INVOKER;
    private static final ReflectiveInvoker SET_STATUS_INVOKER;
    private static final ReflectiveInvoker END_INVOKER;
    private static final ReflectiveInvoker GET_SPAN_CONTEXT_INVOKER;
    private static final ReflectiveInvoker IS_RECORDING_INVOKER;
    private static final ReflectiveInvoker STORE_IN_CONTEXT_INVOKER;
    private static final ReflectiveInvoker FROM_CONTEXT_INVOKER;
    private static final ReflectiveInvoker WRAP_INVOKER;
    private static final Object ERROR_STATUS_CODE;
    private final Object otelSpan;
    private final Object otelContext;

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
                setAttributeInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS,
                    SPAN_CLASS.getMethod("setAttribute", ATTRIBUTE_KEY_CLASS, Object.class));

                setStatusInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS,
                    SPAN_CLASS.getMethod("setStatus", STATUS_CODE_CLASS, String.class));

                endInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS, SPAN_CLASS.getMethod("end"));

                isRecordingInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS, SPAN_CLASS.getMethod("isRecording"));

                getSpanContextInvoker
                    = ReflectionUtils.getMethodInvoker(SPAN_CLASS, SPAN_CLASS.getMethod("getSpanContext"));

                storeInContextInvoker = ReflectionUtils.getMethodInvoker(SPAN_CLASS,
                    SPAN_CLASS.getMethod("storeInContext", CONTEXT_CLASS));

                fromContextInvoker
                    = ReflectionUtils.getMethodInvoker(SPAN_CLASS, SPAN_CLASS.getMethod("fromContext", CONTEXT_CLASS));

                wrapInvoker
                    = ReflectionUtils.getMethodInvoker(SPAN_CLASS, SPAN_CLASS.getMethod("wrap", SPAN_CONTEXT_CLASS));
                errorStatusCode = STATUS_CODE_CLASS.getField("ERROR").get(null);
            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        SET_ATTRIBUTE_INVOKER = setAttributeInvoker;
        SET_STATUS_INVOKER = setStatusInvoker;
        END_INVOKER = endInvoker;
        GET_SPAN_CONTEXT_INVOKER = getSpanContextInvoker;
        IS_RECORDING_INVOKER = isRecordingInvoker;
        STORE_IN_CONTEXT_INVOKER = storeInContextInvoker;
        ERROR_STATUS_CODE = errorStatusCode;
        FROM_CONTEXT_INVOKER = fromContextInvoker;
        WRAP_INVOKER = wrapInvoker;
    }

    OTelSpan(Object otelSpan, Object otelParentContext, SpanKind spanKind) throws Exception {
        assert CONTEXT_CLASS.isInstance(otelParentContext);
        assert otelSpan == null || SPAN_CLASS.isInstance(otelSpan);

        this.otelSpan = otelSpan;

        Object contextWithSpan = otelSpan != null ? storeInContext(otelSpan, otelParentContext) : otelParentContext;
        this.otelContext = markCoreSpan(contextWithSpan, spanKind);
    }

    public OTelSpan setAttribute(String key, Object value) {
        if (OTelInitializer.isInitialized() && otelSpan != null) {
            try {
                SET_ATTRIBUTE_INVOKER.invokeWithArguments(otelSpan, OTelAttributeKey.getKey(key, value),
                    OTelAttributeKey.castAttributeValue(value));
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }

        return this;
    }

    @Override
    public Span setError(String errorType) {
        setAttribute("error.type", errorType);
        if (OTelInitializer.isInitialized() && otelSpan != null) {
            try {
                SET_STATUS_INVOKER.invokeWithArguments(otelSpan, ERROR_STATUS_CODE, null);
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }

        return this;
    }

    @Override
    public Span setError(Throwable error) {
        setAttribute("error.type", error.getClass().getCanonicalName());
        if (OTelInitializer.isInitialized() && otelSpan != null) {
            try {
                SET_STATUS_INVOKER.invokeWithArguments(otelSpan, ERROR_STATUS_CODE, error.getMessage());
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }

        return this;
    }

    @Override
    public void end() {
        if (OTelInitializer.isInitialized() && otelSpan != null) {
            try {
                END_INVOKER.invokeWithArguments(otelSpan);
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }
    }

    @Override
    public SpanContext getSpanContext() {
        if (OTelInitializer.isInitialized() && otelSpan != null) {
            try {
                return new OTelSpanContext(GET_SPAN_CONTEXT_INVOKER.invokeWithArguments(otelSpan));
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }

        return OTelSpanContext.getInvalid();
    }

    @Override
    public boolean isRecording() {
        if (OTelInitializer.isInitialized() && otelSpan != null) {
            try {
                return (boolean) IS_RECORDING_INVOKER.invokeWithArguments(otelSpan);
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }

        return false;
    }

    @Override
    public Scope makeCurrent() {
        if (OTelInitializer.isInitialized() && otelSpan != null) {
            try {
                return wrapOTelScope(OTelContext.makeCurrent(otelContext));
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }

        return NOOP_SCOPE;
    }

    @Override
    public Context storeInContext(Context context) {
        return context.put(TelemetryProvider.TRACE_CONTEXT_KEY, otelContext);
    }

    static Object createPropagatingSpan(Object otelContext) throws Exception {
        assert CONTEXT_CLASS.isInstance(otelContext);

        Object span = FROM_CONTEXT_INVOKER.invokeWithArguments(otelContext);
        assert SPAN_CLASS.isInstance(span);

        Object spanContext = GET_SPAN_CONTEXT_INVOKER.invokeWithArguments(span);
        assert SPAN_CONTEXT_CLASS.isInstance(spanContext);

        Object propagatingSpan = WRAP_INVOKER.invokeStatic(spanContext);
        assert SPAN_CLASS.isInstance(propagatingSpan);

        return propagatingSpan;
    }

    private static Scope wrapOTelScope(AutoCloseable otelScope) {
        return () -> {
            try {
                otelScope.close();
            } catch (Exception e) {
                OTelInitializer.runtimeError(LOGGER, e);
            }
        };
    }

    private static Object storeInContext(Object otelSpan, Object otelContext) throws Exception {
        return STORE_IN_CONTEXT_INVOKER.invokeWithArguments(otelSpan, otelContext);
    }
}
