// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.tracing;

import com.azure.core.implementation.util.Providers;
import com.azure.core.util.Configuration;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.logging.ClientLogger;

import java.util.Iterator;
import java.util.Objects;
import java.util.ServiceLoader;

final class DefaultTracerProvider implements TracerProvider {
    private static final String NO_DEFAULT_PROVIDER = "A request was made to load the default TracerProvider provider "
        + "but one could not be found on the classpath. If you are using a dependency manager, consider including a "
        + "dependency on azure-core-tracing-opentelemetry, or adding instrumentation java agent.";

    private static final String CANNOT_FIND_SPECIFIC_PROVIDER = "A request was made to use a specific "
        + "TracerProvider to create an instance of Tracer but it wasn't found on the classpath. If you're "
        + "using a dependency manager ensure you're including the dependency that provides the specific "
        + "implementation. If you're including the specific implementation ensure that the TracerProvider service "
        + "it supplies is being included in the 'META-INF/services' file 'com.azure.core.util.tracing.TracerProvider'. "
        + "The requested TracerProvider was: ";

    private static final TracerProvider INSTANCE = new DefaultTracerProvider();
    private static final ClientLogger LOGGER = new ClientLogger(DefaultTracerProvider.class);
    private static final TracingOptions DEFAULT_OPTIONS = TracingOptions.fromConfiguration(Configuration.getGlobalConfiguration());
    private static Providers<TracerProvider> tracerProvider = new Providers<>(TracerProvider.class);
    private static final Tracer FALLBACK_TRACER = createFallbackTracer();

    private DefaultTracerProvider() {
    }


    private static Tracer createFallbackTracer() {
        // backward compatibility with preview OTel plugin - it didn't have TracerProvider
        ServiceLoader<Tracer> serviceLoader = ServiceLoader.load(Tracer.class, Tracer.class.getClassLoader());
        Iterator<Tracer> iterator = serviceLoader.iterator();
        if (iterator.hasNext()) {
            Tracer tracer = iterator.next();
            LOGGER.info("Found Tracer implementation on the classpath: {}", tracer.getClass().getName());
            return tracer;
        }

        return NoopTracer.INSTANCE;
    }

    static TracerProvider getInstance() {
        return INSTANCE;
    }

    public Tracer createTracer(String libraryName, String libraryVersion, String azNamespace, TracingOptions options) {
        Objects.requireNonNull(libraryName, "'libraryName' cannot be null.");

        final TracingOptions finalOptions = options != null ? options : DEFAULT_OPTIONS;

        if (finalOptions.isEnabled()) {
            return tracerProvider.createInstance((provider) -> provider.createTracer(libraryName, libraryVersion, azNamespace, finalOptions),
                FALLBACK_TRACER, null, NO_DEFAULT_PROVIDER, CANNOT_FIND_SPECIFIC_PROVIDER);
        }

        return NoopTracer.INSTANCE;
    }
}
