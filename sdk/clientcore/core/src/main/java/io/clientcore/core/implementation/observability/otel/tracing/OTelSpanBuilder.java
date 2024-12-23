package io.clientcore.core.implementation.observability.otel.tracing;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.observability.otel.OTelAttributeKey;
import io.clientcore.core.implementation.observability.otel.OTelInitializer;
import io.clientcore.core.observability.Attributes;
import io.clientcore.core.observability.tracing.Span;
import io.clientcore.core.observability.tracing.SpanBuilder;
import io.clientcore.core.observability.tracing.SpanContext;
import io.clientcore.core.observability.tracing.SpanKind;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.Context;

import static io.clientcore.core.implementation.observability.otel.OTelInitializer.ATTRIBUTES_CLASS;
import static io.clientcore.core.implementation.observability.otel.OTelInitializer.ATTRIBUTE_KEY_CLASS;
import static io.clientcore.core.implementation.observability.otel.OTelInitializer.CONTEXT_CLASS;
import static io.clientcore.core.implementation.observability.otel.OTelInitializer.SPAN_BUILDER_CLASS;
import static io.clientcore.core.implementation.observability.otel.OTelInitializer.SPAN_CONTEXT_CLASS;
import static io.clientcore.core.implementation.observability.otel.OTelInitializer.SPAN_KIND_CLASS;

public class OTelSpanBuilder implements SpanBuilder {
    static final OTelSpanBuilder NOOP = new OTelSpanBuilder(null);

    private static final ClientLogger LOGGER = new ClientLogger(OTelSpanBuilder.class);
    private static final OTelSpan NOOP_SPAN = new OTelSpan(null);
    private static final ReflectiveInvoker SET_PARENT_INVOKER;
    private static final ReflectiveInvoker ADD_LINK_INVOKER;
    private static final ReflectiveInvoker SET_ATTRIBUTE_INVOKER;
    private static final ReflectiveInvoker SET_SPAN_KIND_INVOKER;
    private static final ReflectiveInvoker START_SPAN_INVOKER;

    private final Object otelSpanBuilder;
    static {
        ReflectiveInvoker setParentInvoker = null;
        ReflectiveInvoker addLinkInvoker = null;
        ReflectiveInvoker setAttributeInvoker = null;
        ReflectiveInvoker setSpanKindInvoker = null;
        ReflectiveInvoker startSpanInvoker = null;

        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                setParentInvoker = ReflectionUtils.getMethodInvoker(SPAN_BUILDER_CLASS,
                    SPAN_BUILDER_CLASS.getMethod("setParent", CONTEXT_CLASS));

                addLinkInvoker = ReflectionUtils.getMethodInvoker(SPAN_BUILDER_CLASS,
                    SPAN_BUILDER_CLASS.getMethod("addLink", SPAN_CONTEXT_CLASS, ATTRIBUTES_CLASS));

                setAttributeInvoker = ReflectionUtils.getMethodInvoker(SPAN_BUILDER_CLASS,
                    SPAN_BUILDER_CLASS.getMethod("setAttribute", ATTRIBUTE_KEY_CLASS, Object.class));

                setSpanKindInvoker = ReflectionUtils.getMethodInvoker(SPAN_BUILDER_CLASS,
                    SPAN_BUILDER_CLASS.getMethod("setSpanKind", SPAN_KIND_CLASS));

                startSpanInvoker = ReflectionUtils.getMethodInvoker(SPAN_BUILDER_CLASS,
                    SPAN_BUILDER_CLASS.getMethod("startSpan"));
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.initError(LOGGER, t);
            }
        }

        SET_PARENT_INVOKER = setParentInvoker;
        ADD_LINK_INVOKER = addLinkInvoker;
        SET_ATTRIBUTE_INVOKER = setAttributeInvoker;
        SET_SPAN_KIND_INVOKER = setSpanKindInvoker;
        START_SPAN_INVOKER = startSpanInvoker;
    }

    OTelSpanBuilder(Object otelSpanBuilder) {
        this.otelSpanBuilder = otelSpanBuilder;
    }

    @Override
    public SpanBuilder setParent(Context context) {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpanBuilder != null) {
            try {
                SET_PARENT_INVOKER.invokeWithArguments(otelSpanBuilder, context);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return this;
    }

    @Override
    public SpanBuilder addLink(SpanContext spanContext, Attributes attributes) {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpanBuilder != null) {
            try {
                ADD_LINK_INVOKER.invokeWithArguments(otelSpanBuilder, spanContext, attributes);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return this;
    }


    @Override
    public SpanBuilder setAttribute(String key, Object value) {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpanBuilder != null) {
            try {
                SET_ATTRIBUTE_INVOKER.invokeWithArguments(otelSpanBuilder, OTelAttributeKey.getKey(key, value), OTelAttributeKey.castAttributeValue(value));
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return this;
    }

    @Override
    public SpanBuilder setSpanKind(SpanKind spanKind) {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpanBuilder != null) {
            try {
                SET_SPAN_KIND_INVOKER.invokeWithArguments(otelSpanBuilder, OTelSpanKind.getOtelSpanKind(spanKind));
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return this;
    }

    @Override
    public Span startSpan() {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpanBuilder != null) {
            try {
                Object otelSpan = START_SPAN_INVOKER.invokeWithArguments(otelSpanBuilder);
                return new OTelSpan(otelSpan);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }

        return NOOP_SPAN;
    }
}
