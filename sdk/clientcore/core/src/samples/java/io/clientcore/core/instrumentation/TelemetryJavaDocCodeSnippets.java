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
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.TracingScope;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Application developers that don't have OpenTelemetry on the classpath
 * can take advantage of basic distributed tracing providing log correlation.
 * <p>
 * Instrumented client libraries start a span for each client call and
 * propagate the span context to the HTTP pipeline that creates
 * a new span for each HTTP request.
 * <p>
 * All logs emitted by the client library are automatically correlated
 * with the encompassing span contexts.
 * <p>
 * Span context is propagated to the endpoint using W3C Trace Context format.
 * <p>
 * You can also receive logs describing generated spans by enabling {library-name}.tracing
 * logger at INFO level.
 * <p>
 * You can pass custom correlation IDs (following W3C Trace Context format) in {@link RequestOptions}.
 */
public class TelemetryJavaDocCodeSnippets {

    /**
     * To get basic distributed tracing, just use client libraries as usual.
     */
    public void fallbackTracing() {
        // BEGIN: io.clientcore.core.telemetry.fallback.tracing

        SampleClient client = new SampleClientBuilder().build();
        client.clientCall();

        // END: io.clientcore.core.telemetry.fallback.tracing
    }

    /**
     * You can pass custom logger to the client library to receive spans from
     * this library in the logs.
     */
    public void useCustomLogger() {
        // BEGIN: io.clientcore.core.telemetry.usecustomlogger

        ClientLogger logger = new ClientLogger("sample-client-traces");

        HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions()
            .setTelemetryProvider(logger);

        SampleClient client = new SampleClientBuilder().instrumentationOptions(instrumentationOptions).build();
        client.clientCall();

        // END: io.clientcore.core.telemetry.usecustomlogger
    }

    /**
     * This code snippet shows how to disable distributed tracing
     * for a specific instance of client.
     */
    public void disableDistributedTracing() {
        // BEGIN: io.clientcore.core.telemetry.fallback.disabledistributedtracing

        HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions()
            .setTracingEnabled(false);

        SampleClient client = new SampleClientBuilder().instrumentationOptions(instrumentationOptions).build();
        client.clientCall();

        // END: io.clientcore.core.telemetry.fallback.disabledistributedtracing
    }

    /**
     * This code snippet shows how to assign custom traceId and spanId to the client call.
     */
    public void correlationWithExplicitContext() {
        // BEGIN: io.clientcore.core.telemetry.fallback.correlationwithexplicitcontext

        SampleClient client = new SampleClientBuilder().build();

        RequestOptions options = new RequestOptions()
            .setInstrumentationContext(new MyInstrumentationContext("e4eaaaf2d48f4bf3b299a8a2a2a77ad7", "5e0c63257de34c56"));

        // run on another thread
        client.clientCall(options);

        // END: io.clientcore.core.telemetry.fallback.correlationwithexplicitcontext
    }

    static class MyInstrumentationContext implements InstrumentationContext {
        private final String traceId;
        private final String spanId;

        MyInstrumentationContext(String traceId, String spanId) {
            this.traceId = traceId;
            this.spanId = spanId;
        }

        @Override
        public String getTraceId() {
            return traceId;
        }

        @Override
        public String getSpanId() {
            return spanId;
        }

        @Override
        public String getTraceFlags() {
            return "00";
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        public Span getSpan() {
            return Span.noop();
        }
    }

    static class SampleClientBuilder {
        private HttpInstrumentationOptions instrumentationOptions;
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
            Span span = tracer.spanBuilder("clientCall", SpanKind.CLIENT, options.getInstrumentationContext())
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
