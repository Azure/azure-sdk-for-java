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
 * <p><strong>This interface is intended to be used by client libraries. Application developers
 * should use OpenTelemetry API directly</strong>
 */
public interface TelemetryProvider {
    String DISABLE_TRACING_KEY = "disable-tracing";
    String TRACE_CONTEXT_KEY = "trace-context";

    /**
     * Gets the tracer.
     * <p>
     * Tracer lifetime should usually match the client lifetime. Avoid creating new tracers for each request.
     * <p>
     * <strong>This method is intended to be used by client libraries. Application developers
     * should use OpenTelemetry API directly</strong>
     * <p>
     * <!-- src_embed io.clientcore.core.telemetry.tracing.createtracer -->
     * <pre>
     *
     * LibraryTelemetryOptions libraryOptions = new LibraryTelemetryOptions&#40;&quot;sample&quot;&#41;
     *     .setLibraryVersion&#40;&quot;1.0.0&quot;&#41;
     *     .setSchemaUrl&#40;&quot;https:&#47;&#47;opentelemetry.io&#47;schemas&#47;1.29.0&quot;&#41;;
     *
     * TelemetryOptions&lt;?&gt; telemetryOptions = new TelemetryOptions&lt;&gt;&#40;&#41;;
     *
     * Tracer tracer = TelemetryProvider.getInstance&#40;&#41;.getTracer&#40;telemetryOptions, libraryOptions&#41;;
     *
     * </pre>
     * <!-- end io.clientcore.core.telemetry.tracing.createtracer -->
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
        if (OTelInitializer.isInitialized()) {
            return OTelTelemetryProvider.INSTANCE;
        } else {
            return NOOP_PROVIDER;
        }
    }
}
