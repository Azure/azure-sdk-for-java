// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel.tracing;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.instrumentation.otel.FallbackInvoker;
import io.clientcore.core.implementation.instrumentation.otel.OTelContext;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.tracing.Span;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.SPAN_CONTEXT_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.TRACE_FLAGS_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.TRACE_STATE_CLASS;

/**
 * Wrapper around OpenTelemetry SpanContext.
 */
public class OTelSpanContext implements InstrumentationContext {
    public static final Object INVALID_OTEL_SPAN_CONTEXT;
    private static final String INVALID_TRACE_ID = "00000000000000000000000000000000";
    private static final String INVALID_SPAN_ID = "0000000000000000";
    private static final String INVALID_TRACE_FLAGS = "00";
    private static final OTelSpanContext INVALID;
    private static final ClientLogger LOGGER = new ClientLogger(OTelSpanContext.class);
    private static final FallbackInvoker GET_SPAN_ID_INVOKER;
    private static final FallbackInvoker GET_TRACE_ID_INVOKER;
    private static final FallbackInvoker GET_TRACE_FLAGS_INVOKER;
    private static final FallbackInvoker IS_VALID_INVOKER;
    private static final FallbackInvoker CREATE_INVOKER;

    private final Object otelSpanContext;
    private final Object otelContext;
    private String traceId;
    private String spanId;
    private String traceFlags;
    private Boolean isValid;

    static {
        ReflectiveInvoker getSpanIdInvoker = null;
        ReflectiveInvoker getTraceIdInvoker = null;
        ReflectiveInvoker getTraceFlagsInvoker = null;
        ReflectiveInvoker isValidInvoker = null;
        ReflectiveInvoker createInvoker = null;

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
                isValidInvoker = getMethodInvoker(SPAN_CONTEXT_CLASS, SPAN_CONTEXT_CLASS.getMethod("isValid"));
                createInvoker = getMethodInvoker(SPAN_CONTEXT_CLASS, SPAN_CONTEXT_CLASS.getMethod("create",
                    String.class, String.class, TRACE_FLAGS_CLASS, TRACE_STATE_CLASS));
            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        INVALID_OTEL_SPAN_CONTEXT = invalidInstance;
        INVALID = new OTelSpanContext(invalidInstance, null);
        IS_VALID_INVOKER = new FallbackInvoker(isValidInvoker, false, LOGGER);
        GET_SPAN_ID_INVOKER = new FallbackInvoker(getSpanIdInvoker, INVALID_SPAN_ID, LOGGER);
        GET_TRACE_ID_INVOKER = new FallbackInvoker(getTraceIdInvoker, INVALID_TRACE_ID, LOGGER);
        GET_TRACE_FLAGS_INVOKER = new FallbackInvoker(getTraceFlagsInvoker, INVALID_TRACE_FLAGS, LOGGER);
        CREATE_INVOKER = new FallbackInvoker(createInvoker, INVALID_OTEL_SPAN_CONTEXT, LOGGER);
    }

    /**
     * Creates a new instance of {@link OTelSpanContext} from an OpenTelemetry {@code SpanContext}.
     *
     * @param otelSpanContext the instance of OpenTelemetry {@code io.opentelemetry.api.trace.SpanContext}
     * @param otelContext the instance of OpenTelemetry {@code io.opentelemetry.context.Context}.
     *                    It is used to propagate additional information within the process along with {@link InstrumentationContext}.
     */
    public OTelSpanContext(Object otelSpanContext, Object otelContext) {
        this.otelSpanContext = otelSpanContext;
        this.otelContext = otelContext;
    }

    /**
     * Creates a new instance of {@link OTelSpanContext} from an OpenTelemetry {@code io.opentelemetry.context.Context}.
     * @param otelContext the instance of OpenTelemetry {@code io.opentelemetry.context.Context}
     *
     * @return the instance of {@link OTelSpanContext}
     */
    public static OTelSpanContext fromOTelContext(Object otelContext) {
        if (otelContext == null) {
            return INVALID;
        }
        Object otelSpan = OTelSpan.fromOTelContext(otelContext);
        Object otelSpanContext = OTelSpan.getSpanContext(otelSpan);
        return new OTelSpanContext(otelSpanContext, otelContext);
    }

    /**
     * Creates a new instance of {@link OTelSpanContext} from an OpenTelemetry {@code io.opentelemetry.api.trace.Span}.
     * @param otelSpan the instance of OpenTelemetry {@code io.opentelemetry.api.trace.Span}
     * @return the instance of {@link OTelSpanContext}
     */
    public static OTelSpanContext fromOTelSpan(Object otelSpan) {
        Object otelSpanContext = OTelSpan.getSpanContext(otelSpan);
        Object otelContext = OTelSpan.storeInContext(otelSpan, OTelContext.getCurrent());

        return new OTelSpanContext(otelSpanContext, otelContext);
    }

    /**
     * Returns an invalid instance of {@link OTelSpanContext}.
     * @return the instance of {@link OTelSpanContext}
     */
    public static OTelSpanContext getInvalid() {
        return INVALID;
    }

    /**
     * {@inheritDoc}
     */
    public String getTraceId() {
        if (traceId != null) {
            return traceId;
        }

        traceId = isInitialized() ? (String) GET_TRACE_ID_INVOKER.invoke(otelSpanContext) : INVALID_TRACE_ID;
        return traceId;
    }

    /**
     * {@inheritDoc}
     */
    public String getSpanId() {
        if (spanId != null) {
            return spanId;
        }

        spanId = isInitialized() ? (String) GET_SPAN_ID_INVOKER.invoke(otelSpanContext) : INVALID_SPAN_ID;
        return spanId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTraceFlags() {
        if (traceFlags != null) {
            return traceFlags;
        }

        if (isInitialized()) {
            Object traceFlagsObj = GET_TRACE_FLAGS_INVOKER.invoke(otelSpanContext);
            if (traceFlagsObj != null) {
                traceFlags = traceFlagsObj.toString();
            }
        } else {
            traceFlags = INVALID_TRACE_FLAGS;
        }

        return traceFlags;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isValid() {
        if (isValid != null) {
            return isValid;
        }

        isValid = isInitialized() && (Boolean) IS_VALID_INVOKER.invoke(otelSpanContext);
        return isValid;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span getSpan() {
        if (isInitialized()) {
            if (otelContext != null) {
                OTelSpan coreSpan = OTelContext.getClientCoreSpan(otelContext);
                if (coreSpan != null) {
                    return coreSpan;
                }
            }

            return OTelSpan.createPropagatingSpan(this);
        }
        return Span.noop();
    }

    /**
     * Gets the OpenTelemetry context.
     *
     * @return The OpenTelemetry context.
     */
    public Object getOtelContext() {
        return otelContext;
    }

    Object getOtelSpanContext() {
        return otelSpanContext;
    }

    /**
     * Converts the given {@link InstrumentationContext} to an OpenTelemetry {@code SpanContext}.
     *
     * @param context The {@link InstrumentationContext} to convert.
     * @return The OpenTelemetry {@code SpanContext}.
     */
    public static Object toOTelSpanContext(InstrumentationContext context) {
        if (context instanceof OTelSpanContext) {
            return ((OTelSpanContext) context).otelSpanContext;
        }

        return CREATE_INVOKER.invoke(context.getTraceId(), context.getTraceFlags(), null);
    }

    private boolean isInitialized() {
        return otelSpanContext != null && OTelInitializer.isInitialized();
    }
}
