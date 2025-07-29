// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.instrumentation;

import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.pipeline.HttpInstrumentationOptions;
import io.clientcore.core.http.models.HttpMethod;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpInstrumentationPolicy;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.http.pipeline.HttpPipelineBuilder;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;

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
     * This code snippet shows how to disable distributed tracing
     * for a specific instance of client.
     */
    public void disableMetrics() {
        // BEGIN: io.clientcore.core.telemetry.disablemetrics

        HttpInstrumentationOptions instrumentationOptions = new HttpInstrumentationOptions()
            .setMetricsEnabled(false);

        SampleClient client = new SampleClientBuilder().instrumentationOptions(instrumentationOptions).build();
        client.clientCall();

        // END: io.clientcore.core.telemetry.disablemetrics
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
        // However, in asynchronous code, context may need to be propagated explicitly using RequestContext
        // and explicit io.clientcore.core.util.Context.

        RequestContext context = RequestContext.builder()
            .setInstrumentationContext(Instrumentation.createInstrumentationContext(span))
            .build();

        // run on another thread - all telemetry will be correlated with the span created above
        client.clientCall(context);

        // END: io.clientcore.core.telemetry.correlationwithexplicitcontext
    }

    static class SampleClientBuilder {
        private HttpInstrumentationOptions instrumentationOptions;
        public SampleClientBuilder instrumentationOptions(HttpInstrumentationOptions instrumentationOptions) {
            this.instrumentationOptions = instrumentationOptions;
            return this;
        }

        public SampleClient build() {
            return new SampleClient(instrumentationOptions, new HttpPipelineBuilder()
                .addPolicy(new HttpInstrumentationPolicy(instrumentationOptions))
                .build());
        }
    }

    static class SampleClient {
        private static final String SDK_NAME = "contoso.sample";
        private final Instrumentation instrumentation;
        private final HttpPipeline httpPipeline;
        private final String serviceEndpoint;

        SampleClient(InstrumentationOptions instrumentationOptions, HttpPipeline httpPipeline) {
            serviceEndpoint = "https://contoso.com";
            SdkInstrumentationOptions sdkOptions = new SdkInstrumentationOptions(SDK_NAME)
                .setEndpoint(serviceEndpoint);
            this.httpPipeline = httpPipeline;
            this.instrumentation = Instrumentation.create(instrumentationOptions, sdkOptions);
        }

        public Response<?> clientCall() {
            return this.clientCallWithResponse(null);
        }

        public Response<?> clientCallWithResponse(RequestContext context) {
            return instrumentation.instrumentWithResponse("Sample.call", context, this::clientCallWithResponseImpl);
        }

        public void clientCall(RequestContext context) {
            instrumentation.instrument("Sample.call", context, this::clientCallImpl);
        }

        private Response<?> clientCallWithResponseImpl(RequestContext context) {
            return httpPipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(serviceEndpoint).setContext(context));
        }

        private void clientCallImpl(RequestContext context) {
            httpPipeline.send(new HttpRequest().setMethod(HttpMethod.GET).setUri(serviceEndpoint).setContext(context));
        }
    }
}
