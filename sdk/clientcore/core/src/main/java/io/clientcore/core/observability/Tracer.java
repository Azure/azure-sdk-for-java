package io.clientcore.core.observability;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;

public class Tracer {
    private volatile Object otelTracer;
    private static final ReflectiveInvoker SPAN_BUILDER_INVOKER;
    private static final SpanBuilder NOOP_BUILDER = new SpanBuilder(null);

    static {
        Class<?> otelTracerClass;
        ReflectiveInvoker spanBuilderInvoker;

        try {
            otelTracerClass = Class.forName("io.opentelemetry.api.trace.Tracer", true, Tracer.class.getClassLoader());

            spanBuilderInvoker = ReflectionUtils.getMethodInvoker(otelTracerClass,
                otelTracerClass.getMethod("spanBuilder", String.class));
        } catch (Exception e) {
            spanBuilderInvoker = null;
            // TODO
        }

        SPAN_BUILDER_INVOKER = spanBuilderInvoker;
    }

    public Tracer(Object otelTracer) {
        this.otelTracer = otelTracer;
    }

    public SpanBuilder spanBuilder(String spanName) {
        if (otelTracer == null || SPAN_BUILDER_INVOKER == null) {
            return null;
        }

        try {
            Object otelSpanBuilder = SPAN_BUILDER_INVOKER.invokeWithArguments(otelTracer, spanName);
            return new SpanBuilder(otelSpanBuilder);
        } catch (Throwable e) {
            // TODO log
            return NOOP_BUILDER;
        }
    }
}
