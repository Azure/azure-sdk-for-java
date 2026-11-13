// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel.tracing;

import io.clientcore.core.implementation.instrumentation.otel.FallbackInvoker;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.SdkInstrumentationOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.tracing.SpanBuilder;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.opentelemetry.api.trace.TracerBuilder;
import io.opentelemetry.api.trace.TracerProvider;

/**
 * OpenTelemetry implementation of {@link Tracer}.
 */
public final class OTelTracer implements Tracer {
    public static final OTelTracer NOOP = new OTelTracer();
    private static final ClientLogger LOGGER = new ClientLogger(OTelTracer.class);

    private final Object otelTracer;
    private final SdkInstrumentationOptions sdkOptions;

    private OTelTracer() {
        this.otelTracer = null;
        this.sdkOptions = null;
    }

    /**
     * Creates a new instance of {@link OTelTracer}.
     * @param otelTracerProvider the OpenTelemetry tracer provider
     * @param sdkOptions the sdk instrumentation options
     */
    public OTelTracer(Object otelTracerProvider, SdkInstrumentationOptions sdkOptions) {
        TracerBuilder tracerBuilder = FallbackInvoker.call(LOGGER, null,
            () -> ((TracerProvider) otelTracerProvider).tracerBuilder(sdkOptions.getSdkName()));
        if (tracerBuilder != null) {
            FallbackInvoker.run(LOGGER, () -> tracerBuilder.setInstrumentationVersion(sdkOptions.getSdkVersion()));
            FallbackInvoker.run(LOGGER, () -> tracerBuilder.setSchemaUrl(sdkOptions.getSchemaUrl()));
            this.otelTracer = FallbackInvoker.call(LOGGER, null, () -> tracerBuilder.build());
        } else {
            this.otelTracer = null;
        }
        this.sdkOptions = sdkOptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanBuilder spanBuilder(String spanName, SpanKind spanKind, InstrumentationContext parentContext) {
        if (isEnabled()) {
            Object otelSpanBuilder = FallbackInvoker.call(LOGGER, null,
                () -> ((io.opentelemetry.api.trace.Tracer) otelTracer).spanBuilder(spanName));
            if (otelSpanBuilder != null) {
                return new OTelSpanBuilder(otelSpanBuilder, spanKind, parentContext, sdkOptions);
            }
        }

        return OTelSpanBuilder.NOOP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return otelTracer != null && OTelInitializer.isInitialized();
    }
}
