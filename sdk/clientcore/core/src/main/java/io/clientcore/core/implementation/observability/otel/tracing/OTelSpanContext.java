package io.clientcore.core.implementation.observability.otel.tracing;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.observability.otel.OTelInitializer;
import io.clientcore.core.observability.tracing.SpanContext;
import io.clientcore.core.util.ClientLogger;

import static io.clientcore.core.implementation.observability.otel.OTelInitializer.SPAN_CONTEXT_CLASS;
import static io.clientcore.core.implementation.observability.otel.OTelInitializer.TRACE_FLAGS_CLASS;
import static io.clientcore.core.implementation.observability.otel.OTelInitializer.TRACE_STATE_CLASS;

public class OTelSpanContext implements SpanContext {
    private final static OTelSpanContext INVALID;
    private static final ClientLogger LOGGER = new ClientLogger(OTelSpanContext.class);
    private static final ReflectiveInvoker GET_SPAN_ID_INVOKER;
    private static final ReflectiveInvoker GET_TRACE_ID_INVOKER;
    private static final ReflectiveInvoker GET_TRACE_FLAGS_INVOKER;
    private static final ReflectiveInvoker GET_TRACE_STATE_INVOKER;
    private static final ReflectiveInvoker IS_REMOTE_INVOKER;
    private static final ReflectiveInvoker IS_SAMPLED_INVOKER;
    private static final ReflectiveInvoker CREATE_INVOKER;
    private static final ReflectiveInvoker CREATE_FROM_REMOTE_PARENT_INVOKER;

    private final Object otelSpanContext;
    static {
        ReflectiveInvoker getSpanIdInvoker = null;
        ReflectiveInvoker getTraceIdInvoker = null;
        ReflectiveInvoker getTraceFlagsInvoker = null;
        ReflectiveInvoker getTraceStateInvoker = null;
        ReflectiveInvoker isRemoteInvoker = null;
        ReflectiveInvoker isSampledInvoker = null;
        ReflectiveInvoker createInvoker = null;
        ReflectiveInvoker createFromRemoteParentInvoker = null;

        Object invalidInstance = null;

        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                ReflectiveInvoker getInvalidInvoker = ReflectionUtils.getMethodInvoker(SPAN_CONTEXT_CLASS,
                    SPAN_CONTEXT_CLASS.getMethod("getInvalid"));

                // TODO: this is probably wrong (Tracestate)
                createInvoker = ReflectionUtils.getMethodInvoker(SPAN_CONTEXT_CLASS,
                    SPAN_CONTEXT_CLASS.getMethod("create", String.class, String.class, TRACE_FLAGS_CLASS, TRACE_STATE_CLASS));
                createFromRemoteParentInvoker = ReflectionUtils.getMethodInvoker(SPAN_CONTEXT_CLASS,
                    SPAN_CONTEXT_CLASS.getMethod("createFromRemoteParent", String.class, String.class, TRACE_FLAGS_CLASS, TRACE_STATE_CLASS));
                getTraceIdInvoker = ReflectionUtils.getMethodInvoker(SPAN_CONTEXT_CLASS,
                    SPAN_CONTEXT_CLASS.getMethod("getTraceId"));
                getSpanIdInvoker = ReflectionUtils.getMethodInvoker(SPAN_CONTEXT_CLASS,
                    SPAN_CONTEXT_CLASS.getMethod("getSpanId"));
                getTraceFlagsInvoker = ReflectionUtils.getMethodInvoker(SPAN_CONTEXT_CLASS,
                    SPAN_CONTEXT_CLASS.getMethod("getTraceFlags"));
                getTraceStateInvoker = ReflectionUtils.getMethodInvoker(SPAN_CONTEXT_CLASS,
                    SPAN_CONTEXT_CLASS.getMethod("getTraceState"));
                isRemoteInvoker = ReflectionUtils.getMethodInvoker(SPAN_CONTEXT_CLASS,
                    SPAN_CONTEXT_CLASS.getMethod("isRemote"));
                isSampledInvoker = ReflectionUtils.getMethodInvoker(SPAN_CONTEXT_CLASS,
                    SPAN_CONTEXT_CLASS.getMethod("isSampled"));

                invalidInstance = getInvalidInvoker.invokeStatic();
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.initError(LOGGER, t);
            }
        }

        INVALID = new OTelSpanContext(invalidInstance);
        CREATE_INVOKER = createInvoker;
        CREATE_FROM_REMOTE_PARENT_INVOKER = createFromRemoteParentInvoker;
        IS_SAMPLED_INVOKER = isSampledInvoker;
        GET_SPAN_ID_INVOKER = getSpanIdInvoker;
        GET_TRACE_ID_INVOKER = getTraceIdInvoker;
        GET_TRACE_FLAGS_INVOKER = getTraceFlagsInvoker;
        GET_TRACE_STATE_INVOKER = getTraceStateInvoker;
        IS_REMOTE_INVOKER = isRemoteInvoker;
    }

    OTelSpanContext (Object otelSpanContext) {
        this.otelSpanContext = otelSpanContext;
    }

    static OTelSpanContext getInvalid() {
        return INVALID;
    }

    static OTelSpanContext create(String traceIdHex, String spanIdHex, TraceFlags traceFlags, Object traceState) {
        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                Object spanContext = CREATE_INVOKER.invokeStatic(
                    traceIdHex,
                    spanIdHex,
                    traceFlags,
                    traceState);
                return new OTelSpanContext(spanContext);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }
        return INVALID;
    }

    static OTelSpanContext createFromRemoteParent(
            String traceIdHex, String spanIdHex, TraceFlags traceFlags, Object traceState) {
        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                Object spanContext = CREATE_FROM_REMOTE_PARENT_INVOKER.invokeStatic(
                    traceIdHex,
                    spanIdHex,
                    traceFlags,
                    traceState);
                return new OTelSpanContext(spanContext);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

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
    public boolean isSampled() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpanContext != null) {
            try {
                return (boolean) IS_SAMPLED_INVOKER.invokeWithArguments(otelSpanContext);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return false;
    }

    /*@Override
    public TraceFlags getTraceFlags() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpanContext != null) {
            try {
                Object traceFlags = GET_TRACE_FLAGS_INVOKER.invokeWithArguments(otelSpanContext);
                return TraceFlags.fromByte((byte) traceFlags);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return TraceFlags.getDefault();
    }

    @Override
    public Object getTraceState() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpanContext != null) {
            try {
                return GET_TRACE_STATE_INVOKER.invokeWithArguments(otelSpanContext);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return null;
    }*/

    @Override
    public boolean isRemote() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpanContext != null) {
            try {
                return (boolean) IS_REMOTE_INVOKER.invokeWithArguments(otelSpanContext);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return false;
    }
}
