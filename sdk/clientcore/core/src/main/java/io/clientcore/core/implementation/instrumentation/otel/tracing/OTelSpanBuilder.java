// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel.tracing;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.instrumentation.SdkInstrumentationOptionsAccessHelper;
import io.clientcore.core.implementation.instrumentation.otel.FallbackInvoker;
import io.clientcore.core.implementation.instrumentation.otel.OTelAttributes;
import io.clientcore.core.implementation.instrumentation.otel.OTelContext;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.instrumentation.InstrumentationAttributes;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.SdkInstrumentationOptions;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanBuilder;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.instrumentation.otel.OTelAttributeKey.castAttributeValue;
import static io.clientcore.core.implementation.instrumentation.otel.OTelAttributeKey.getKey;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.ATTRIBUTES_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.ATTRIBUTE_KEY_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.CONTEXT_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.SPAN_BUILDER_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.SPAN_KIND_CLASS;

/**
 * OpenTelemetry implementation of {@link SpanBuilder}.
 */
public class OTelSpanBuilder implements SpanBuilder {
    static final OTelSpanBuilder NOOP
        = new OTelSpanBuilder(null, SpanKind.INTERNAL, null, new SdkInstrumentationOptions("noop"));

    private static final ClientLogger LOGGER = new ClientLogger(OTelSpanBuilder.class);
    private static final FallbackInvoker SET_PARENT_INVOKER;
    private static final FallbackInvoker SET_ATTRIBUTE_INVOKER;
    private static final FallbackInvoker SET_SPAN_KIND_INVOKER;
    private static final FallbackInvoker START_SPAN_INVOKER;
    private static final FallbackInvoker SET_ALL_ATTRIBUTES_INVOKER;
    private static final Object INTERNAL_KIND;
    private static final Object SERVER_KIND;
    private static final Object CLIENT_KIND;
    private static final Object PRODUCER_KIND;
    private static final Object CONSUMER_KIND;

    private final Object otelSpanBuilder;
    private final boolean suppressNestedSpans;
    private final SpanKind spanKind;
    private final InstrumentationContext context;

    static {
        ReflectiveInvoker setParentInvoker = null;
        ReflectiveInvoker setAttributeInvoker = null;
        ReflectiveInvoker setSpanKindInvoker = null;
        ReflectiveInvoker startSpanInvoker = null;
        ReflectiveInvoker setAllAttributesInvoker = null;

        Object internalKind = null;
        Object serverKind = null;
        Object clientKind = null;
        Object producerKind = null;
        Object consumerKind = null;

        if (OTelInitializer.isInitialized()) {
            try {
                setParentInvoker
                    = getMethodInvoker(SPAN_BUILDER_CLASS, SPAN_BUILDER_CLASS.getMethod("setParent", CONTEXT_CLASS));

                setAttributeInvoker = getMethodInvoker(SPAN_BUILDER_CLASS,
                    SPAN_BUILDER_CLASS.getMethod("setAttribute", ATTRIBUTE_KEY_CLASS, Object.class));

                setSpanKindInvoker = getMethodInvoker(SPAN_BUILDER_CLASS,
                    SPAN_BUILDER_CLASS.getMethod("setSpanKind", SPAN_KIND_CLASS));

                startSpanInvoker = getMethodInvoker(SPAN_BUILDER_CLASS, SPAN_BUILDER_CLASS.getMethod("startSpan"));
                setAllAttributesInvoker = getMethodInvoker(SPAN_BUILDER_CLASS,
                    SPAN_BUILDER_CLASS.getMethod("setAllAttributes", ATTRIBUTES_CLASS));

                internalKind = SPAN_KIND_CLASS.getField("INTERNAL").get(null);
                serverKind = SPAN_KIND_CLASS.getField("SERVER").get(null);
                clientKind = SPAN_KIND_CLASS.getField("CLIENT").get(null);
                producerKind = SPAN_KIND_CLASS.getField("PRODUCER").get(null);
                consumerKind = SPAN_KIND_CLASS.getField("CONSUMER").get(null);
            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        SET_PARENT_INVOKER = new FallbackInvoker(setParentInvoker, LOGGER);
        SET_ATTRIBUTE_INVOKER = new FallbackInvoker(setAttributeInvoker, LOGGER);
        SET_SPAN_KIND_INVOKER = new FallbackInvoker(setSpanKindInvoker, LOGGER);
        START_SPAN_INVOKER = new FallbackInvoker(startSpanInvoker, OTelSpan.NOOP_SPAN.getOtelSpan(), LOGGER);
        SET_ALL_ATTRIBUTES_INVOKER = new FallbackInvoker(setAllAttributesInvoker, LOGGER);
        INTERNAL_KIND = internalKind;
        SERVER_KIND = serverKind;
        CLIENT_KIND = clientKind;
        PRODUCER_KIND = producerKind;
        CONSUMER_KIND = consumerKind;

    }

    OTelSpanBuilder(Object otelSpanBuilder, SpanKind kind, InstrumentationContext parent,
        SdkInstrumentationOptions sdkOptions) {
        this.otelSpanBuilder = otelSpanBuilder;
        this.suppressNestedSpans
            = sdkOptions == null || !SdkInstrumentationOptionsAccessHelper.isSpanSuppressionDisabled(sdkOptions);
        this.spanKind = kind;
        this.context = parent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanBuilder setAttribute(String key, Object value) {
        if (isInitialized()) {
            Object otelKey = getKey(key, value);
            if (otelKey != null) {
                SET_ATTRIBUTE_INVOKER.invoke(otelSpanBuilder, otelKey, castAttributeValue(value));
            }
        }

        return this;
    }

    @Override
    public SpanBuilder setAllAttributes(InstrumentationAttributes attributes) {
        if (isInitialized() && attributes instanceof OTelAttributes) {
            Object otelAttributes = ((OTelAttributes) attributes).getOTelAttributes();
            if (otelAttributes != null) {
                SET_ALL_ATTRIBUTES_INVOKER.invoke(otelSpanBuilder, otelAttributes);
            }
        }

        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Span startSpan() {
        if (isInitialized()) {
            Object otelParentContext = OTelContext.fromInstrumentationContext(context);
            OTelSpan parentSpan = OTelContext.getClientCoreSpan(otelParentContext);
            SpanKind parentKind = parentSpan == null ? null : parentSpan.getSpanKind();
            Object otelSpan;
            if (suppressNestedSpans && parentKind == spanKind) {
                otelSpan = OTelSpan.createPropagatingSpan(otelParentContext);
            } else {
                SET_PARENT_INVOKER.invoke(otelSpanBuilder, otelParentContext);
                SET_SPAN_KIND_INVOKER.invoke(otelSpanBuilder, toOtelSpanKind(spanKind));
                otelSpan = START_SPAN_INVOKER.invoke(otelSpanBuilder);
            }

            if (otelSpan != null) {
                return new OTelSpan(otelSpan, otelParentContext, this.spanKind);
            }
        }

        return OTelSpan.NOOP_SPAN;
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

    private boolean isInitialized() {
        return otelSpanBuilder != null && OTelInitializer.isInitialized();
    }
}
