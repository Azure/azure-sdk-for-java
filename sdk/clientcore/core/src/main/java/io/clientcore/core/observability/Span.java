package io.clientcore.core.observability;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.util.Context;

public class Span {
    static final Class<?> OTEL_SPAN_CLASS;
    private final static Span INVALID;
    private static ReflectiveInvoker FROM_CONTEXT_INVOKER;
    private final static ReflectiveInvoker SET_ATTRIBUTE_INVOKER;
    private final static ReflectiveInvoker SET_STATUS_INVOKER;
    private final static ReflectiveInvoker ADD_LINK_INVOKER;
    private final static ReflectiveInvoker END_INVOKER;
    private final static ReflectiveInvoker GET_SPAN_CONTEXT_INVOKER;
    private final static ReflectiveInvoker IS_RECORDING_INVOKER;

    private volatile Object otelSpan;
    static {
        Class<?> otelSpanClass;
        Class<?> otelContextClass;

        ReflectiveInvoker fromContextInvoker;
        ReflectiveInvoker getInvalidInvoker;
        ReflectiveInvoker setAttributeInvoker;
        ReflectiveInvoker setStatusInvoker;
        ReflectiveInvoker addLinkInvoker;
        ReflectiveInvoker endInvoker;
        ReflectiveInvoker getSpanContextInvoker;
        ReflectiveInvoker isRecordingInvoker;

        Object invalidInstance;

        try {
            otelSpanClass = Class.forName("io.opentelemetry.api.trace.Span", true, Span.class.getClassLoader());
            otelContextClass = Class.forName("io.opentelemetry.context.Context", true, Span.class.getClassLoader());

            fromContextInvoker = ReflectionUtils.getMethodInvoker(otelSpanClass,
                otelSpanClass.getMethod("fromContext", otelContextClass));

            getInvalidInvoker = ReflectionUtils.getMethodInvoker(otelSpanClass,
                otelSpanClass.getMethod("getInvalid"));

            invalidInstance = getInvalidInvoker.invokeStatic();

            setAttributeInvoker = ReflectionUtils.getMethodInvoker(otelSpanClass,
                otelSpanClass.getMethod("setAttribute", AttributeKey.OTEL_ATTRIBUTE_KEY_CLASS, Object.class));

            setStatusInvoker = ReflectionUtils.getMethodInvoker(otelSpanClass,
                otelSpanClass.getMethod("setStatus", StatusCode.OTEL_STATUS_CODE_CLASS, String.class));

            addLinkInvoker = ReflectionUtils.getMethodInvoker(otelSpanClass,
                otelSpanClass.getMethod("addLink", SpanContext.OTEL_SPAN_CONTEXT_CLASS, Attributes.OTEL_ATTRIBUTES_CLASS));

            endInvoker = ReflectionUtils.getMethodInvoker(otelSpanClass,
                otelSpanClass.getMethod("end"));

            isRecordingInvoker = ReflectionUtils.getMethodInvoker(otelSpanClass,
                otelSpanClass.getMethod("isRecording"));

            getSpanContextInvoker = ReflectionUtils.getMethodInvoker(otelSpanClass,
                otelSpanClass.getMethod("getSpanContext"));
        } catch (Exception e) {
            otelSpanClass = null;
            fromContextInvoker = null;
            getSpanContextInvoker = null;
            setAttributeInvoker = null;
            setStatusInvoker = null;
            addLinkInvoker = null;
            endInvoker = null;
            isRecordingInvoker = null;
            invalidInstance = null;

            // TODO
        }

        OTEL_SPAN_CLASS = otelSpanClass;
        INVALID = new Span(invalidInstance);
        FROM_CONTEXT_INVOKER = fromContextInvoker;
        SET_ATTRIBUTE_INVOKER = setAttributeInvoker;
        SET_STATUS_INVOKER = setStatusInvoker;
        ADD_LINK_INVOKER = addLinkInvoker;
        END_INVOKER = endInvoker;
        GET_SPAN_CONTEXT_INVOKER = getSpanContextInvoker;
        IS_RECORDING_INVOKER = isRecordingInvoker;
    }

    Span(Object otelSpan) {
        this.otelSpan = otelSpan;
    }

    // do we need it?
    static Span fromContext(Context context) {
        if (FROM_CONTEXT_INVOKER == null) {
            return INVALID;
        }

        try {
            Object otelSpan = FROM_CONTEXT_INVOKER.invokeStatic(context);
            return new Span(otelSpan);
        } catch (Throwable e) {
            // TODO log
            FROM_CONTEXT_INVOKER = null;
            return INVALID;
        }
    }

    static Span getInvalid() {
        return INVALID;
    }

    public <T> Span setAttribute(AttributeKey<T> key, T value) {
        if (otelSpan == null || SET_ATTRIBUTE_INVOKER == null) {
            return this;
        }

        try {
            SET_ATTRIBUTE_INVOKER.invokeWithArguments(otelSpan, key, value);
        } catch (Throwable e) {
            // TODO log
        }

        return this;
    }

    public Span setStatus(StatusCode statusCode, String description) {
        if (otelSpan == null || SET_STATUS_INVOKER == null) {
            return this;
        }

        try {
            SET_STATUS_INVOKER.invokeWithArguments(otelSpan, statusCode.getOtelStatusCode(), description);
        } catch (Throwable e) {
            // TODO log
        }

        return this;
    }

    Span addLink(SpanContext spanContext, Attributes attributes) {
        if (otelSpan == null || ADD_LINK_INVOKER == null) {
            return this;
        }

        try {
            ADD_LINK_INVOKER.invokeWithArguments(otelSpan, spanContext, attributes);
        } catch (Throwable e) {
            // TODO log
            otelSpan = null;
        }

        return this;
    }

    public void end() {
        if (otelSpan == null || END_INVOKER == null) {
            return;
        }

        try {
            END_INVOKER.invokeWithArguments(otelSpan);
        } catch (Throwable e) {
            // TODO log
            otelSpan = null;
        }
    }

    SpanContext getSpanContext() {
        if (otelSpan == null || GET_SPAN_CONTEXT_INVOKER == null) {
            return SpanContext.getInvalid();
        }

        try {
            Object otelSpanContext = GET_SPAN_CONTEXT_INVOKER.invokeWithArguments(otelSpan);
            return new SpanContext(otelSpanContext);
        } catch (Throwable e) {
            // TODO log
            otelSpan = null;
            return SpanContext.getInvalid();
        }
    }

    public boolean isRecording() {
        if (otelSpan == null || IS_RECORDING_INVOKER == null) {
            return false;
        }

        try {
            return (boolean) IS_RECORDING_INVOKER.invokeWithArguments(otelSpan);
        } catch (Throwable e) {
            // TODO log
            otelSpan = null;
            return false;
        }
    }
}
