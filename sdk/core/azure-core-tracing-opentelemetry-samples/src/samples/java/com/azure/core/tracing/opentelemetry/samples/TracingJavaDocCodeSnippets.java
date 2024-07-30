// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry.samples;

import com.azure.core.http.rest.Response;
import com.azure.core.tracing.opentelemetry.OpenTelemetryTracingOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Context;
import com.azure.core.util.TracingOptions;
import com.azure.core.util.tracing.TracerProvider;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;

/**
 * Contains code snippets showing how to configure Azure Client libraries to use OpenTelemetry Tracing plugin.
 */
public class TracingJavaDocCodeSnippets {

    @SuppressWarnings("try")
    public void sampleGlobalSdkConfiguration() {
        // BEGIN: com.azure.core.util.tracing.TracingOptions#default

        // no need to configure OpenTelemetry if you're using the OpenTelemetry Java agent (or another vendor-specific Java agent based on it).
        // if you're using OpenTelemetry SDK, you can configure it with io.opentelemetry:opentelemetry-sdk-extension-autoconfigure package:
        OpenTelemetry opentelemetry = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();

        // configure Azure Client, no metric configuration needed
        AzureClient sampleClient = new AzureClientBuilder()
            .endpoint("https://my-client.azure.com")
            .build();

        Span span = opentelemetry.getTracer("azure-core-samples")
            .spanBuilder("doWork")
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // do some work

            // Current context flows to underlying Azure client explicitly
            // and is used as a parent on span-to-be-created for sampleClient.methodCall.
            String response = sampleClient.methodCall("get items");
            // do more work
        }

        span.end();

        // END: com.azure.core.util.tracing.TracingOptions#default
    }

    public void customProviderSdkConfiguration() {
        // BEGIN: com.azure.core.tracing.TracingOptions#custom

        // configure OpenTelemetry SDK explicitly per https://opentelemetry.io/docs/instrumentation/java/manual/
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(LoggingSpanExporter.create()).build())
            .build();

        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder().setTracerProvider(tracerProvider).build();
        // Pass OpenTelemetry container to TracingOptions.
        TracingOptions customTracingOptions = new OpenTelemetryTracingOptions()
            .setOpenTelemetry(openTelemetry);

        // configure Azure Client to use customTracingOptions - it will use tracerProvider
        // to create tracers
        AzureClient sampleClient = new AzureClientBuilder()
            .endpoint("https://my-client.azure.com")
            .clientOptions(new ClientOptions().setTracingOptions(customTracingOptions))
            .build();

        // use client as usual, if it emits spans, they will be exported
        sampleClient.methodCall("get items");

        // END: com.azure.core.tracing.TracingOptions#custom
        openTelemetry.close();
    }

    public void passContextExplicitly() {
        // BEGIN: com.azure.core.util.tracing#explicit-parent

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(LoggingSpanExporter.create()).build())
            .build();

        AzureClient sampleClient = new AzureClientBuilder()
            .endpoint("Https://my-client.azure.com")
            .build();

        Tracer tracer = tracerProvider.get("test");
        Span parent = tracer.spanBuilder("parent").startSpan();
        io.opentelemetry.context.Context traceContext = io.opentelemetry.context.Context.current().with(parent);

        // do some work

        // You can pass parent explicitly using PARENT_TRACE_CONTEXT_KEY in the com.azure.core.util.Context.
        // Or, when using async clients, pass it in reactor.util.context.Context under the same key.
        String response = sampleClient.methodCall("get items",
            new Context(PARENT_TRACE_CONTEXT_KEY, traceContext));

        // do more work
        parent.end();

        // END: com.azure.core.util.tracing#explicit-parent
        tracerProvider.close();
    }

    /**
     * Sample Azure client builder
     */
    public final class AzureClientBuilder {
        private String endpoint;
        private ClientOptions options;

        public AzureClientBuilder endpoint(String endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public AzureClientBuilder clientOptions(ClientOptions options) {
            this.options = options;
            return this;
        }

        public AzureClient build() {
            return new AzureClient(endpoint, options);
        }
    }

    /**
     * Sample Azure client implementation
     */
    private static final class AzureClient {
        private static final TracerProvider DEFAULT_PROVIDER = TracerProvider.getDefaultProvider();
        private final com.azure.core.util.tracing.Tracer tracer;
        AzureClient(String endpoint, ClientOptions options) {
            tracer = DEFAULT_PROVIDER.createTracer("azure-storage-blob", "12.20.0",
                "Microsoft.Storage", options == null ? null : options.getTracingOptions());
        }

        public String methodCall(String request) {
            return methodCall(request, Context.NONE);
        }

        public String methodCall(String request, com.azure.core.util.Context context) {
            Context span = tracer.start("AzureClient.methodCall", context);

            Throwable throwable = null;
            try {
                callService();
            } catch (Throwable ex) {
                throwable = ex;
            } finally {
                tracer.end(null, throwable, span);
            }

            return "done";
        }

        private Response<Void> callService() {
            return null;
        }
    }
}
