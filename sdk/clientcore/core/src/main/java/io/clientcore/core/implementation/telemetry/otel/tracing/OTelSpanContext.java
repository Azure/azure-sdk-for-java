// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel.tracing;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.telemetry.otel.OTelInitializer;
import io.clientcore.core.telemetry.tracing.SpanContext;
import io.clientcore.core.util.ClientLogger;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.SPAN_CONTEXT_CLASS;

public class OTelSpanContext implements SpanContext {
    private static final OTelSpanContext INVALID;
    private static final ClientLogger LOGGER = new ClientLogger(OTelSpanContext.class);
    private static final ReflectiveInvoker GET_SPAN_ID_INVOKER;
    private static final ReflectiveInvoker GET_TRACE_ID_INVOKER;
    private static final ReflectiveInvoker GET_TRACE_FLAGS_INVOKER;

    private final Object otelSpanContext;
    static {
        ReflectiveInvoker getSpanIdInvoker = null;
        ReflectiveInvoker getTraceIdInvoker = null;
        ReflectiveInvoker getTraceFlagsInvoker = null;

        Object invalidInstance = null;

        if (OTelInitializer.isInitialized()) {
            try {
                getTraceIdInvoker = getMethodInvoker(SPAN_CONTEXT_CLASS, SPAN_CONTEXT_CLASS.getMethod("getTraceId"));

                getSpanIdInvoker = getMethodInvoker(SPAN_CONTEXT_CLASS, SPAN_CONTEXT_CLASS.getMethod("getSpanId"));

                getTraceFlagsInvoker
                    = getMethodInvoker(SPAN_CONTEXT_CLASS, SPAN_CONTEXT_CLASS.getMethod("getTraceFlags"));

                ReflectiveInvoker getInvalidInvoker
                    = getMethodInvoker(SPAN_CONTEXT_CLASS, SPAN_CONTEXT_CLASS.getMethod("getInvalid"));

                invalidInstance = getInvalidInvoker.invoke();
            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        INVALID = new OTelSpanContext(invalidInstance);
        GET_SPAN_ID_INVOKER = getSpanIdInvoker;
        GET_TRACE_ID_INVOKER = getTraceIdInvoker;
        GET_TRACE_FLAGS_INVOKER = getTraceFlagsInvoker;
    }

    OTelSpanContext(Object otelSpanContext) {
        this.otelSpanContext = otelSpanContext;
    }

    static OTelSpanContext getInvalid() {
        return INVALID;
    }

    @Override
    public String getTraceId() {
        if (OTelInitializer.isInitialized() && otelSpanContext != null) {
            try {
                return (String) GET_TRACE_ID_INVOKER.invoke(otelSpanContext);
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }

        return "00000000000000000000000000000000";
    }

    @Override
    public String getSpanId() {
        if (OTelInitializer.isInitialized() && otelSpanContext != null) {
            try {
                return (String) GET_SPAN_ID_INVOKER.invoke(otelSpanContext);
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }

        return "0000000000000000";
    }

    @Override
    public String getTraceFlags() {
        if (OTelInitializer.isInitialized() && otelSpanContext != null) {
            try {
                Object otelTraceFlags = GET_TRACE_FLAGS_INVOKER.invoke(otelSpanContext);
                return otelTraceFlags == null ? null : otelTraceFlags.toString();
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }

        return "00";
    }
}
