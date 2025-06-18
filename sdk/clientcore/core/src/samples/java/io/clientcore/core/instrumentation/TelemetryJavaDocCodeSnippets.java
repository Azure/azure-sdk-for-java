// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.tracing.Span;

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
 * You can pass custom correlation IDs (following W3C Trace Context format) in {@link RequestContext}.
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

        RequestContext context = RequestContext.builder()
            .setInstrumentationContext(new MyInstrumentationContext("e4eaaaf2d48f4bf3b299a8a2a2a77ad7", "5e0c63257de34c56"))
            .build();

        // run on another thread
        client.downloadContent(context);

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
}
