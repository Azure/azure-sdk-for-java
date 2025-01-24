// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.models.HttpInstrumentationOptions;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpInstrumentationPolicy;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.TracingScope;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Application developers are expected to configure OpenTelemetry
 * to leverage instrumentation code in client libraries.
 * <p>
 *
 * It can be done by
 * 1. providing javaagent based on OpenTelemetry
 * 2. setting configured OpenTelemetry SDK as global
 * 3. setting up OpenTelemetry SDK and providing it to client libraries
 *    explicitly.
 * <p>
 *
 * Refer to <a href="https://opentelemetry.io/docs/languages/java/configuration">OpenTelemetry documentation</a> for
 * the details on how to configure OpenTelemetry.
 * <p>
 *
 * Option 1 (javaagent) and Options 2 do not involve any code changes specific to
 * client libraries which discover and use global OpenTelemetry instance.
 * <p>
 *
 * See {@link TelemetryJavaDocCodeSnippets#useGlobalOpenTelemetry()} for Option 2,
 * {@link TelemetryJavaDocCodeSnippets#useExplicitOpenTelemetry()} for Option 3.
 *
 */
public class TelemetryJavaDocCodeSnippets {

    /**
     * This code snippet shows how to initialize global OpenTelemetry SDK
     * and let client libraries discover it.
     */
    public void useGlobalOpenTelemetry() {
        // BEGIN: io.clientcore.core.telemetry.useglobalopentelemetry

        AutoConfiguredOpenTelemetrySdk.initialize();

        SampleClient client = new SampleClientBuilder().build();

        // this call will be traced using OpenTelemetry SDK initialized globally
        client.clientCall();

        // END: io.clientcore.core.telemetry.useglobalopentelemetry
    }

    /**
     * This code snippet shows how to pass OpenTelemetry SDK instance
     * to client libraries explicitly.
     */
    public void useExplicitOpenTelemetry() {
        // BEGIN: io.clientcore.core.telemetry.useexplicitopentelemetry

        OpenTelemetry openTelemetry = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();
        HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions()
            .setTelemetryProvider(openTelemetry);

        SampleClient client = new SampleClientBuilder().instrumentationOptions(instrumentationOptions).build();

        // this call will be traced using OpenTelemetry SDK provided explicitly
        client.clientCall();

        // END: io.clientcore.core.telemetry.useexplicitopentelemetry
    }

    /**
     * This code snippet shows how to disable distributed tracing
     * for a specific instance of client.
     */
    public void disableDistributedTracing() {
        // BEGIN: io.clientcore.core.telemetry.disabledistributedtracing

        HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions()
            .setTracingEnabled(false);

        SampleClient client = new SampleClientBuilder().instrumentationOptions(instrumentationOptions).build();
        client.clientCall();

        // END: io.clientcore.core.telemetry.disabledistributedtracing
    }

    /**
     * This code snippet shows how to correlate spans from
     * client library with spans from application code
     * using current context.
     */
    @SuppressWarnings("try")
    public void correlationWithImplicitContext() {
        // BEGIN: io.clientcore.core.telemetry.correlationwithimplicitcontext

        Tracer tracer = GlobalOpenTelemetry.getTracer("sample");
        Span span = tracer.spanBuilder("my-operation")
            .startSpan();
        SampleClient client = new SampleClientBuilder().build();

        try (Scope scope = span.makeCurrent()) {
            // Client library will create span for the clientCall operation
            // and will use current span (my-operation) as a parent.
            client.clientCall();
        } finally {
            span.end();
        }

        // END: io.clientcore.core.telemetry.correlationwithimplicitcontext
    }

    /**
     * This code snippet shows how to correlate spans from
     * client library with spans from application code
     * by passing context explicitly.
     */
    public void correlationWithExplicitContext() {
        // BEGIN: io.clientcore.core.telemetry.correlationwithexplicitcontext

        Tracer tracer = GlobalOpenTelemetry.getTracer("sample");
        Span span = tracer.spanBuilder("my-operation")
            .startSpan();
        SampleClient client = new SampleClientBuilder().build();

        // Propagating context implicitly is preferred way in synchronous code.
        // However, in asynchronous code, context may need to be propagated explicitly using RequestOptions
        // and explicit io.clientcore.core.util.Context.

        RequestOptions options = new RequestOptions()
            .setInstrumentationContext(Instrumentation.createInstrumentationContext(span));

        // run on another thread - all telemetry will be correlated with the span created above
        client.clientCall(options);

        // END: io.clientcore.core.telemetry.correlationwithexplicitcontext
    }

    static class SampleClientBuilder {
        private HttpInstrumentationOptions instrumentationOptions;
        // TODO (limolkova): do we need InstrumentationTrait?
        public SampleClientBuilder instrumentationOptions(HttpInstrumentationOptions instrumentationOptions) {
            this.instrumentationOptions = instrumentationOptions;
            return this;
        }

        public SampleClient build() {
            return new SampleClient(instrumentationOptions, new HttpPipelineBuilder()
                .policies(new HttpInstrumentationPolicy(instrumentationOptions))
                .build());
        }
    }

    static class SampleClient {
        private final static LibraryInstrumentationOptions LIBRARY_OPTIONS = new LibraryInstrumentationOptions("sample");
        private final HttpPipeline httpPipeline;
        private final io.clientcore.core.instrumentation.tracing.Tracer tracer;

        SampleClient(InstrumentationOptions instrumentationOptions, HttpPipeline httpPipeline) {
            this.httpPipeline = httpPipeline;
            this.tracer = Instrumentation.create(instrumentationOptions, LIBRARY_OPTIONS).getTracer();
        }

        public void clientCall() {
            this.clientCall(null);
        }

        @SuppressWarnings("try")
        public void clientCall(RequestOptions options) {
            io.clientcore.core.instrumentation.tracing.Span span = tracer.spanBuilder("clientCall", SpanKind.CLIENT, null)
                .startSpan();

            if (options == null) {
                options = new RequestOptions();
            }

            options.setInstrumentationContext(span.getInstrumentationContext());

            try (TracingScope scope = span.makeCurrent()) {
                Response<?> response = httpPipeline.send(new HttpRequest(HttpMethod.GET, "https://example.com"));
                response.close();
                span.end();
            } catch (Throwable t) {
                span.end(t);

                if (t instanceof IOException) {
                    throw new UncheckedIOException((IOException) t);
                } else if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else {
                    throw new RuntimeException(t);
                }
            }
        }
    }
}
