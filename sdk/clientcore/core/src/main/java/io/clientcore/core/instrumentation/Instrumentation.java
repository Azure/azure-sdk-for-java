// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.implementation.instrumentation.fallback.FallbackInstrumentation;
import io.clientcore.core.implementation.instrumentation.otel.OTelInitializer;
import io.clientcore.core.implementation.instrumentation.otel.OTelInstrumentation;
import io.clientcore.core.instrumentation.metrics.Meter;
import io.clientcore.core.instrumentation.tracing.TraceContextPropagator;
import io.clientcore.core.instrumentation.tracing.Tracer;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

import static io.clientcore.core.implementation.instrumentation.InstrumentationUtils.getServerPort;

/**
 * A container that can resolve observability provider and its components. Only OpenTelemetry is supported.
 */
public interface Instrumentation {
    /**
     * Gets or creates the tracer associated with this instrumentation instance.
     * <p>
     * Tracer lifetime should usually match the client lifetime. Avoid creating new tracers for each request.
     *
     * <p><strong>This method is intended to be used by client libraries. Application developers
     * should use OpenTelemetry API directly</strong></p>
     *
     * <!-- src_embed io.clientcore.core.instrumentation.gettracer -->
     * <pre>
     *
     * SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions&#40;&quot;sample&quot;&#41;
     *     .setSdkVersion&#40;&quot;1.0.0&quot;&#41;
     *     .setSchemaUrl&#40;&quot;https:&#47;&#47;opentelemetry.io&#47;schemas&#47;1.29.0&quot;&#41;;
     *
     * InstrumentationOptions instrumentationOptions = new InstrumentationOptions&#40;&#41;;
     * Instrumentation instrumentation = Instrumentation.create&#40;instrumentationOptions, sdkOptions&#41;;
     *
     * Tracer tracer = instrumentation.getTracer&#40;&#41;;
     *
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.gettracer -->
     *
     * @return The tracer.
     */
    Tracer getTracer();

    /**
     * Gets or creates the meter associated with this instrumentation instance.
     * <p>
     * Meter lifetime should usually match the client lifetime. Avoid creating new meters for each request.
     *
     * <p><strong>This method is intended to be used by client libraries. Application developers
     * should use OpenTelemetry API directly</strong></p>
     *
     * <!-- src_embed io.clientcore.core.instrumentation.getmeter -->
     * <pre>
     *
     * SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions&#40;&quot;sample&quot;&#41;
     *     .setSdkVersion&#40;&quot;1.0.0&quot;&#41;
     *     .setSchemaUrl&#40;&quot;https:&#47;&#47;opentelemetry.io&#47;schemas&#47;1.29.0&quot;&#41;;
     *
     * InstrumentationOptions instrumentationOptions = new InstrumentationOptions&#40;&#41;;
     * Instrumentation instrumentation = Instrumentation.create&#40;instrumentationOptions, sdkOptions&#41;;
     * Meter meter = instrumentation.getMeter&#40;&#41;;
     *
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.getmeter -->
     *
     * @return The meter.
     */
    Meter getMeter();

    /**
     * Converts the given attributes into the implementation-specific attributes.
     * Reuse created attributes when possible between operations to avoid unnecessary overhead.
     *
     * <p><strong>This method is intended to be used by client libraries. Application developers
     * should use OpenTelemetry API directly</strong></p>
     * <!-- src_embed io.clientcore.core.instrumentation.createattributes -->
     * <pre>
     * SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions&#40;&quot;sample&quot;&#41;
     *     .setSdkVersion&#40;&quot;1.0.0&quot;&#41;
     *     .setSchemaUrl&#40;&quot;https:&#47;&#47;opentelemetry.io&#47;schemas&#47;1.29.0&quot;&#41;;
     *
     * InstrumentationOptions instrumentationOptions = new InstrumentationOptions&#40;&#41;;
     *
     * Instrumentation instrumentation = Instrumentation.create&#40;instrumentationOptions, sdkOptions&#41;;
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
     * Instruments a client call which includes distributed tracing and duration metric.
     * Created span becomes current and is used to correlate all telemetry reported under it such as other spans, logs, or metrics exemplars.
     * <p>
     * The method updates the {@link RequestContext} object with the instrumentation context that should be used for the call.
     * <!-- src_embed io.clientcore.core.instrumentation.instrumentwithresponse -->
     * <pre>
     * return instrumentation.instrumentWithResponse&#40;&quot;Sample.download&quot;, context, this::downloadImpl&#41;;
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.instrumentwithresponse -->
     *
     * @param operationName the name of the operation, it should be fully-qualified, language-agnostic method definition name such as TypeSpec's crossLanguageDefinitionId
     *                      or OpenAPI operationId.
     * @param requestContext the request options.
     * @param operation the operation to instrument. Note: the operation is executed in the scope of the instrumentation and should use updated request options passed to it.
     * @param <TResponse> the type of the response.
     * @return the response.
     * @throws RuntimeException if the call throws a runtime exception.
     */
    <TResponse> TResponse instrumentWithResponse(String operationName, RequestContext requestContext,
        Function<RequestContext, TResponse> operation);

