// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel.tracing;

import io.clientcore.core.implementation.instrumentation.otel.FallbackInvoker;
import io.clientcore.core.implementation.instrumentation.otel.OTelContext;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.instrumentation.InstrumentationContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.tracing.TraceContextGetter;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.TraceContextSetter;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;

/**
 * OpenTelemetry implementation of {@link TraceContextPropagator}.
 */
public class OTelTraceContextPropagator implements TraceContextPropagator {
    public static final TraceContextPropagator NOOP = new OTelTraceContextPropagator(null);

    private static final ClientLogger LOGGER = new ClientLogger(OTelTraceContextPropagator.class);
    private final Object otelPropagator;

    /**
     * Creates a new instance of {@link OTelTraceContextPropagator}.
     *
     * @param otelPropagator the OpenTelemetry propagator
     */
    public OTelTraceContextPropagator(Object otelPropagator) {
        this.otelPropagator = otelPropagator;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <C> void inject(InstrumentationContext context, C carrier, TraceContextSetter<C> setter) {
        if (isInitialized()) {
            FallbackInvoker.run(LOGGER, () -> ((TextMapPropagator) otelPropagator)
                .inject((Context) OTelContext.fromInstrumentationContext(context), carrier, setter::set));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <C> InstrumentationContext extract(InstrumentationContext context, C carrier, TraceContextGetter<C> getter) {
        if (isInitialized()) {
            Object updatedContext = FallbackInvoker.call(LOGGER, null, () -> ((TextMapPropagator) otelPropagator)
                .extract((Context) OTelContext.fromInstrumentationContext(context), carrier, new Getter<>(getter)));
            if (updatedContext != null) {
                return OTelSpanContext.fromOTelContext(updatedContext);
            }
        }
        return context;
    }

    private boolean isInitialized() {
        return otelPropagator != null && OTelInitializer.isInitialized();
    }

    private static final class Getter<C> implements TextMapGetter<C> {
        private final TraceContextGetter<C> getter;

        private Getter(TraceContextGetter<C> getter) {
            this.getter = getter;
        }

        @Override
        public Iterable<String> keys(C carrier) {
            return getter.keys(carrier);
        }

        @Override
        public String get(C carrier, String key) {
            return getter.get(carrier, key);
        }
    }
}
