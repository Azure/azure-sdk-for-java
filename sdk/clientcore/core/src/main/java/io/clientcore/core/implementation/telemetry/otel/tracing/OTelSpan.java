// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel.tracing;

import io.clientcore.core.implementation.telemetry.otel.OTelAttributeKey;
import io.clientcore.core.implementation.telemetry.otel.OTelInitializer;
import io.clientcore.core.telemetry.Scope;
import io.clientcore.core.telemetry.tracing.Span;
import io.clientcore.core.telemetry.tracing.SpanContext;
import io.clientcore.core.telemetry.tracing.SpanKind;
import io.clientcore.core.util.ClientLogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.ATTRIBUTE_KEY_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.CONTEXT_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.SPAN_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.SPAN_CONTEXT_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.STATUS_CODE_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.tracing.OTelContext.markCoreSpan;

public class OTelSpan implements Span {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
    private static final ClientLogger LOGGER = new ClientLogger(OTelSpan.class);
    private static final Scope NOOP_SCOPE = () -> {
    };
    private static final MethodHandle SET_ATTRIBUTE_INVOKER;
    private static final MethodHandle SET_STATUS_INVOKER;
    private static final MethodHandle END_INVOKER;
    private static final MethodHandle GET_SPAN_CONTEXT_INVOKER;
    private static final MethodHandle IS_RECORDING_INVOKER;
    private static final MethodHandle STORE_IN_CONTEXT_INVOKER;
    private static final MethodHandle FROM_CONTEXT_INVOKER;
    private static final MethodHandle WRAP_INVOKER;
    private static final Object ERROR_STATUS_CODE;
    private final Object otelSpan;
    private final Object otelContext;

    static {
        MethodHandle setAttributeInvoker = null;
        MethodHandle setStatusInvoker = null;
        MethodHandle endInvoker = null;
        MethodHandle getSpanContextInvoker = null;
        MethodHandle isRecordingInvoker = null;
        MethodHandle storeInContextInvoker = null;
        MethodHandle fromContextInvoker = null;
        MethodHandle wrapInvoker = null;

        Object errorStatusCode = null;

        if (OTelInitializer.isInitialized()) {
            try {
                setAttributeInvoker = LOOKUP.findVirtual(SPAN_CLASS, "setAttribute",
                    MethodType.methodType(SPAN_CLASS, ATTRIBUTE_KEY_CLASS, Object.class));

                setStatusInvoker = LOOKUP.findVirtual(SPAN_CLASS, "setStatus",
                    MethodType.methodType(SPAN_CLASS, STATUS_CODE_CLASS, String.class));
                endInvoker = LOOKUP.findVirtual(SPAN_CLASS, "end", MethodType.methodType(void.class));
                isRecordingInvoker
                    = LOOKUP.findVirtual(SPAN_CLASS, "isRecording", MethodType.methodType(boolean.class));
                getSpanContextInvoker
                    = LOOKUP.findVirtual(SPAN_CLASS, "getSpanContext", MethodType.methodType(SPAN_CONTEXT_CLASS));
                storeInContextInvoker = LOOKUP.findVirtual(SPAN_CLASS, "storeInContext",
                    MethodType.methodType(CONTEXT_CLASS, CONTEXT_CLASS));
                fromContextInvoker
                    = LOOKUP.findStatic(SPAN_CLASS, "fromContext", MethodType.methodType(SPAN_CLASS, CONTEXT_CLASS));

                wrapInvoker
                    = LOOKUP.findStatic(SPAN_CLASS, "wrap", MethodType.methodType(SPAN_CLASS, SPAN_CONTEXT_CLASS));
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

    OTelSpan(Object otelSpan, Object otelParentContext, SpanKind spanKind) throws Throwable {
        assert CONTEXT_CLASS.isInstance(otelParentContext);
        assert otelSpan == null || SPAN_CLASS.isInstance(otelSpan);

        this.otelSpan = otelSpan;

        Object contextWithSpan = otelSpan != null ? storeInContext(otelSpan, otelParentContext) : otelParentContext;
        this.otelContext = markCoreSpan(contextWithSpan, spanKind);
    }

    public OTelSpan setAttribute(String key, Object value) {
        if (OTelInitializer.isInitialized() && otelSpan != null) {
            try {
                SET_ATTRIBUTE_INVOKER.invoke(otelSpan, OTelAttributeKey.getKey(key, value),
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
                SET_STATUS_INVOKER.invoke(otelSpan, ERROR_STATUS_CODE, null);
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
                SET_STATUS_INVOKER.invoke(otelSpan, ERROR_STATUS_CODE, error.getMessage());
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
                END_INVOKER.invoke(otelSpan);
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }
    }

    @Override
    public SpanContext getSpanContext() {
        if (OTelInitializer.isInitialized() && otelSpan != null) {
            try {
                return new OTelSpanContext(GET_SPAN_CONTEXT_INVOKER.invoke(otelSpan));
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
                return (boolean) IS_RECORDING_INVOKER.invoke(otelSpan);
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

    static Object createPropagatingSpan(Object otelContext) throws Throwable {
        assert CONTEXT_CLASS.isInstance(otelContext);

        Object span = FROM_CONTEXT_INVOKER.invoke(otelContext);
        assert SPAN_CLASS.isInstance(span);

        Object spanContext = GET_SPAN_CONTEXT_INVOKER.invoke(span);
        assert SPAN_CONTEXT_CLASS.isInstance(spanContext);

        Object propagatingSpan = WRAP_INVOKER.invoke(spanContext);
        assert SPAN_CLASS.isInstance(propagatingSpan);

        return propagatingSpan;
    }

    Object getOtelContext()  {
        return otelContext;
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

    private static Object storeInContext(Object otelSpan, Object otelContext) throws Throwable {
        return STORE_IN_CONTEXT_INVOKER.invoke(otelSpan, otelContext);
    }
}
