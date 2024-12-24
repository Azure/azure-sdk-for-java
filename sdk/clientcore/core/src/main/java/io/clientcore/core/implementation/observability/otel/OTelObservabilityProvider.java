package io.clientcore.core.implementation.observability.otel;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.observability.otel.tracing.OTelTracer;
import io.clientcore.core.implementation.observability.otel.tracing.OTelTracerBuilder;
import io.clientcore.core.observability.LibraryObservabilityOptions;
import io.clientcore.core.observability.ObservabilityOptions;
import io.clientcore.core.observability.ObservabilityProvider;
import io.clientcore.core.observability.tracing.Tracer;
import io.clientcore.core.util.ClientLogger;

import java.util.Objects;

import static io.clientcore.core.implementation.observability.otel.OTelInitializer.GLOBAL_OTEL_CLASS;
import static io.clientcore.core.implementation.observability.otel.OTelInitializer.OTEL_CLASS;
import static io.clientcore.core.implementation.observability.otel.OTelInitializer.TRACER_PROVIDER_CLASS;

public class OTelObservabilityProvider implements ObservabilityProvider {

    private static final ReflectiveInvoker GET_PROVIDER_INVOKER;
    private static final ReflectiveInvoker GET_GLOBAL_PROVIDER_INVOKER;
    private static final ReflectiveInvoker GET_TRACER_BUILDER_INVOKER;

    private static final Object NOOP_PROVIDER;
    private static final ClientLogger LOGGER = new ClientLogger(OTelObservabilityProvider.class);
    static {
        ReflectiveInvoker getProviderInvoker = null;
        ReflectiveInvoker getGlobalProviderInvoker = null;
        ReflectiveInvoker getTracerBuilderInvoker = null;

        Object noopProvider = null;

        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                getProviderInvoker = ReflectionUtils.getMethodInvoker(OTEL_CLASS,
                    OTEL_CLASS.getMethod("getTracerProvider"));
                getGlobalProviderInvoker = ReflectionUtils.getMethodInvoker(GLOBAL_OTEL_CLASS,
                    GLOBAL_OTEL_CLASS.getMethod("getTracerProvider"));
                getTracerBuilderInvoker = ReflectionUtils.getMethodInvoker(TRACER_PROVIDER_CLASS,
                    TRACER_PROVIDER_CLASS.getMethod("tracerBuilder", String.class));

                ReflectiveInvoker noopProviderInvoker = ReflectionUtils.getMethodInvoker(TRACER_PROVIDER_CLASS,
                    TRACER_PROVIDER_CLASS.getMethod("noop"));

                noopProvider = noopProviderInvoker.invokeStatic();
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.initError(LOGGER, t);
            }
        }

        GET_PROVIDER_INVOKER = getProviderInvoker;
        GET_GLOBAL_PROVIDER_INVOKER = getGlobalProviderInvoker;
        GET_TRACER_BUILDER_INVOKER = getTracerBuilderInvoker;
        NOOP_PROVIDER = noopProvider;
    }

    @Override
    public Tracer getTracer(ObservabilityOptions<?> applicationOptions, LibraryObservabilityOptions libraryOptions) {
        Objects.requireNonNull(libraryOptions, "'libraryOptions' cannot be null");

        if (!OTelInitializer.INSTANCE.isInitialized() || (applicationOptions != null && !applicationOptions.isTracingEnabled())) {
            return OTelTracer.NOOP;
        }

        Object otel = applicationOptions == null ? null : applicationOptions.getProvider();
        Object otelTracerProvider = getTracerProvider(otel);

        if (otelTracerProvider == null || otelTracerProvider == NOOP_PROVIDER) {
            return OTelTracer.NOOP;
        }

        OTelTracerBuilder otelTracerBuilder = getTracerBuilder(otelTracerProvider, libraryOptions.getLibraryName());
        return otelTracerBuilder
            .setInstrumentationVersion(libraryOptions.getLibraryVersion())
            .setSchemaUrl(libraryOptions.getSchemaUrl())
            .build();
    }

    private Object getTracerProvider(Object otel) {
        if (otel != null && !OTEL_CLASS.isInstance(otel)) {
            IllegalArgumentException error = new IllegalArgumentException("Observability provider is not an instance of " + OTEL_CLASS.getName());

            throw LOGGER.atError()
                .addKeyValue("expectedProvider", OTEL_CLASS.getName())
                .addKeyValue("actualProvider", otel.getClass().getName())
                .log("Unexpected observability provider type.", error);
        }

        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                if (OTEL_CLASS.isInstance(otel)) {
                    return GET_PROVIDER_INVOKER.invokeWithArguments(otel);
                } else {
                    return GET_GLOBAL_PROVIDER_INVOKER.invokeStatic();
                }
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }
        return null;
    }

    private OTelTracerBuilder getTracerBuilder(Object otelTracerProvider, String instrumentationName) {
        if (OTelInitializer.INSTANCE.isInitialized()) {
            try {
                Object tracerBuilder = GET_TRACER_BUILDER_INVOKER.invokeWithArguments(otelTracerProvider, instrumentationName);
                return new OTelTracerBuilder(tracerBuilder);
            } catch (Throwable t) {
                OTelInitializer.INSTANCE.runtimeError(LOGGER, t);
            }
        }
        return null;
    }
}
