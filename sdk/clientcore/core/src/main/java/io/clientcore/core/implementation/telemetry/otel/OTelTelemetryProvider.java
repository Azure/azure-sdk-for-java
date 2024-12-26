// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel;

import io.clientcore.core.implementation.telemetry.otel.tracing.OTelTracer;
import io.clientcore.core.telemetry.LibraryTelemetryOptions;
import io.clientcore.core.telemetry.TelemetryOptions;
import io.clientcore.core.telemetry.TelemetryProvider;
import io.clientcore.core.telemetry.tracing.Tracer;
import io.clientcore.core.util.ClientLogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Objects;

import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.GLOBAL_OTEL_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.OTEL_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.TRACER_PROVIDER_CLASS;

public class OTelTelemetryProvider implements TelemetryProvider {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.publicLookup();
    public static final TelemetryProvider INSTANCE = new OTelTelemetryProvider();
    private static final MethodHandle GET_PROVIDER_INVOKER;
    private static final MethodHandle GET_GLOBAL_PROVIDER_INVOKER;

    private static final Object NOOP_PROVIDER;
    private static final ClientLogger LOGGER = new ClientLogger(OTelTelemetryProvider.class);
    static {
        MethodHandle getProviderInvoker = null;
        MethodHandle getGlobalProviderInvoker = null;

        Object noopProvider = null;

        if (OTelInitializer.isInitialized()) {
            try {
                getProviderInvoker
                    = LOOKUP.findVirtual(OTEL_CLASS, "getTracerProvider", MethodType.methodType(TRACER_PROVIDER_CLASS));
                getGlobalProviderInvoker = LOOKUP.findStatic(GLOBAL_OTEL_CLASS, "getTracerProvider",
                    MethodType.methodType(TRACER_PROVIDER_CLASS));

                MethodHandle noopProviderInvoker
                    = LOOKUP.findStatic(TRACER_PROVIDER_CLASS, "noop", MethodType.methodType(TRACER_PROVIDER_CLASS));
                noopProvider = noopProviderInvoker.invoke();
            } catch (Throwable t) {
                OTelInitializer.initError(LOGGER, t);
            }
        }

        GET_PROVIDER_INVOKER = getProviderInvoker;
        GET_GLOBAL_PROVIDER_INVOKER = getGlobalProviderInvoker;
        NOOP_PROVIDER = noopProvider;
    }

    @Override
    public Tracer getTracer(TelemetryOptions<?> applicationOptions, LibraryTelemetryOptions libraryOptions) {
        Objects.requireNonNull(libraryOptions, "'libraryOptions' cannot be null");

        if (!OTelInitializer.isInitialized()
            || (applicationOptions != null && !applicationOptions.isTracingEnabled())) {
            return OTelTracer.NOOP;
        }

        Object otel = applicationOptions == null ? null : applicationOptions.getProvider();
        Object otelTracerProvider = getTracerProvider(otel);

        if (otelTracerProvider == null || otelTracerProvider == NOOP_PROVIDER) {
            return OTelTracer.NOOP;
        }

        return OTelTracer.createTracer(otelTracerProvider, libraryOptions);
    }

    private Object getTracerProvider(Object otel) {
        if (otel != null && !OTEL_CLASS.isInstance(otel)) {
            throw LOGGER.atError()
                .addKeyValue("expectedProvider", OTEL_CLASS.getName())
                .addKeyValue("actualProvider", otel.getClass().getName())
                .log("Unexpected telemetry provider type.",
                    new IllegalArgumentException("Telemetry provider is not an instance of " + OTEL_CLASS.getName()));
        }

        if (OTelInitializer.isInitialized()) {
            try {
                if (OTEL_CLASS.isInstance(otel)) {
                    return GET_PROVIDER_INVOKER.invoke(otel);
                } else {
                    return GET_GLOBAL_PROVIDER_INVOKER.invoke();
                }
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }
        return null;
    }
}
