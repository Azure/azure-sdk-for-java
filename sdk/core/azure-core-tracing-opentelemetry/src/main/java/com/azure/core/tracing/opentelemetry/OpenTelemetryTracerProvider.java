// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.LibraryTelemetryOptions;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;

import java.util.Objects;

/**
 * Resolves and provides {@link Tracer} implementation.
 * <p>
 * This class is intended to be used by Azure client libraries and provides abstraction over possible tracing
 * implementations.
 * Application developers should use tracing libraries such as OpenTelemetry or Spring tracing.
 */
public final class OpenTelemetryTracerProvider implements TracerProvider {
    /**
     * Creates an instance of {@link OpenTelemetryTracerProvider}.
     */
    public OpenTelemetryTracerProvider() {
    }

    /**
     * Creates named and versioned OpenTelemetry-based implementation of {@link Tracer}
     *
     * @param libraryName Azure client library package name
     * @param libraryVersion Azure client library version
     * @param azNamespace Azure Resource Provider namespace.
     * @param applicationOptions instance of {@link com.azure.core.util.TracingOptions} passed by the application.
     * @return a tracer instance.
     */
    @Override
    public Tracer createTracer(String libraryName, String libraryVersion, String azNamespace,
        TracingOptions applicationOptions) {
        Objects.requireNonNull(libraryName, "'libraryName' cannot be null.");

        final LibraryTelemetryOptions libraryOptions
            = new LibraryTelemetryOptions(libraryName).setLibraryVersion(libraryVersion)
                .setResourceProviderNamespace(azNamespace);
        return new OpenTelemetryTracer(libraryOptions, applicationOptions);
    }

    /**
     * Creates OpenTelemetry-based implementation of {@link Tracer}
     *
     * @param libraryOptions Library-specific telemetry options.
     * @param applicationOptions Tracing options configured by the application.
     * @return a tracer instance.
     */
    @Override
    public Tracer createTracer(LibraryTelemetryOptions libraryOptions, TracingOptions applicationOptions) {
        Objects.requireNonNull(libraryOptions, "'libraryOptions' cannot be null.");

        return new OpenTelemetryTracer(libraryOptions, applicationOptions);
    }
}
