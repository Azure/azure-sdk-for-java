// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.telemetry;

import io.clientcore.core.implementation.telemetry.otel.OTelInitializer;
import io.clientcore.core.implementation.telemetry.otel.OTelTelemetryProvider;
import io.clientcore.core.telemetry.tracing.Tracer;

import static io.clientcore.core.telemetry.NoopTelemetryProvider.NOOP_PROVIDER;

/**
 * Provides observability capabilities (distributed tracing, metrics, etc.) with OpenTelemetry to the client library.
 * <p>
 *
 * This interface should only be used by client libraries. It is not intended to be used directly by the end users.
 */
public interface TelemetryProvider {
    String DISABLE_TRACING_KEY = "disable-tracing";
    String TRACE_CONTEXT_KEY = "trace-context";

    /**
     * Gets the tracer.
     *
     * @param applicationOptions Options provided by the application.
     * @param libraryOptions Options provided by the library.
     * @return The tracer.
     */
    Tracer getTracer(TelemetryOptions<?> applicationOptions, LibraryTelemetryOptions libraryOptions);

    /**
     * Gets the singleton instance of the resolved telemetry provider.
     *
     * @return The singleton instance of the resolved telemetry provider.
     */
    static TelemetryProvider getInstance() {
        if (OTelInitializer.INSTANCE.isInitialized()) {
            return OTelTelemetryProvider.INSTANCE;
        } else {
            return NOOP_PROVIDER;
        }
    }
}
