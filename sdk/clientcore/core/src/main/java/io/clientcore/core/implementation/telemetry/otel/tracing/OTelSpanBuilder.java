// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel.tracing;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.telemetry.LibraryTelemetryOptionsAccessHelper;
import io.clientcore.core.implementation.telemetry.otel.OTelAttributeKey;
import io.clientcore.core.implementation.telemetry.otel.OTelInitializer;
import io.clientcore.core.telemetry.LibraryTelemetryOptions;
import io.clientcore.core.telemetry.tracing.Span;
import io.clientcore.core.telemetry.tracing.SpanBuilder;
import io.clientcore.core.telemetry.tracing.SpanKind;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.Context;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.ATTRIBUTE_KEY_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.CONTEXT_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.SPAN_BUILDER_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.SPAN_KIND_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.tracing.OTelUtils.getOTelContext;

public class OTelSpanBuilder implements SpanBuilder {
    static final OTelSpanBuilder NOOP
        = new OTelSpanBuilder(null, SpanKind.INTERNAL, Context.none(), new LibraryTelemetryOptions("noop"));

    private static final ClientLogger LOGGER = new ClientLogger(OTelSpanBuilder.class);
    private static final OTelSpan NOOP_SPAN;
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
    private final boolean suppressNestedSpans;
    private final SpanKind spanKind;
    private final Context context;

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
        OTelSpan noopSpan = null;

        if (OTelInitializer.isInitialized()) {
            try {
                setParentInvoker
                    = getMethodInvoker(SPAN_BUILDER_CLASS, SPAN_BUILDER_CLASS.getMethod("setParent", CONTEXT_CLASS));

                setAttributeInvoker = getMethodInvoker(SPAN_BUILDER_CLASS,
                    SPAN_BUILDER_CLASS.getMethod("setAttribute", ATTRIBUTE_KEY_CLASS, Object.class));

                setSpanKindInvoker = getMethodInvoker(SPAN_BUILDER_CLASS,
                    SPAN_BUILDER_CLASS.getMethod("setSpanKind", SPAN_KIND_CLASS));

                startSpanInvoker = getMethodInvoker(SPAN_BUILDER_CLASS, SPAN_BUILDER_CLASS.getMethod("startSpan"));

                internalKind = SPAN_KIND_CLASS.getField("INTERNAL").get(null);
                serverKind = SPAN_KIND_CLASS.getField("SERVER").get(null);
                clientKind = SPAN_KIND_CLASS.getField("CLIENT").get(null);
                producerKind = SPAN_KIND_CLASS.getField("PRODUCER").get(null);
                consumerKind = SPAN_KIND_CLASS.getField("CONSUMER").get(null);

                noopSpan = new OTelSpan(null, OTelContext.getCurrent(), SpanKind.INTERNAL);
            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
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
        NOOP_SPAN = noopSpan;
    }

    OTelSpanBuilder(Object otelSpanBuilder, SpanKind kind, Context parent, LibraryTelemetryOptions libraryOptions) {
        this.otelSpanBuilder = otelSpanBuilder;
        this.suppressNestedSpans
            = libraryOptions == null || !LibraryTelemetryOptionsAccessHelper.isSpanSuppressionDisabled(libraryOptions);
        this.spanKind = kind;
        this.context = parent;
    }

    @Override
    public SpanBuilder setAttribute(String key, Object value) {
        if (OTelInitializer.isInitialized() && otelSpanBuilder != null) {
            try {
                SET_ATTRIBUTE_INVOKER.invoke(otelSpanBuilder, OTelAttributeKey.getKey(key, value),
                    OTelAttributeKey.castAttributeValue(value));
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }

        return this;
    }

    @Override
    public Span startSpan() {
        if (OTelInitializer.isInitialized() && otelSpanBuilder != null) {
            try {
                Object otelParentContext = getOTelContext(context);
                SET_PARENT_INVOKER.invoke(otelSpanBuilder, otelParentContext);
                SET_SPAN_KIND_INVOKER.invoke(otelSpanBuilder, toOtelSpanKind(spanKind));
                Object otelSpan = shouldSuppress(otelParentContext)
                    ? OTelSpan.createPropagatingSpan(otelParentContext)
                    : START_SPAN_INVOKER.invoke(otelSpanBuilder);
                return new OTelSpan(otelSpan, otelParentContext, this.spanKind);
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }

        return NOOP_SPAN;
    }

    private boolean shouldSuppress(Object parentContext) throws Throwable {
        return suppressNestedSpans
            && (this.spanKind == SpanKind.CLIENT || this.spanKind == SpanKind.INTERNAL)
            && OTelContext.hasClientCoreSpan(parentContext);
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
