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
import io.clientcore.core.instrumentation.metrics.DoubleHistogram;
import io.clientcore.core.instrumentation.metrics.Meter;
import io.clientcore.core.instrumentation.tracing.Span;
import io.clientcore.core.instrumentation.tracing.SpanKind;
import io.clientcore.core.instrumentation.tracing.Tracer;
import io.clientcore.core.instrumentation.tracing.TracingScope;

import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
        client.downloadContent();

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
        client.downloadContent();

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
        client.downloadContent();

        // END: io.clientcore.core.telemetry.fallback.disabledistributedtracing
    }

    /**
     * This code snippet shows how to disable metrics
     * for a specific instance of client.
     */
    public void disableMetrics() {
        // BEGIN: io.clientcore.core.telemetry.fallback.disablemetrics

        HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions()
            .setMetricsEnabled(false);

        SampleClient client = new SampleClientBuilder().instrumentationOptions(instrumentationOptions).build();
        client.downloadContent();

        // END: io.clientcore.core.telemetry.fallback.disablemetrics
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
        client.downloadContent(options);

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
            HttpPipeline pipeline = new HttpPipelineBuilder()
                .policies(new HttpInstrumentationPolicy(instrumentationOptions))
                .build();
            return new SampleClient(instrumentationOptions, pipeline, URI.create("https://example.com"));
        }
    }

    static class SampleClient {
        private final static LibraryInstrumentationOptions LIBRARY_OPTIONS = new LibraryInstrumentationOptions("sample");
        private final HttpPipeline httpPipeline;
        private final Instrumentation instrumentation;
        private final InstrumentationScopeFactory downloadContentInstrumentation;
        private final URI endpoint;

        SampleClient(InstrumentationOptions instrumentationOptions, HttpPipeline httpPipeline, URI endpoint) {
            this.httpPipeline = httpPipeline;
            this.endpoint = endpoint;
            this.instrumentation = Instrumentation.create(instrumentationOptions, LIBRARY_OPTIONS);
            this.downloadContentInstrumentation = new InstrumentationScopeFactory("downloadContent", endpoint, "sample.client.operation.duration",
                "Duration of the sample client calls", instrumentation);
        }

        public Response<?> downloadContent() {
            return this.downloadContent(null);
        }

        @SuppressWarnings("try")
        public Response<?> downloadContent(RequestOptions options) {
            if (!downloadContentInstrumentation.isEnabled()) {
                return httpPipeline.send(new HttpRequest(HttpMethod.GET, endpoint));
            }

            if (options == null || options == RequestOptions.none()) {
                options = new RequestOptions();
            }

            InstrumentationScopeFactory.Scope scope = downloadContentInstrumentation.startScope(options);
            try {
                if (scope.getInstrumentationContext().isValid()) {
                    options.setInstrumentationContext(scope.getInstrumentationContext());
                }

                return httpPipeline.send(new HttpRequest(HttpMethod.GET, endpoint));
            } catch (RuntimeException t) {
                scope.setError(t);
                throw t;
            } finally {
                scope.close();
            }
        }
    }

    static class InstrumentationScopeFactory {
        private static final List<Double> DURATION_BOUNDARIES_ADVICE = Arrays.asList(0.005d, 0.01d, 0.025d, 0.05d, 0.075d, 0.1d, 0.25d, 0.5d, 0.75d, 1d, 2.5d, 5d, 7.5d, 10d);
        private final Tracer tracer;
        private final DoubleHistogram callDuration;
        private final String operationName;

        private final InstrumentationAttributes successAttributes;

        InstrumentationScopeFactory(String operationName, URI serviceEndpoint, String metricName, String metricDescription, Instrumentation instrumentation) {
            Objects.requireNonNull(operationName, "'operationName' cannot be null");
            Objects.requireNonNull(serviceEndpoint, "'serviceEndpoint' cannot be null");
            Objects.requireNonNull(metricName, "'metricName' cannot be null");
            Objects.requireNonNull(instrumentation, "'instrumentation' cannot be null");


            this.tracer = instrumentation.createTracer();
            Meter meter = instrumentation.createMeter();
            this.callDuration = meter.createDoubleHistogram(metricName, metricDescription, "s", DURATION_BOUNDARIES_ADVICE);
            this.successAttributes = createAttributes(operationName, serviceEndpoint, instrumentation);
            this.operationName = operationName;
        }

        public boolean isEnabled() {
            return tracer.isEnabled() || callDuration.isEnabled();
        }

        public Scope startScope(RequestOptions requestOptions) {
            InstrumentationContext parent = requestOptions == null ? null : requestOptions.getInstrumentationContext();
            return new Scope(operationName, successAttributes, parent, tracer, callDuration).makeCurrent();
        }

        class Scope implements AutoCloseable {
            private final Span span;
            private final InstrumentationContext instrumentationContext;
            private final InstrumentationAttributes startAttributes;
            private final long startTimeNs;
            private Throwable error;
            private TracingScope tracingScope;

            Scope(String operationName, InstrumentationAttributes startAttributes, InstrumentationContext parent, Tracer tracer, DoubleHistogram callDuration) {
                this.startAttributes = startAttributes;
                this.span = tracer.spanBuilder(operationName, SpanKind.CLIENT, parent)
                    .setAllAttributes(startAttributes)
                    .startSpan();
                this.instrumentationContext = span.getInstrumentationContext().isValid() ? span.getInstrumentationContext() : parent;
                this.startTimeNs = callDuration.isEnabled() ? System.nanoTime() : 0;
            }

            Scope makeCurrent() {
                this.tracingScope = span.makeCurrent();
                return this;
            }

            Scope setError(Throwable error) {
                this.error = error;
                return this;
            }

            InstrumentationContext getInstrumentationContext() {
                return instrumentationContext;
            }

            @Override
            public void close() {
                if (callDuration.isEnabled()) {
                    InstrumentationAttributes attributes = error == null ? successAttributes : startAttributes.put("error.type", error.getClass().getCanonicalName());
                    callDuration.record((System.nanoTime() - startTimeNs) / 1e9, attributes, instrumentationContext);
                }

                span.end(error);
                if (tracingScope != null) {
                    tracingScope.close();
                    tracingScope = null;
                }
            }
        }

        private static InstrumentationAttributes createAttributes(String operationName, URI endpoint, Instrumentation instrumentation) {
            Map<String, Object> attributeMap = new HashMap<>(4);
            attributeMap.put("operation.name", operationName);
            attributeMap.put("server.address", endpoint.getHost());
            if (endpoint.getPort() != -1) {
                attributeMap.put("server.port", endpoint.getPort());
            }

            return instrumentation.createAttributes(attributeMap);
        }
    }
}
