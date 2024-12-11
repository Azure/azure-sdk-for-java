package io.clientcore.core.observability;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;

public class SpanContext {

    static final Class<?> OTEL_SPAN_CONTEXT_CLASS;
    private volatile Object otelSpanContext;
    private final static SpanContext INVALID;
    private static final ReflectiveInvoker GET_SPAN_ID_INVOKER;
    private static final ReflectiveInvoker GET_TRACE_FLAGS_INVOKER;
    private static final ReflectiveInvoker GET_TRACE_STATE_INVOKER;
    private static final ReflectiveInvoker IS_REMOTE_INVOKER;
    private static final ReflectiveInvoker IS_SAMPLED_INVOKER;
    private static final ReflectiveInvoker CREATE_INVOKER;
    private static final ReflectiveInvoker CREATE_FROM_REMOTE_PARENT_INVOKER;

    static {
        Class<?> otelSpanContextClass;
        Class<?> otelTraceStateClass;

        ReflectiveInvoker getSpanIdInvoker;
        ReflectiveInvoker getTraceFlagsInvoker;
        ReflectiveInvoker getTraceStateInvoker;
        ReflectiveInvoker isRemoteInvoker;
        ReflectiveInvoker isSampledInvoker;
        ReflectiveInvoker createInvoker;
        ReflectiveInvoker createFromRemoteParentInvoker;

        Object invalidInstance;


        try {
            otelSpanContextClass = Class.forName("io.opentelemetry.api.trace.SpanContext", true, Span.class.getClassLoader());
            otelTraceStateClass = Class.forName("io.opentelemetry.api.trace.TraceState", true, Span.class.getClassLoader());

            ReflectiveInvoker getInvalidInvoker = ReflectionUtils.getMethodInvoker(otelSpanContextClass,
                otelSpanContextClass.getMethod("getInvalid"));

            // TODO: this is probably wrong (Tracestate)
            createInvoker = ReflectionUtils.getMethodInvoker(otelSpanContextClass,
                otelSpanContextClass.getMethod("create", String.class, String.class, TraceFlags.OTEL_TRACE_FLAGS_CLASS, otelTraceStateClass));
            createFromRemoteParentInvoker = ReflectionUtils.getMethodInvoker(otelSpanContextClass,
                otelSpanContextClass.getMethod("createFromRemoteParent", String.class, String.class, TraceFlags.OTEL_TRACE_FLAGS_CLASS, otelTraceStateClass));
            getSpanIdInvoker = ReflectionUtils.getMethodInvoker(otelSpanContextClass,
                otelSpanContextClass.getMethod("getSpanId"));
            getTraceFlagsInvoker = ReflectionUtils.getMethodInvoker(otelSpanContextClass,
                otelSpanContextClass.getMethod("getTraceFlags"));
            getTraceStateInvoker = ReflectionUtils.getMethodInvoker(otelSpanContextClass,
                otelSpanContextClass.getMethod("getTraceState"));
            isRemoteInvoker = ReflectionUtils.getMethodInvoker(otelSpanContextClass,
                otelSpanContextClass.getMethod("isRemote"));
            isSampledInvoker = ReflectionUtils.getMethodInvoker(otelSpanContextClass,
                otelSpanContextClass.getMethod("isSampled"));

            invalidInstance = getInvalidInvoker.invokeStatic();
        } catch (Exception e) {
            otelSpanContextClass = null;

            createInvoker = null;
            createFromRemoteParentInvoker = null;
            getSpanIdInvoker = null;
            getTraceFlagsInvoker = null;
            getTraceStateInvoker = null;
            isRemoteInvoker = null;
            isSampledInvoker = null;
            invalidInstance = null;
            // TODO
        }

        OTEL_SPAN_CONTEXT_CLASS = otelSpanContextClass;
        INVALID = new SpanContext(invalidInstance);
        CREATE_INVOKER = createInvoker;
        CREATE_FROM_REMOTE_PARENT_INVOKER = createFromRemoteParentInvoker;
        IS_SAMPLED_INVOKER = isSampledInvoker;
        GET_SPAN_ID_INVOKER = getSpanIdInvoker;
        GET_TRACE_FLAGS_INVOKER = getTraceFlagsInvoker;
        GET_TRACE_STATE_INVOKER = getTraceStateInvoker;
        IS_REMOTE_INVOKER = isRemoteInvoker;
    }

    SpanContext (Object otelSpanContext) {
        this.otelSpanContext = otelSpanContext;
    }

    static SpanContext getInvalid() {
        return INVALID;
    }

    static SpanContext create(
        String traceIdHex, String spanIdHex, TraceFlags traceFlags, Object traceState) {
        if (OTEL_SPAN_CONTEXT_CLASS == null) {
            return INVALID;
        }

        try {
            Object spanContext = CREATE_INVOKER.invokeStatic(
                traceIdHex,
                spanIdHex,
                traceFlags,
                traceState);
            return new SpanContext(spanContext);
        } catch (Throwable e) {
            // TODO log
            return INVALID;
        }
    }

    static SpanContext createFromRemoteParent(
        String traceIdHex, String spanIdHex, TraceFlags traceFlags, Object traceState) {
        if (OTEL_SPAN_CONTEXT_CLASS == null) {
            return INVALID;
        }

        try {
            Object spanContext = CREATE_FROM_REMOTE_PARENT_INVOKER.invokeStatic(
                traceIdHex,
                spanIdHex,
                traceFlags,
                traceState);
            return new SpanContext(spanContext);
        } catch (Throwable e) {
            // TODO log
            return INVALID;
        }
    }

    String getSpanId() {
        if (otelSpanContext == null || GET_SPAN_ID_INVOKER == null) {
            return null;
        }

        try {
            return (String) GET_SPAN_ID_INVOKER.invokeWithArguments(otelSpanContext);
        } catch (Throwable e) {
            // TODO log
            otelSpanContext = null;
            return null;
        }
    }

    boolean isSampled() {
        if (otelSpanContext == null || GET_TRACE_FLAGS_INVOKER == null) {
            return false;
        }

        try {
            return (boolean) IS_SAMPLED_INVOKER.invokeWithArguments(otelSpanContext);
        } catch (Throwable e) {
            // TODO log
            otelSpanContext = null;
            return false;
        }
    }

    TraceFlags getTraceFlags() {
        if (otelSpanContext == null || GET_TRACE_FLAGS_INVOKER == null) {
            return TraceFlags.getDefault();
        }

        try {
            Object traceFlags = GET_TRACE_FLAGS_INVOKER.invokeWithArguments(otelSpanContext);
            return TraceFlags.fromByte((byte) traceFlags);
        } catch (Throwable e) {
            // TODO log
            otelSpanContext = null;
            return TraceFlags.getDefault();
        }
    }

    Object getTraceState() {
        if (otelSpanContext == null || GET_TRACE_STATE_INVOKER == null) {
            return null;
        }

        try {
            return GET_TRACE_STATE_INVOKER.invokeWithArguments(otelSpanContext);
        } catch (Throwable e) {
            // TODO log
            otelSpanContext = null;
            return null;
        }
    }

    boolean isRemote() {
        if (otelSpanContext == null || IS_REMOTE_INVOKER == null) {
            return false;
        }

        try {
            return (boolean) IS_REMOTE_INVOKER.invokeWithArguments(otelSpanContext);
        } catch (Throwable e) {
            // TODO log
            otelSpanContext = null;
            return false;
        }
    }
}
