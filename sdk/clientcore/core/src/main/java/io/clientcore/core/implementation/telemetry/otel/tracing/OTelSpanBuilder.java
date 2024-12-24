// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel.tracing;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.telemetry.otel.OTelAttributeKey;
import io.clientcore.core.implementation.telemetry.otel.OTelInitializer;
import io.clientcore.core.telemetry.TelemetryProvider;
import io.clientcore.core.telemetry.tracing.Span;
import io.clientcore.core.telemetry.tracing.SpanBuilder;
import io.clientcore.core.telemetry.tracing.SpanKind;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.Context;

import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.ATTRIBUTE_KEY_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.CONTEXT_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.SPAN_BUILDER_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.SPAN_KIND_CLASS;

public class OTelSpanBuilder implements SpanBuilder {
    static final OTelSpanBuilder NOOP = new OTelSpanBuilder(null);

    private static final ClientLogger LOGGER = new ClientLogger(OTelSpanBuilder.class);
    private static final OTelSpan NOOP_SPAN = new OTelSpan(null);
    private static final ReflectiveInvoker SET_PARENT_INVOKER;
    private static final ReflectiveInvoker SET_ATTRIBUTE_INVOKER;
    private static final ReflectiveInvoker SET_SPAN_KIND_INVOKER;
    private static final ReflectiveInvoker START_SPAN_INVOKER;
    private static final Object INTERNAL_KIND;
    private static final Object SERVER_KIND;
    private static final Object CLIENT_KIND;
    private static final Object PRODUCER_KIND;
    private static final Object CONSUMER_KIND;

    private final Object otelSpanBuilder;
    static {
        ReflectiveInvoker setParentInvoker = null;
        ReflectiveInvoker setAttributeInvoker = null;
        ReflectiveInvoker setSpanKindInvoker = null;
        ReflectiveInvoker startSpanInvoker = null;

        Object internalKind = null;
        Object serverKind = null;
        Object clientKind = null;
        Object producerKind = null;
        Object consumerKind = null;

        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                setParentInvoker = ReflectionUtils.getMethodInvoker(SPAN_BUILDER_CLASS,
                    SPAN_BUILDER_CLASS.getMethod("setParent", CONTEXT_CLASS));

                setAttributeInvoker = ReflectionUtils.getMethodInvoker(SPAN_BUILDER_CLASS,
                    SPAN_BUILDER_CLASS.getMethod("setAttribute", ATTRIBUTE_KEY_CLASS, Object.class));

                setSpanKindInvoker = ReflectionUtils.getMethodInvoker(SPAN_BUILDER_CLASS,
                    SPAN_BUILDER_CLASS.getMethod("setSpanKind", SPAN_KIND_CLASS));

                startSpanInvoker
                    = ReflectionUtils.getMethodInvoker(SPAN_BUILDER_CLASS, SPAN_BUILDER_CLASS.getMethod("startSpan"));

                internalKind = SPAN_KIND_CLASS.getField("INTERNAL").get(null);
                serverKind = SPAN_KIND_CLASS.getField("SERVER").get(null);
                clientKind = SPAN_KIND_CLASS.getField("CLIENT").get(null);
                producerKind = SPAN_KIND_CLASS.getField("PRODUCER").get(null);
                consumerKind = SPAN_KIND_CLASS.getField("CONSUMER").get(null);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.initError(LOGGER, t);
            }
        }

        SET_PARENT_INVOKER = setParentInvoker;
        SET_ATTRIBUTE_INVOKER = setAttributeInvoker;
        SET_SPAN_KIND_INVOKER = setSpanKindInvoker;
        START_SPAN_INVOKER = startSpanInvoker;
        INTERNAL_KIND = internalKind;
        SERVER_KIND = serverKind;
        CLIENT_KIND = clientKind;
        PRODUCER_KIND = producerKind;
        CONSUMER_KIND = consumerKind;
    }

    OTelSpanBuilder(Object otelSpanBuilder) {
        this.otelSpanBuilder = otelSpanBuilder;
    }

    @Override
    public SpanBuilder setParent(Context context) {
        if (OTelInitializer.INSTANCE.isInitialized() && otelSpanBuilder != null) {
            Object otelContext = context.get(TelemetryProvider.TRACE_CONTEXT_KEY);
            if (!CONTEXT_CLASS.isInstance(otelContext)) {
                return this;
            }

            try {
                SET_PARENT_INVOKER.invokeWithArguments(otelSpanBuilder, otelContext);
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
                SET_ATTRIBUTE_INVOKER.invokeWithArguments(otelSpanBuilder, OTelAttributeKey.getKey(key, value),
                    OTelAttributeKey.castAttributeValue(value));
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
                SET_SPAN_KIND_INVOKER.invokeWithArguments(otelSpanBuilder, toOtelSpanKind(spanKind));
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

    private Object toOtelSpanKind(SpanKind spanKind) {
        switch (spanKind) {
            case SERVER:
                return SERVER_KIND;

            case CLIENT:
                return CLIENT_KIND;

            case PRODUCER:
                return PRODUCER_KIND;

            case CONSUMER:
                return CONSUMER_KIND;

            default:
                return INTERNAL_KIND;
        }
    }
}
