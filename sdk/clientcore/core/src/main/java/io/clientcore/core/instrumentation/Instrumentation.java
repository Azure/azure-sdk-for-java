// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.implementation.instrumentation.otel.OTelInstrumentation;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.Tracer;

import java.util.Objects;

import static io.clientcore.core.instrumentation.NoopInstrumentation.NOOP_PROVIDER;

/**
 * A container that can resolve observability provider and its components. Only OpenTelemetry is supported.
 *
 * <p><strong>This interface is intended to be used by client libraries. Application developers
 * should use OpenTelemetry API directly</strong></p>
 */
public interface Instrumentation {
    /**
     * The key used to disable tracing on a per-request basis.
     * To disable tracing, set this key to {@code true} on the request context.
     */
    String DISABLE_TRACING_KEY = "disable-tracing";

    /**
     * The key used to set the parent trace context explicitly.
     * To set the trace context, set this key to a value of {@code io.opentelemetry.context.Context}.
     */
    String TRACE_CONTEXT_KEY = "trace-context";

    /**
     * Gets the tracer.
     * <p>
     * Tracer lifetime should usually match the client lifetime. Avoid creating new tracers for each request.
     *
     * <p><strong>This method is intended to be used by client libraries. Application developers
     * should use OpenTelemetry API directly</strong></p>
     *
     * <!-- src_embed io.clientcore.core.telemetry.tracing.createtracer -->
     * <pre>
     *
     * LibraryInstrumentationOptions libraryOptions = new LibraryInstrumentationOptions&#40;&quot;sample&quot;&#41;
     *     .setLibraryVersion&#40;&quot;1.0.0&quot;&#41;
     *     .setSchemaUrl&#40;&quot;https:&#47;&#47;opentelemetry.io&#47;schemas&#47;1.29.0&quot;&#41;;
     *
     * InstrumentationOptions&lt;?&gt; instrumentationOptions = new InstrumentationOptions&lt;&gt;&#40;&#41;;
     *
     * Tracer tracer = Instrumentation.create&#40;instrumentationOptions, libraryOptions&#41;.getTracer&#40;&#41;;
     *
     * </pre>
     * <!-- end io.clientcore.core.telemetry.tracing.createtracer -->
     *
     * @return The tracer.
     */
    Tracer getTracer();

    /**
     * Gets the implementation of W3C Trace Context propagator.
     *
     * @return The context propagator.
     */
    TraceContextPropagator getW3CTraceContextPropagator();

    /**
     * Gets the singleton instance of the resolved telemetry provider.
     *
     * @param applicationOptions Telemetry collection options provided by the application.
     * @param libraryOptions Library-specific telemetry collection options.
     * @return The instance of telemetry provider implementation.
     */
    static Instrumentation create(InstrumentationOptions<?> applicationOptions,
        LibraryInstrumentationOptions libraryOptions) {
        Objects.requireNonNull(libraryOptions, "'libraryOptions' cannot be null");
        if (OTelInitializer.isInitialized()) {
            return new OTelInstrumentation(applicationOptions, libraryOptions);
        } else {
            return NOOP_PROVIDER;
        }
    }
}
