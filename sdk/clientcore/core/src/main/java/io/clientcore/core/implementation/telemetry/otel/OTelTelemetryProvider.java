// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.telemetry.otel;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.telemetry.otel.tracing.OTelTracer;
import io.clientcore.core.telemetry.LibraryTelemetryOptions;
import io.clientcore.core.telemetry.TelemetryOptions;
import io.clientcore.core.telemetry.TelemetryProvider;
import io.clientcore.core.telemetry.tracing.Tracer;
import io.clientcore.core.util.ClientLogger;

import java.util.Objects;

import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.GLOBAL_OTEL_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.OTEL_CLASS;
import static io.clientcore.core.implementation.telemetry.otel.OTelInitializer.TRACER_PROVIDER_CLASS;

public class OTelTelemetryProvider implements TelemetryProvider {
    public static final TelemetryProvider INSTANCE = new OTelTelemetryProvider();
    private static final ReflectiveInvoker GET_PROVIDER_INVOKER;
    private static final ReflectiveInvoker GET_GLOBAL_PROVIDER_INVOKER;

    private static final Object NOOP_PROVIDER;
    private static final ClientLogger LOGGER = new ClientLogger(OTelTelemetryProvider.class);
    static {
        ReflectiveInvoker getProviderInvoker = null;
        ReflectiveInvoker getGlobalProviderInvoker = null;

        Object noopProvider = null;

        if (OTelInitializer.isInitialized()) {
            try {
                getProviderInvoker
                    = ReflectionUtils.getMethodInvoker(OTEL_CLASS, OTEL_CLASS.getMethod("getTracerProvider"));
                getGlobalProviderInvoker = ReflectionUtils.getMethodInvoker(GLOBAL_OTEL_CLASS,
                    GLOBAL_OTEL_CLASS.getMethod("getTracerProvider"));

                ReflectiveInvoker noopProviderInvoker
                    = ReflectionUtils.getMethodInvoker(TRACER_PROVIDER_CLASS, TRACER_PROVIDER_CLASS.getMethod("noop"));

                noopProvider = noopProviderInvoker.invokeStatic();
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
                    return GET_PROVIDER_INVOKER.invokeWithArguments(otel);
                } else {
                    return GET_GLOBAL_PROVIDER_INVOKER.invokeStatic();
                }
            } catch (Throwable t) {
                OTelInitializer.runtimeError(LOGGER, t);
            }
        }
        return null;
    }
}
