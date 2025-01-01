// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation.otel;

import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.instrumentation.otel.tracing.OTelTraceContextPropagator;
import io.clientcore.core.implementation.instrumentation.otel.tracing.OTelTracer;
import io.clientcore.core.instrumentation.LibraryInstrumentationOptions;
import io.clientcore.core.instrumentation.InstrumentationOptions;
import io.clientcore.core.instrumentation.Instrumentation;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.util.ClientLogger;

import static io.clientcore.core.implementation.ReflectionUtils.getMethodInvoker;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.GLOBAL_OTEL_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.OTEL_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.TRACER_PROVIDER_CLASS;
import static io.clientcore.core.implementation.instrumentation.otel.OTelInitializer.W3C_PROPAGATOR_CLASS;

/**
 * A {@link Instrumentation} implementation that uses OpenTelemetry.
 */
public class OTelInstrumentation implements Instrumentation {
    private static final FallbackInvoker GET_PROVIDER_INVOKER;
    private static final FallbackInvoker GET_GLOBAL_OTEL_INVOKER;

    private static final Object NOOP_PROVIDER;
    private static final OTelTraceContextPropagator W3C_PROPAGATOR_INSTANCE;
    private static final ClientLogger LOGGER = new ClientLogger(OTelInstrumentation.class);
    static {
        ReflectiveInvoker getProviderInvoker = null;
        ReflectiveInvoker getGlobalOtelInvoker = null;

        Object noopProvider = null;
        Object w3cPropagatorInstance = null;

        if (OTelInitializer.isInitialized()) {
            try {
                getProviderInvoker = getMethodInvoker(OTEL_CLASS, OTEL_CLASS.getMethod("getTracerProvider"));
                getGlobalOtelInvoker = getMethodInvoker(GLOBAL_OTEL_CLASS, GLOBAL_OTEL_CLASS.getMethod("get"));

                ReflectiveInvoker noopProviderInvoker
                    = getMethodInvoker(TRACER_PROVIDER_CLASS, TRACER_PROVIDER_CLASS.getMethod("noop"));
                noopProvider = noopProviderInvoker.invoke();

                ReflectiveInvoker w3cPropagatorInvoker
                    = getMethodInvoker(W3C_PROPAGATOR_CLASS, W3C_PROPAGATOR_CLASS.getMethod("getInstance"));
                w3cPropagatorInstance = w3cPropagatorInvoker.invoke();

            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        GET_PROVIDER_INVOKER = new FallbackInvoker(getProviderInvoker, LOGGER);
        GET_GLOBAL_OTEL_INVOKER = new FallbackInvoker(getGlobalOtelInvoker, LOGGER);
        NOOP_PROVIDER = noopProvider;

        W3C_PROPAGATOR_INSTANCE = new OTelTraceContextPropagator(w3cPropagatorInstance);
    }

    private final Object otelInstance;
    private final LibraryInstrumentationOptions libraryOptions;
    private final boolean isTracingEnabled;

    /**
     * Creates a new instance of {@link OTelInstrumentation}.
     *
     * @param applicationOptions the application options
     * @param libraryOptions the library options
     */
    public OTelInstrumentation(InstrumentationOptions<?> applicationOptions,
        LibraryInstrumentationOptions libraryOptions) {
        Object explicitOTel = applicationOptions == null ? null : applicationOptions.getProvider();
        if (explicitOTel != null && !OTEL_CLASS.isInstance(explicitOTel)) {
            throw LOGGER.atError()
                .addKeyValue("expectedProvider", OTEL_CLASS.getName())
                .addKeyValue("actualProvider", explicitOTel.getClass().getName())
                .log("Unexpected telemetry provider type.",
                    new IllegalArgumentException("Telemetry provider is not an instance of " + OTEL_CLASS.getName()));
        }

        this.otelInstance = explicitOTel;
        this.libraryOptions = libraryOptions;
        this.isTracingEnabled = applicationOptions == null || applicationOptions.isTracingEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Tracer getTracer() {
        if (OTelInitializer.isInitialized() && isTracingEnabled) {
            Object otelTracerProvider = GET_PROVIDER_INVOKER.invoke(getOtelInstance());

            if (otelTracerProvider != null && otelTracerProvider != NOOP_PROVIDER) {
                return new OTelTracer(otelTracerProvider, libraryOptions);
            }
        }

        return OTelTracer.NOOP;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TraceContextPropagator getW3CTraceContextPropagator() {
        return OTelInitializer.isInitialized() ? W3C_PROPAGATOR_INSTANCE : OTelTraceContextPropagator.NOOP;
    }

    private Object getOtelInstance() {
        // not caching global to prevent caching instance that was not setup yet at the start time.
        return otelInstance != null ? otelInstance : GET_GLOBAL_OTEL_INVOKER.invoke();
    }
}