    /**
     * Instruments a client call which includes distributed tracing and duration metric.
     * Created span becomes current and is used to correlate all telemetry reported under it such as other spans, logs, or metrics exemplars.
     * <p>
     * The method updates the {@link RequestContext} object with the instrumentation context that should be used for the call.
     * <!-- src_embed io.clientcore.core.instrumentation.instrument -->
     * <pre>
     * instrumentation.instrument&#40;&quot;Sample.create&quot;, context, this::createImpl&#41;;
     * </pre>
     * <!-- end io.clientcore.core.instrumentation.instrument -->
     *
     * @param operationName the name of the operation, it should be fully-qualified, language-agnostic method definition name such as TypeSpec's crossLanguageDefinitionId
     *                      or OpenAPI operationId.
     * @param requestContext the request options.
     * @param operation the operation to instrument. Note: the operation is executed in the scope of the instrumentation and should use updated request options passed to it.
     * @throws RuntimeException if the call throws a runtime exception.
     */
    default void instrument(String operationName, RequestContext requestContext, Consumer<RequestContext> operation) {
        instrumentWithResponse(operationName, requestContext, updatedContext -> {
            operation.accept(updatedContext);
            return null;
        });
    }

    /**
     * Gets the singleton instance of the resolved telemetry provider.
     *
     * <p><strong>This method is intended to be used by client libraries. Application developers
     * should use OpenTelemetry API directly</strong></p>
     *
     * @param applicationOptions Telemetry collection options provided by the application.
     * @param sdkOptions Library-specific instrumentation options.
     * @return The instance of telemetry provider implementation.
     */
    static Instrumentation create(InstrumentationOptions applicationOptions, SdkInstrumentationOptions sdkOptions) {
        Objects.requireNonNull(sdkOptions, "'sdkOptions' cannot be null");

        String host = null;
        int port = -1;
        if (sdkOptions.getEndpoint() != null) {
            URI uri = URI.create(sdkOptions.getEndpoint());
            host = uri.getHost();
            port = getServerPort(uri);
        }

        if (OTelInitializer.isInitialized()) {
            return new OTelInstrumentation(applicationOptions, sdkOptions, host, port);
        } else {
            return new FallbackInstrumentation(applicationOptions, sdkOptions, host, port);
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
     * RequestContext context = RequestContext.builder&#40;&#41;
     *     .setInstrumentationContext&#40;new MyInstrumentationContext&#40;&quot;e4eaaaf2d48f4bf3b299a8a2a2a77ad7&quot;, &quot;5e0c63257de34c56&quot;&#41;&#41;
     *     .build&#40;&#41;;
     *
     * &#47;&#47; run on another thread
     * client.downloadContent&#40;context&#41;;
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
     *
     * SampleClient client = new SampleClientBuilder&#40;&#41;.build&#40;&#41;;
     *
     * &#47;&#47; Propagating context implicitly is preferred way in synchronous code.
     * &#47;&#47; However, in asynchronous code, context may need to be propagated explicitly using RequestContext
     * &#47;&#47; and explicit io.clientcore.core.util.Context.
     *
     * RequestContext context = RequestContext.builder&#40;&#41;
     *     .setInstrumentationContext&#40;Instrumentation.createInstrumentationContext&#40;span&#41;&#41;
     *     .build&#40;&#41;;
     *
     * &#47;&#47; run on another thread - all telemetry will be correlated with the span created above
     * client.clientCall&#40;context&#41;;
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
