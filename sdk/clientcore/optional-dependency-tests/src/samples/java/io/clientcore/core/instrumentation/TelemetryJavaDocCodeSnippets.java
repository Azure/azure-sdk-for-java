// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.RequestOptions;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.clientcore.core.http.pipeline.InstrumentationPolicy;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

import java.io.IOException;
import java.io.UncheckedIOException;

import static io.clientcore.core.instrumentation.InstrumentationProvider.TRACE_CONTEXT_KEY;

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
        client.clientCall();

        // END: io.clientcore.core.telemetry.useglobalopentelemetry
    }

    /**
     * This code snippet shows how to pass OpenTelemetry SDK instance
     * to client libraries explicitly.
     */
    public void useExplicitOpenTelemetry() {
        // BEGIN: io.clientcore.core.telemetry.useexplicitopentelemetry

        OpenTelemetry openTelemetry =  AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();
        InstrumentationOptions<OpenTelemetry> instrumentationOptions = new InstrumentationOptions<OpenTelemetry>()
            .setProvider(openTelemetry);

        SampleClient client = new SampleClientBuilder().instrumentationOptions(instrumentationOptions).build();
        client.clientCall();

        // END: io.clientcore.core.telemetry.useexplicitopentelemetry
    }

    /**
     * This code snippet shows how to disable distributed tracing
     * for a specific instance of client.
     */
    public void disableDistributedTracing() {
        // BEGIN: io.clientcore.core.telemetry.disabledistributedtracing

        InstrumentationOptions<?> instrumentationOptions = new InstrumentationOptions<>()
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
            .setContext(io.clientcore.core.util.Context.of(TRACE_CONTEXT_KEY, Context.current().with(span)));

        // run on another thread
        client.clientCall(options);

        // END: io.clientcore.core.telemetry.correlationwithexplicitcontext
    }

    static class SampleClientBuilder {
        private InstrumentationOptions<?> instrumentationOptions;
        // TODO (limolkova): do we need InstrumnetationTrait?
        public SampleClientBuilder instrumentationOptions(InstrumentationOptions<?> instrumentationOptions) {
            this.instrumentationOptions = instrumentationOptions;
            return this;
        }

        public SampleClient build() {
            return new SampleClient(instrumentationOptions, new HttpPipelineBuilder()
                .policies(new InstrumentationPolicy(instrumentationOptions, null))
                .build());
        }
    }

    static class SampleClient {
        private final static LibraryTelemetryOptions LIBRARY_OPTIONS = new LibraryTelemetryOptions("sample");
        private final HttpPipeline httpPipeline;
        private final io.clientcore.core.instrumentation.tracing.Tracer tracer;

        SampleClient(InstrumentationOptions<?> instrumentationOptions, HttpPipeline httpPipeline) {
            this.httpPipeline = httpPipeline;
            this.tracer = InstrumentationProvider.create(instrumentationOptions, LIBRARY_OPTIONS).getTracer();
        }

        public void clientCall() {
            this.clientCall(null);
        }

        @SuppressWarnings("try")
        public void clientCall(RequestOptions options) {
            io.clientcore.core.instrumentation.tracing.Span span = tracer.spanBuilder("clientCall", SpanKind.CLIENT, options)
                .startSpan();

            if (options == null) {
                options = new RequestOptions();
            }

            options.setContext(options.getContext().put(TRACE_CONTEXT_KEY, span));

            try (InstrumentationScope scope = span.makeCurrent()) {
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
