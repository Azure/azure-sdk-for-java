// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel.tracing;

import io.clientcore.core.implementation.telemetry.LibraryTelemetryOptionsAccessHelper;
import io.clientcore.core.implementation.telemetry.otel.OTelAttributeKey;
import io.clientcore.core.implementation.telemetry.otel.OTelInitializer;
import io.clientcore.core.telemetry.LibraryTelemetryOptions;
import io.clientcore.core.telemetry.TelemetryProvider;
import io.clientcore.core.telemetry.tracing.Span;
import io.clientcore.core.telemetry.tracing.SpanBuilder;
import io.clientcore.core.telemetry.tracing.SpanKind;
import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.Context;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.ATTRIBUTE_KEY_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.CONTEXT_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.SPAN_BUILDER_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.SPAN_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.SPAN_KIND_CLASS;

public class OTelSpanBuilder implements SpanBuilder {
    static final OTelSpanBuilder NOOP = new OTelSpanBuilder(null,  SpanKind.INTERNAL, Context.none(), new LibraryTelemetryOptions("noop"));

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
    private static final ClientLogger LOGGER = new ClientLogger(OTelSpanBuilder.class);
    private static final OTelSpan NOOP_SPAN;
    private static final MethodHandle SET_PARENT_INVOKER;
    private static final MethodHandle SET_ATTRIBUTE_INVOKER;
    private static final MethodHandle SET_SPAN_KIND_INVOKER;
    private static final MethodHandle START_SPAN_INVOKER;
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
        MethodHandle setParentInvoker = null;
        MethodHandle setAttributeInvoker = null;
        MethodHandle setSpanKindInvoker = null;
        MethodHandle startSpanInvoker = null;

        Object internalKind = null;
        Object serverKind = null;
        Object clientKind = null;
        Object producerKind = null;
        Object consumerKind = null;
        OTelSpan noopSpan = null;

        if (OTelInitializer.isInitialized()) {
            try {
                setParentInvoker = LOOKUP.findVirtual(SPAN_BUILDER_CLASS, "setParent",
                    MethodType.methodType(SPAN_BUILDER_CLASS, CONTEXT_CLASS));
                setAttributeInvoker = LOOKUP.findVirtual(SPAN_BUILDER_CLASS, "setAttribute",
                    MethodType.methodType(SPAN_BUILDER_CLASS, ATTRIBUTE_KEY_CLASS, Object.class));
                setSpanKindInvoker = LOOKUP.findVirtual(SPAN_BUILDER_CLASS, "setSpanKind",
                    MethodType.methodType(SPAN_BUILDER_CLASS, SPAN_KIND_CLASS));
                startSpanInvoker
                    = LOOKUP.findVirtual(SPAN_BUILDER_CLASS, "startSpan", MethodType.methodType(SPAN_CLASS));

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
                Object otelParentContext = getParent(context);
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

    private static Object getParent(Context context) throws Throwable {
        Object parent = context.get(TelemetryProvider.TRACE_CONTEXT_KEY);
        if (CONTEXT_CLASS.isInstance(parent)) {
            return parent;
        } else if (parent instanceof OTelSpan) {
            return  ((OTelSpan) parent).getOtelContext();
        } else if (parent != null) {
            LOGGER.atVerbose()
                .addKeyValue("expectedType", CONTEXT_CLASS.getName())
                .addKeyValue("actualType", parent.getClass().getName())
                .log("Context does not contain an OpenTelemetry context. Ignoring it.");
        }

        return OTelContext.getCurrent();
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
