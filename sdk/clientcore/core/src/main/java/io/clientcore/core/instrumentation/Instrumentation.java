// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.implementation.instrumentation.fallback.FallbackInstrumentation;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.implementation.instrumentation.otel.OTelInstrumentation;
import io.clientcore.core.instrumentation.metrics.Meter;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.Tracer;

import java.util.Map;
import java.util.Objects;

/**
 * A container that can resolve observability provider and its components. Only OpenTelemetry is supported.
 */
public interface Instrumentation {
    /**
     * Creates the tracer.
     * <p>
     * Tracer lifetime should usually match the client lifetime. Avoid creating new tracers for each request.
     *
     * <p><strong>This method is intended to be used by client libraries. Application developers
     * should use OpenTelemetry API directly</strong></p>
     *
     * <!-- src_embed io.clientcore.core.instrumentation.createtracer -->
     * <pre>
     *
     * LibraryInstrumentationOptions libraryOptions = new LibraryInstrumentationOptions&#40;&quot;sample&quot;&#41;
     *     .setLibraryVersion&#40;&quot;1.0.0&quot;&#41;
     *     .setSchemaUrl&#40;&quot;https:&#47;&#47;opentelemetry.io&#47;schemas&#47;1.29.0&quot;&#41;;
     *
     * InstrumentationOptions instrumentationOptions = new InstrumentationOptions&#40;&#41;;
     * Instrumentation instrumentation = Instrumentation.create&#40;instrumentationOptions, libraryOptions&#41;;
     *
     * Tracer tracer = instrumentation.createTracer&#40;&#41;;
     *
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.createtracer -->
     *
     * @return The tracer.
     */
    Tracer createTracer();

    /**
     * Creates the meter.
     * <p>
     * Meter lifetime should usually match the client lifetime. Avoid creating new meters for each request.
     *
     * <p><strong>This method is intended to be used by client libraries. Application developers
     * should use OpenTelemetry API directly</strong></p>
     *
     * <!-- src_embed io.clientcore.core.instrumentation.createmeter -->
     * <pre>
     *
     * LibraryInstrumentationOptions libraryOptions = new LibraryInstrumentationOptions&#40;&quot;sample&quot;&#41;
     *     .setLibraryVersion&#40;&quot;1.0.0&quot;&#41;
     *     .setSchemaUrl&#40;&quot;https:&#47;&#47;opentelemetry.io&#47;schemas&#47;1.29.0&quot;&#41;;
     *
     * InstrumentationOptions instrumentationOptions = new InstrumentationOptions&#40;&#41;;
     * Instrumentation instrumentation = Instrumentation.create&#40;instrumentationOptions, libraryOptions&#41;;
     * Meter meter = instrumentation.createMeter&#40;&#41;;
     * &#47;&#47; Close the meter when it's no longer needed.
     * meter.close&#40;&#41;;
     *
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.createmeter -->
     *
     * @return The meter.
     */
    Meter createMeter();

    /**
     * Converts the given attributes into the implementation-specific attributes.
     * Reuse created attributes when possible between operations to avoid unnecessary overhead.
     *
     * <p><strong>This method is intended to be used by client libraries. Application developers
     * should use OpenTelemetry API directly</strong></p>
     * <!-- src_embed io.clientcore.core.instrumentation.createattributes -->
     * <pre>
     *
     * LibraryInstrumentationOptions libraryOptions = new LibraryInstrumentationOptions&#40;&quot;sample&quot;&#41;
     *     .setLibraryVersion&#40;&quot;1.0.0&quot;&#41;
     *     .setSchemaUrl&#40;&quot;https:&#47;&#47;opentelemetry.io&#47;schemas&#47;1.29.0&quot;&#41;;
     *
     * InstrumentationOptions instrumentationOptions = new InstrumentationOptions&#40;&#41;;
     * Instrumentation instrumentation = Instrumentation.create&#40;instrumentationOptions, libraryOptions&#41;;
     * InstrumentationAttributes attributes = instrumentation
     *     .createAttributes&#40;Collections.singletonMap&#40;&quot;key1&quot;, &quot;value1&quot;&#41;&#41;;
     *
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.createattributes -->
     *
     * @param attributes Attributes to convert to implementation-specific attributes.
     * @return The implementation-specific attributes instance.
     */
    InstrumentationAttributes createAttributes(Map<String, Object> attributes);

