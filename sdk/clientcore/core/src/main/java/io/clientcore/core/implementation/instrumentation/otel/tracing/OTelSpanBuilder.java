// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel.tracing;

import io.clientcore.core.implementation.instrumentation.SdkInstrumentationOptionsAccessHelper;
import io.clientcore.core.implementation.instrumentation.otel.FallbackInvoker;
import io.clientcore.core.implementation.instrumentation.otel.OTelAttributes;
import io.clientcore.core.implementation.instrumentation.otel.OTelContext;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.instrumentation.InstrumentationAttributes;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.SdkInstrumentationOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanBuilder;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;

import static io.clientcore.core.implementation.instrumentation.otel.OTelAttributeKey.castAttributeValue;
import static io.clientcore.core.implementation.instrumentation.otel.OTelAttributeKey.getKey;

/**
 * OpenTelemetry implementation of {@link SpanBuilder}.
 */
@SuppressWarnings("unchecked")
public class OTelSpanBuilder implements SpanBuilder {
    static final OTelSpanBuilder NOOP
        = new OTelSpanBuilder(null, SpanKind.INTERNAL, null, new SdkInstrumentationOptions("noop"));

    private static final ClientLogger LOGGER = new ClientLogger(OTelSpanBuilder.class);

    private final Object otelSpanBuilder;
    private final boolean suppressNestedSpans;
    private final SpanKind spanKind;
    private final InstrumentationContext context;

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
                FallbackInvoker.run(LOGGER, () -> ((io.opentelemetry.api.trace.SpanBuilder) otelSpanBuilder)
                    .setAttribute((AttributeKey<Object>) otelKey, castAttributeValue(value)));
            }
        }

        return this;
    }

    @Override
    public SpanBuilder setAllAttributes(InstrumentationAttributes attributes) {
        if (isInitialized() && attributes instanceof OTelAttributes) {
            Object otelAttributes = ((OTelAttributes) attributes).getOTelAttributes();
            if (otelAttributes != null) {
                FallbackInvoker.run(LOGGER, () -> ((io.opentelemetry.api.trace.SpanBuilder) otelSpanBuilder)
                    .setAllAttributes((Attributes) otelAttributes));
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
                FallbackInvoker.run(LOGGER, () -> ((io.opentelemetry.api.trace.SpanBuilder) otelSpanBuilder)
                    .setParent((Context) otelParentContext));
                FallbackInvoker.run(LOGGER, () -> ((io.opentelemetry.api.trace.SpanBuilder) otelSpanBuilder)
                    .setSpanKind(toOtelSpanKind(spanKind)));
                otelSpan = FallbackInvoker.call(LOGGER, null,
                    () -> ((io.opentelemetry.api.trace.SpanBuilder) otelSpanBuilder).startSpan());
            }

            if (otelSpan != null) {
                return new OTelSpan(otelSpan, otelParentContext, this.spanKind);
            }
        }

        return Span.noop();
    }

    private static io.opentelemetry.api.trace.SpanKind toOtelSpanKind(SpanKind spanKind) {
        switch (spanKind) {
            case SERVER:
                return io.opentelemetry.api.trace.SpanKind.SERVER;

            case CLIENT:
                return io.opentelemetry.api.trace.SpanKind.CLIENT;

            case PRODUCER:
                return io.opentelemetry.api.trace.SpanKind.PRODUCER;

            case CONSUMER:
                return io.opentelemetry.api.trace.SpanKind.CONSUMER;

            default:
                return io.opentelemetry.api.trace.SpanKind.INTERNAL;
        }
    }

    private boolean isInitialized() {
        return otelSpanBuilder != null && OTelInitializer.isInitialized();
    }
}
