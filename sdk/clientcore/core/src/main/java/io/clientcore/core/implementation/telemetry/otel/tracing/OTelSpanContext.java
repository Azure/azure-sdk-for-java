// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel.tracing;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.telemetry.FallbackInvoker;
import io.clientcore.core.implementation.telemetry.otel.OTelInitializer;
import io.clientcore.core.telemetry.tracing.SpanContext;
import io.clientcore.core.util.ClientLogger;

import java.util.function.Consumer;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.SPAN_CONTEXT_CLASS;

/**
 * OpenTelemetry implementation of {@link SpanContext}.
 */
public class OTelSpanContext implements SpanContext {
    public static final Object INVALID_OTEL_SPAN_CONTEXT;
    private static final String INVALID_TRACE_ID = "00000000000000000000000000000000";
    private static final String INVALID_SPAN_ID = "0000000000000000";
    private static final String INVALID_TRACE_FLAGS = "00";
    private static final OTelSpanContext INVALID;
    private static final ClientLogger LOGGER = new ClientLogger(OTelSpanContext.class);
    private static final FallbackInvoker GET_SPAN_ID_INVOKER;
    private static final FallbackInvoker GET_TRACE_ID_INVOKER;
    private static final FallbackInvoker GET_TRACE_FLAGS_INVOKER;

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

        Consumer<Throwable> onError = t -> OTelInitializer.runtimeError(LOGGER, t);
        INVALID_OTEL_SPAN_CONTEXT = invalidInstance;
        INVALID = new OTelSpanContext(invalidInstance);
        GET_SPAN_ID_INVOKER = new FallbackInvoker(getSpanIdInvoker, INVALID_SPAN_ID, onError);
        GET_TRACE_ID_INVOKER = new FallbackInvoker(getTraceIdInvoker, INVALID_TRACE_ID, onError);
        GET_TRACE_FLAGS_INVOKER = new FallbackInvoker(getTraceFlagsInvoker, INVALID_TRACE_FLAGS, onError);
    }

    OTelSpanContext(Object otelSpanContext) {
        this.otelSpanContext = otelSpanContext;
    }

    static OTelSpanContext getInvalid() {
        return INVALID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTraceId() {
        return isInitialized() ? (String) GET_TRACE_ID_INVOKER.invoke(otelSpanContext) : INVALID_TRACE_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSpanId() {
        return isInitialized() ? (String) GET_SPAN_ID_INVOKER.invoke(otelSpanContext) : INVALID_SPAN_ID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTraceFlags() {
        if (isInitialized()) {
            Object traceFlags = GET_TRACE_FLAGS_INVOKER.invoke(otelSpanContext);
            if (traceFlags != null) {
                return traceFlags.toString();
            }
        }

        return INVALID_TRACE_FLAGS;
    }

    private boolean isInitialized() {
        return (OTelInitializer.isInitialized() && otelSpanContext != null);
    }
}