    /**
     * Gets the implementation of W3C Trace Context propagator.
     *
     * <p><strong>This method is intended to be used by client libraries. Application developers
     * should use OpenTelemetry API directly</strong></p>
     *
     * @return The context propagator.
     */
    TraceContextPropagator getW3CTraceContextPropagator();

    /**
     * Gets the singleton instance of the resolved telemetry provider.
     *
     * <p><strong>This method is intended to be used by client libraries. Application developers
     * should use OpenTelemetry API directly</strong></p>
     *
     * @param applicationOptions Telemetry collection options provided by the application.
     * @param libraryOptions Library-specific telemetry collection options.
     * @return The instance of telemetry provider implementation.
     */
    static Instrumentation create(InstrumentationOptions applicationOptions,
        LibraryInstrumentationOptions libraryOptions) {
        Objects.requireNonNull(libraryOptions, "'libraryOptions' cannot be null");
        if (OTelInitializer.isInitialized()) {
            return new OTelInstrumentation(applicationOptions, libraryOptions);
        } else {
            return new FallbackInstrumentation(applicationOptions, libraryOptions);
        }
    }

    /**
     * Retrieves the instrumentation context from the given context. The type of the context is determined by the
     * instrumentation implementation.
     * <!-- src_embed io.clientcore.core.telemetry.fallback.correlationwithexplicitcontext -->
     * <pre>
     *
     * SampleClient client = new SampleClientBuilder&#40;&#41;.build&#40;&#41;;
     *
     * RequestOptions options = new RequestOptions&#40;&#41;
     *     .setInstrumentationContext&#40;new MyInstrumentationContext&#40;&quot;e4eaaaf2d48f4bf3b299a8a2a2a77ad7&quot;, &quot;5e0c63257de34c56&quot;&#41;&#41;;
     *
     * &#47;&#47; run on another thread
     * client.downloadContent&#40;options&#41;;
     *
     * </pre>
     * <!-- end io.clientcore.core.telemetry.fallback.correlationwithexplicitcontext -->
     * <p>
     * When using OpenTelemetry, the context can be a {@code io.opentelemetry.api.trace.Span}, {@code io.opentelemetry.api.trace.SpanContext},
     * {@code io.opentelemetry.context.Context} or any implementation of {@link InstrumentationContext}.
     * <!-- src_embed io.clientcore.core.telemetry.correlationwithexplicitcontext -->
     * <pre>
     *
     * Tracer tracer = GlobalOpenTelemetry.getTracer&#40;&quot;sample&quot;&#41;;
     * Span span = tracer.spanBuilder&#40;&quot;my-operation&quot;&#41;
     *     .startSpan&#40;&#41;;
     * SampleClient client = new SampleClientBuilder&#40;&#41;.build&#40;&#41;;
     *
     * &#47;&#47; Propagating context implicitly is preferred way in synchronous code.
     * &#47;&#47; However, in asynchronous code, context may need to be propagated explicitly using RequestOptions
     * &#47;&#47; and explicit io.clientcore.core.util.Context.
     *
     * RequestOptions options = new RequestOptions&#40;&#41;
     *     .setInstrumentationContext&#40;Instrumentation.createInstrumentationContext&#40;span&#41;&#41;;
     *
     * &#47;&#47; run on another thread - all telemetry will be correlated with the span created above
     * client.clientCall&#40;options&#41;;
     *
     * </pre>
     * <!-- end io.clientcore.core.telemetry.fallback.correlationwithexplicitcontext -->
     *
     * @param context the context to retrieve the instrumentation context from.
     * @return the instrumentation context.
     * @param <T> the type of the context.
     */
    static <T> InstrumentationContext createInstrumentationContext(T context) {
        if (OTelInitializer.isInitialized()) {
            return OTelInstrumentation.DEFAULT_INSTANCE.createInstrumentationContext(context);
        } else {
            return FallbackInstrumentation.DEFAULT_INSTANCE.createInstrumentationContext(context);
        }
    }
}
