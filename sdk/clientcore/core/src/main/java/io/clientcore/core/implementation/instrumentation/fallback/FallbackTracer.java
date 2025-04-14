// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.fallback;

import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import io.clientcore.core.instrumentation.SdkInstrumentationOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.tracing.SpanBuilder;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.Tracer;

import java.util.HashMap;
import java.util.Map;

final class FallbackTracer implements Tracer {
    private static final ClientLogger LOGGER = new ClientLogger(FallbackTracer.class);
    private final boolean isEnabled;
    private final ClientLogger logger;

    FallbackTracer(InstrumentationOptions instrumentationOptions, SdkInstrumentationOptions sdkOptions) {
        // TODO (limolkova): do we need additional config to enable fallback tracing? Or maybe we enable it only if logs are enabled?
        this.isEnabled = instrumentationOptions == null || instrumentationOptions.isTracingEnabled();
        this.logger = isEnabled ? getLogger(instrumentationOptions, sdkOptions) : LOGGER;
    }

    private static ClientLogger getLogger(InstrumentationOptions instrumentationOptions,
        SdkInstrumentationOptions sdkOptions) {
        Object providedLogger = instrumentationOptions == null ? null : instrumentationOptions.getTelemetryProvider();
        if (providedLogger instanceof ClientLogger) {
            return (ClientLogger) providedLogger;
        }

        Map<String, Object> libraryContext = new HashMap<>(2);
        libraryContext.put("sdk.name", sdkOptions.getSdkName());
        libraryContext.put("sdk.version", sdkOptions.getSdkVersion());

        return new ClientLogger(sdkOptions.getSdkName() + ".tracing", libraryContext);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SpanBuilder spanBuilder(String spanName, SpanKind spanKind, InstrumentationContext instrumentationContext) {
        return isEnabled
            ? new FallbackSpanBuilder(logger, spanName, spanKind, instrumentationContext)
            : FallbackSpanBuilder.NOOP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return isEnabled;
    }
}
