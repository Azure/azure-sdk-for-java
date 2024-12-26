// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel.tracing;

import io.clientcore.core.implementation.telemetry.otel.OTelInitializer;
import io.clientcore.core.telemetry.tracing.SpanContext;
import io.clientcore.core.util.ClientLogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.SPAN_CONTEXT_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.TRACE_FLAGS_CLASS;

public class OTelSpanContext implements SpanContext {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
    private static final OTelSpanContext INVALID;
    private static final ClientLogger LOGGER = new ClientLogger(OTelSpanContext.class);
    private static final MethodHandle GET_SPAN_ID_INVOKER;
    private static final MethodHandle GET_TRACE_ID_INVOKER;
    private static final MethodHandle GET_TRACE_FLAGS_INVOKER;

    private final Object otelSpanContext;
    static {
        MethodHandle getSpanIdInvoker = null;
        MethodHandle getTraceIdInvoker = null;
        MethodHandle getTraceFlagsInvoker = null;

        Object invalidInstance = null;

        if (OTelInitializer.isInitialized()) {
            try {
                getTraceIdInvoker
                    = LOOKUP.findVirtual(SPAN_CONTEXT_CLASS, "getTraceId", MethodType.methodType(String.class));
                getSpanIdInvoker
                    = LOOKUP.findVirtual(SPAN_CONTEXT_CLASS, "getSpanId", MethodType.methodType(String.class));
                getTraceFlagsInvoker
                    = LOOKUP.findVirtual(SPAN_CONTEXT_CLASS, "getTraceFlags", MethodType.methodType(TRACE_FLAGS_CLASS));

                MethodHandle getInvalidInvoker
                    = LOOKUP.findStatic(SPAN_CONTEXT_CLASS, "getInvalid", MethodType.methodType(SPAN_CONTEXT_CLASS));
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
