package io.clientcore.core.observability;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.util.Context;

public class SpanBuilder {
    static final Class<?> OTEL_SPAN_BUILDER_CLASS;

    private static final Span NOOP_SPAN = new Span(null);
    private volatile Object otelSpanBuilder;

    private static final ReflectiveInvoker SET_PARENT_INVOKER;
    private static final ReflectiveInvoker ADD_LINK_INVOKER;
    private static final ReflectiveInvoker SET_ATTRIBUTE_INVOKER;
    private static final ReflectiveInvoker SET_SPAN_KIND_INVOKER;
    private static final ReflectiveInvoker START_SPAN_INVOKER;

    static {
        Class<?> otelSpanBuilderClass;
        Class<?> otelContextClass;

        ReflectiveInvoker setParentInvoker;
        ReflectiveInvoker addLinkInvoker;
        ReflectiveInvoker setAttributeInvoker;
        ReflectiveInvoker setSpanKindInvoker;
        ReflectiveInvoker startSpanInvoker;

        try {
            otelSpanBuilderClass = Class.forName("io.opentelemetry.api.trace.SpanBuilder", true, SpanBuilder.class.getClassLoader());
            otelContextClass = Class.forName("io.opentelemetry.context.Context", true, SpanBuilder.class.getClassLoader());


            setParentInvoker = ReflectionUtils.getMethodInvoker(otelSpanBuilderClass,
                otelSpanBuilderClass.getMethod("setParent", otelContextClass));

            addLinkInvoker = ReflectionUtils.getMethodInvoker(otelSpanBuilderClass,
                otelSpanBuilderClass.getMethod("addLink", SpanContext.OTEL_SPAN_CONTEXT_CLASS, Attributes.OTEL_ATTRIBUTES_CLASS));

            setAttributeInvoker = ReflectionUtils.getMethodInvoker(otelSpanBuilderClass,
                otelSpanBuilderClass.getMethod("setAttribute", AttributeKey.OTEL_ATTRIBUTE_KEY_CLASS, Object.class));

            setSpanKindInvoker = ReflectionUtils.getMethodInvoker(otelSpanBuilderClass,
                otelSpanBuilderClass.getMethod("setSpanKind", SpanKind.OTEL_SPAN_KIND_CLASS));

            startSpanInvoker = ReflectionUtils.getMethodInvoker(otelSpanBuilderClass,
                otelSpanBuilderClass.getMethod("startSpan"));
        } catch (Exception e) {
            otelSpanBuilderClass = null;
            setParentInvoker = null;
            addLinkInvoker = null;
            setSpanKindInvoker = null;
            startSpanInvoker = null;
            setAttributeInvoker = null;

            // TODO
        }

        OTEL_SPAN_BUILDER_CLASS = otelSpanBuilderClass;
        SET_PARENT_INVOKER = setParentInvoker;
        ADD_LINK_INVOKER = addLinkInvoker;
        SET_ATTRIBUTE_INVOKER = setAttributeInvoker;
        SET_SPAN_KIND_INVOKER = setSpanKindInvoker;
        START_SPAN_INVOKER = startSpanInvoker;
    }

    SpanBuilder(Object otelSpanBuilder) {
        this.otelSpanBuilder = otelSpanBuilder;
    }

    public SpanBuilder setParent(Context context) {
        if (otelSpanBuilder == null || SET_PARENT_INVOKER == null) {
            return this;
        }

        try {
            SET_PARENT_INVOKER.invokeWithArguments(otelSpanBuilder, context);
        } catch (Throwable e) {
            // TODO log
            otelSpanBuilder = null;
        }

        return this;
    }

    public SpanBuilder addLink(SpanContext spanContext, Attributes attributes) {
        if (otelSpanBuilder == null || ADD_LINK_INVOKER == null) {
            return this;
        }

        try {
            ADD_LINK_INVOKER.invokeWithArguments(otelSpanBuilder, spanContext, attributes);
        } catch (Throwable e) {
            // TODO log
            otelSpanBuilder = null;
        }

        return this;
    }


    public <T> SpanBuilder setAttribute(AttributeKey<T> key, T value) {
        if (otelSpanBuilder == null || SET_ATTRIBUTE_INVOKER == null) {
            return this;
        }

        try {
            SET_ATTRIBUTE_INVOKER.invokeWithArguments(otelSpanBuilder, key.otelKey(), value);
        } catch (Throwable e) {
            // TODO log
            otelSpanBuilder = null;
        }

        return this;
    }

    public SpanBuilder setSpanKind(SpanKind spanKind) {
        if (otelSpanBuilder == null || SET_SPAN_KIND_INVOKER == null) {
            return this;
        }

        try {
            SET_SPAN_KIND_INVOKER.invokeWithArguments(otelSpanBuilder, spanKind.getOtelSpanKind());
        } catch (Throwable e) {
            // TODO log
            otelSpanBuilder = null;
        }

        return this;
    }

    public Span startSpan() {
        if (otelSpanBuilder == null || START_SPAN_INVOKER == null) {
            return Span.getInvalid();
        }

        try {
            Object otelSpan = START_SPAN_INVOKER.invokeWithArguments(otelSpanBuilder);
            return new Span(otelSpan);
        } catch (Throwable e) {
            // TODO log
            otelSpanBuilder = null;
            return NOOP_SPAN;
        }
    }
}
