// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.observability.otel.tracing;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.observability.otel.OTelInitializer;
import io.clientcore.core.observability.tracing.SpanContext;
import io.clientcore.core.util.ClientLogger;

import static io.clientcore.core.implementation.observability.otel.OTelInitializer.SPAN_CONTEXT_CLASS;
import static io.clientcore.core.implementation.observability.otel.OTelInitializer.TRACE_FLAGS_CLASS;

public class OTelSpanContext implements SpanContext {
    private static final OTelSpanContext INVALID;
    private static final ClientLogger LOGGER = new ClientLogger(OTelSpanContext.class);
    private static final ReflectiveInvoker GET_SPAN_ID_INVOKER;
    private static final ReflectiveInvoker GET_TRACE_ID_INVOKER;
    private static final ReflectiveInvoker GET_TRACE_FLAGS_INVOKER;

    private static final String DEFAULT_TRACE_FLAGS;
    private final Object otelSpanContext;
    static {
        ReflectiveInvoker getSpanIdInvoker = null;
        ReflectiveInvoker getTraceIdInvoker = null;
        ReflectiveInvoker getTraceFlagsInvoker = null;

        Object invalidInstance = null;
        String defaultTraceFlags = null;

        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                ReflectiveInvoker getInvalidInvoker
                    = ReflectionUtils.getMethodInvoker(SPAN_CONTEXT_CLASS, SPAN_CONTEXT_CLASS.getMethod("getInvalid"));

                getTraceIdInvoker
                    = ReflectionUtils.getMethodInvoker(SPAN_CONTEXT_CLASS, SPAN_CONTEXT_CLASS.getMethod("getTraceId"));
                getSpanIdInvoker
                    = ReflectionUtils.getMethodInvoker(SPAN_CONTEXT_CLASS, SPAN_CONTEXT_CLASS.getMethod("getSpanId"));
                getTraceFlagsInvoker = ReflectionUtils.getMethodInvoker(SPAN_CONTEXT_CLASS,
                    SPAN_CONTEXT_CLASS.getMethod("getTraceFlags"));

                ReflectiveInvoker getDefaultTraceFlagsInvoker
                    = ReflectionUtils.getMethodInvoker(TRACE_FLAGS_CLASS, TRACE_FLAGS_CLASS.getMethod("getDefault"));
                defaultTraceFlags = getDefaultTraceFlagsInvoker.invokeStatic().toString();
                invalidInstance = getInvalidInvoker.invokeStatic();
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.initError(LOGGER, t);
            }
        }

        INVALID = new OTelSpanContext(invalidInstance);
        GET_SPAN_ID_INVOKER = getSpanIdInvoker;
        GET_TRACE_ID_INVOKER = getTraceIdInvoker;
        GET_TRACE_FLAGS_INVOKER = getTraceFlagsInvoker;
        DEFAULT_TRACE_FLAGS = defaultTraceFlags;
    }

    OTelSpanContext(Object otelSpanContext) {
        this.otelSpanContext = otelSpanContext;
    }

    static OTelSpanContext getInvalid() {
        return INVALID;
    }

    @Override
    public String getTraceId() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpanContext != null) {
            try {
                return (String) GET_TRACE_ID_INVOKER.invokeWithArguments(otelSpanContext);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return null;
    }

    @Override
    public String getSpanId() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpanContext != null) {
            try {
                return (String) GET_SPAN_ID_INVOKER.invokeWithArguments(otelSpanContext);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return null;
    }

    @Override
    public String getTraceFlags() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpanContext != null) {
            try {
                Object otelTraceFlags = GET_TRACE_FLAGS_INVOKER.invokeWithArguments(otelSpanContext);
                return otelTraceFlags == null ? null : otelTraceFlags.toString();
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return DEFAULT_TRACE_FLAGS;
    }
}
