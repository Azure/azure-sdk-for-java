// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel;

import io.clientcore.core.implementation.telemetry.otel.tracing.OTelTextMapPropagator;
import io.clientcore.core.implementation.telemetry.otel.tracing.OTelTracer;
import io.clientcore.core.telemetry.LibraryTelemetryOptions;
import io.clientcore.core.telemetry.TelemetryOptions;
import io.clientcore.core.telemetry.TelemetryProvider;
import io.clientcore.core.telemetry.tracing.TextMapPropagator;
import io.clientcore.core.telemetry.tracing.Tracer;
import io.clientcore.core.util.ClientLogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.GLOBAL_OTEL_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.OTEL_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.TRACER_PROVIDER_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.W3C_PROPAGATOR_CLASS;

public class OTelTelemetryProvider implements TelemetryProvider {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
    private static final MethodHandle GET_PROVIDER_INVOKER;
    private static final MethodHandle GET_GLOBAL_OTEL_INVOKER;

    private static final Object NOOP_PROVIDER;
    private static final OTelTextMapPropagator W3C_PROPAGATOR_INSTANCE;
    private static final ClientLogger LOGGER = new ClientLogger(OTelTelemetryProvider.class);
    static {
        MethodHandle getProviderInvoker = null;
        MethodHandle getGlobalOtelInvoker = null;

        Object noopProvider = null;
        Object w3cPropagatorInstance = null;

        if (OTelInitializer.isInitialized()) {
            try {
                getProviderInvoker
                    = LOOKUP.findVirtual(OTEL_CLASS, "getTracerProvider", MethodType.methodType(TRACER_PROVIDER_CLASS));
                getGlobalOtelInvoker = LOOKUP.findStatic(GLOBAL_OTEL_CLASS, "get",
                    MethodType.methodType(OTEL_CLASS));

                MethodHandle noopProviderInvoker
                    = LOOKUP.findStatic(TRACER_PROVIDER_CLASS, "noop", MethodType.methodType(TRACER_PROVIDER_CLASS));
                noopProvider = noopProviderInvoker.invoke();

                MethodHandle w3cPropagatorInvoker
                    = LOOKUP.findStatic(W3C_PROPAGATOR_CLASS, "getInstance", MethodType.methodType(W3C_PROPAGATOR_CLASS));

                w3cPropagatorInstance = w3cPropagatorInvoker.invoke();

            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        GET_PROVIDER_INVOKER = getProviderInvoker;
        GET_GLOBAL_OTEL_INVOKER = getGlobalOtelInvoker;
        NOOP_PROVIDER = noopProvider;

        W3C_PROPAGATOR_INSTANCE = new OTelTextMapPropagator(w3cPropagatorInstance);
    }

    private final Object otelInstance;
    private final LibraryTelemetryOptions libraryOptions;
    private final boolean isTracingEnabled;

    public OTelTelemetryProvider(TelemetryOptions<?> applicationOptions, LibraryTelemetryOptions libraryOptions) {
        Objects.requireNonNull(libraryOptions, "'libraryOptions' cannot be null");
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

    @Override
    public Tracer getTracer() {
        if (OTelInitializer.isInitialized() && isTracingEnabled) {
            try {
                Object otelTracerProvider = GET_PROVIDER_INVOKER.invoke(getOtelInstance());

                if (otelTracerProvider != null && otelTracerProvider != NOOP_PROVIDER) {
                    return new OTelTracer(otelTracerProvider, libraryOptions);
                }
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }

        return OTelTracer.NOOP;
    }

    private Object getOtelInstance() throws Throwable {
        // not caching global to prevent caching instance that was not setup yet at the start time.
        return otelInstance != null ? otelInstance : GET_GLOBAL_OTEL_INVOKER.invoke();
    }

    @Override
    public TextMapPropagator getW3CTraceContextPropagator() {
        return OTelInitializer.isInitialized() ? W3C_PROPAGATOR_INSTANCE : OTelTextMapPropagator.NOOP;
    }
}
