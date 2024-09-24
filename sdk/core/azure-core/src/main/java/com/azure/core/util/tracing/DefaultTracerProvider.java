// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.tracing;

import com.azure.core.implementation.util.Providers;
import com.azure.core.util.LibraryTelemetryOptions;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;

import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;

final class DefaultTracerProvider implements TracerProvider {
    private static final String NO_DEFAULT_PROVIDER = "A request was made to load the default TracerProvider provider "
        + "but one could not be found on the classpath. If you are using a dependency manager, consider including a "
        + "dependency on azure-core-tracing-opentelemetry or enabling instrumentation java agent.";

    private static final TracerProvider INSTANCE = new DefaultTracerProvider();
    private static final ClientLogger LOGGER = new ClientLogger(DefaultTracerProvider.class);
    private static final TracingOptions DEFAULT_OPTIONS = new TracingOptions();
    private static final Providers<TracerProvider, Tracer> TRACER_PROVIDERS
        = new Providers<>(TracerProvider.class, null, NO_DEFAULT_PROVIDER);
    private static final Tracer FALLBACK_TRACER = createFallbackTracer();

    private DefaultTracerProvider() {
    }

    private static Tracer createFallbackTracer() {
        // backward compatibility with preview OTel plugin - it didn't have TracerProvider
        ServiceLoader<Tracer> serviceLoader = ServiceLoader.load(Tracer.class, Tracer.class.getClassLoader());
        Iterator<Tracer> iterator = serviceLoader.iterator();
        if (iterator.hasNext()) {
            Tracer tracer = iterator.next();
            LOGGER.log(LogLevel.INFORMATIONAL,
                () -> "Found Tracer implementation on the classpath: " + tracer.getClass().getName());
            return tracer;
        }

        return NoopTracer.INSTANCE;
    }

    static TracerProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public Tracer createTracer(String libraryName, String libraryVersion, String azNamespace,
        TracingOptions applicationOptions) {
        Objects.requireNonNull(libraryName, "'libraryName' cannot be null.");

        final LibraryTelemetryOptions libraryOptions
            = new LibraryTelemetryOptions(libraryName).setLibraryVersion(libraryVersion)
                .setResourceProviderNamespace(azNamespace);

        return createTracer(libraryOptions, applicationOptions);
    }

    @Override
    public Tracer createTracer(LibraryTelemetryOptions libraryOptions, TracingOptions applicationOptions) {
        Objects.requireNonNull(libraryOptions, "'libraryOptions' cannot be null.");

        final TracingOptions finalOptions = applicationOptions != null ? applicationOptions : DEFAULT_OPTIONS;

        if (finalOptions.isEnabled()) {
            return TRACER_PROVIDERS.create((provider) -> provider.createTracer(libraryOptions, finalOptions),
                FALLBACK_TRACER, finalOptions.getTracerProvider());
        }

        return NoopTracer.INSTANCE;
    }
}
