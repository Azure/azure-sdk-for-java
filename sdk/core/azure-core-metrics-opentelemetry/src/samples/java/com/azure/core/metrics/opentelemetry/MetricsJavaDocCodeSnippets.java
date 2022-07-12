// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.ClientOptions;
import com.azure.core.util.Context;
import com.azure.core.util.MetricsOptions;
import com.azure.core.util.TelemetryAttributes;
import com.azure.core.util.metrics.LongHistogram;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

import java.time.Instant;
import java.util.Collections;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;

/**
 * Contains code snippets showing how to configure Azure Client libraries to use OpenTelemetry Metrics plugin.
 */
public class MetricsJavaDocCodeSnippets {
    /**
     * Code snippet for {@link com.azure.core.metrics.opentelemetry.OpenTelemetryMeterProvider#createMeter(String, String, MetricsOptions)}}
     */
    @SuppressWarnings("try")
    public void sampleDefaultSdkConfigurationWithMetricsAndTraces() {
        // BEGIN: com.azure.core.util.metrics.OpenTelemetryMeterProvider.createMeter#default

        // configure OpenTelemetry SDK using OpenTelemetry SDK Autoconfigure
        AutoConfiguredOpenTelemetrySdk.initialize();

        // configure Azure Client, no metric configuration needed
        AzureClient sampleClient = new AzureClientBuilder()
            .endpoint("https://my-client.azure.com")
            .build();

        Span span = GlobalOpenTelemetry.getTracer("azure-core-samples")
            .spanBuilder("doWork")
            .startSpan();

        try (Scope scope = span.makeCurrent()) {
            // do some work

            // Current context flows to OpenTelemetry metrics and is used to populate exemplars
            // you can also pass OpenTelemetry context explicitly by passing it under PARENT_TRACE_CONTEXT_KEY
            String response = sampleClient.methodCall("get items");
            // do more work
        }

        span.end();

        // END: com.azure.core.util.metrics.OpenTelemetryMeterProvider.createMeter#default
    }

    public void readmeSampleDefaultSdkConfiguration() {
        // BEGIN: readme-sample-defaultConfiguration

        // configure OpenTelemetry SDK using OpenTelemetry SDK Autoconfigure
        AutoConfiguredOpenTelemetrySdk.initialize();

        // configure Azure Client, no metric configuration needed
        // client will use global OTel configured by OpenTelemetry autoconfigure package.
        AzureClient sampleClient = new AzureClientBuilder()
            .endpoint("https://my-client.azure.com")
            .build();

        // use client as usual, if it emits metric, they will be exported
        sampleClient.methodCall("get items");

        // END: readme-sample-defaultConfiguration
    }

    public void readmeSampleCustomSdkConfiguration() {
        // BEGIN: readme-sample-customConfiguration

        // configure OpenTelemetry SDK explicitly per https://opentelemetry.io/docs/instrumentation/java/manual/
        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder().build()).build())
            .build();

        // Pass OTel meterProvider to MetricsOptions.
        MetricsOptions customMetricsOptions = new OpenTelemetryMetricsOptions()
            .setProvider(meterProvider);

        // configure Azure Client to use customMetricsOptions - it will use meterProvider
        // to create meters and instruments
        AzureClient sampleClient = new AzureClientBuilder()
            .endpoint("https://my-client.azure.com")
            .clientOptions(new ClientOptions().setMetricsOptions(customMetricsOptions))
            .build();

        // use client as usual, if it emits metric, they will be exported
        sampleClient.methodCall("get items");

        // END: readme-sample-customConfiguration
    }

    /**
     * Code snippet for {@link com.azure.core.metrics.opentelemetry.OpenTelemetryMeterProvider#createMeter(String, String, MetricsOptions)}}
     */
    public void configureClientLibraryToUseCustomMeter() {
        // BEGIN: com.azure.core.util.metrics.OpenTelemetryMeterProvider.createMeter#custom

        // configure OpenTelemetry SDK
        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(OtlpGrpcSpanExporter.builder().build()).build())
            .build();

        SdkMeterProvider meterProvider = SdkMeterProvider.builder()
            .registerMetricReader(PeriodicMetricReader.builder(OtlpGrpcMetricExporter.builder().build()).build())
            .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .setMeterProvider(meterProvider)
            .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
            .build();

        Tracer tracer = openTelemetry.getTracer("azure-core-samples");

        // pass custom OpenTelemetry SdkMeterProvider to MetricsOptions
        MetricsOptions metricsOptions = new OpenTelemetryMetricsOptions()
            .setProvider(openTelemetry.getMeterProvider());

        // configure Azure Client to use customized MetricOptions
        AzureClient sampleClient = new AzureClientBuilder()
            .endpoint("Https://my-client.azure.com")
            .clientOptions(new ClientOptions().setMetricsOptions(metricsOptions))
            .build();

        Span span = tracer.spanBuilder("doWork").startSpan();
        io.opentelemetry.context.Context otelContext = io.opentelemetry.context.Context.current().with(span);

        // do some work

        // Context is used by OpenTelemetry metrics to populate exemplars, Context.current() will be used if no
        // explicit context is provided.
        String response = sampleClient.methodCall("get items",
            new Context(PARENT_TRACE_CONTEXT_KEY, otelContext));

        // do more work
        span.end();

        // END: com.azure.core.util.metrics.OpenTelemetryMeterProvider.createMeter#custom
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
        private static final MeterProvider DEFAULT_PROVIDER = MeterProvider.getDefaultProvider();
        private final Meter meter;
        private final LongHistogram callDuration;
        private final TelemetryAttributes attributes;
        AzureClient(String endpoint, ClientOptions options) {
            meter = DEFAULT_PROVIDER.createMeter("azure-core-samples", "1.0.0", options == null ? null : options.getMetricsOptions());
            callDuration = meter.createLongHistogram("az.sample.method.duration", "Duration of sample method call", "ms");
            attributes = meter.createAttributes(Collections.singletonMap("endpoint", endpoint));
        }

        public String methodCall(String request) {
            return methodCall(request, Context.NONE);
        }

        public String methodCall(String request, com.azure.core.util.Context context) {
            Instant start = Instant.now();

            // call service and get response
            if (callDuration.isEnabled()) {
                callDuration.record(Instant.now().toEpochMilli() - start.toEpochMilli(), attributes, context);
            }
            return "done";
        }
    }
}
