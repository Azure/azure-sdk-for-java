// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.metrics.opentelemetry;

import com.azure.core.util.MetricsOptions;
import com.azure.core.util.metrics.Meter;
import com.azure.core.util.metrics.MeterProvider;

import java.util.Objects;

/**
 * Resolves and provides {@link Meter} implementation.
 * <p>
 * This class is intended to be used by Azure client libraries and provides abstraction over different metrics implementations.
 * Application developers should use metrics implementations such as OpenTelemetry or Micrometer directly.
 */
public final class OpenTelemetryMeterProvider implements MeterProvider {
    /**
     * Creates an instance of {@link OpenTelemetryMeterProvider}.
     */
    public OpenTelemetryMeterProvider() {
    }

    /**
     * Creates named and versioned OpenTelemetry-based implementation of {@link Meter}
     *
     * Use global OpenTelemetry SDK configuration:
     * <!-- src_embed com.azure.core.util.metrics.OpenTelemetryMeterProvider.createMeter#default -->
     * <pre>
     *
     * &#47;&#47; configure OpenTelemetry SDK using io.opentelemetry:opentelemetry-sdk-extension-autoconfigure
     * &#47;&#47; AutoConfiguredOpenTelemetrySdk.initialize&#40;&#41;;
     *
     * &#47;&#47; configure Azure Client, no metric configuration needed
     * AzureClient sampleClient = new AzureClientBuilder&#40;&#41;
     *     .endpoint&#40;&quot;https:&#47;&#47;my-client.azure.com&quot;&#41;
     *     .build&#40;&#41;;
     *
     * Span span = GlobalOpenTelemetry.getTracer&#40;&quot;azure-core-samples&quot;&#41;
     *     .spanBuilder&#40;&quot;doWork&quot;&#41;
     *     .startSpan&#40;&#41;;
     *
     * try &#40;Scope scope = span.makeCurrent&#40;&#41;&#41; &#123;
     *     &#47;&#47; do some work
     *
     *     &#47;&#47; Current context flows to OpenTelemetry metrics and is used to populate exemplars
     *     &#47;&#47; you can also pass OpenTelemetry context explicitly by passing it under PARENT_TRACE_CONTEXT_KEY
     *     String response = sampleClient.methodCall&#40;&quot;get items&quot;&#41;;
     *     &#47;&#47; do more work
     * &#125;
     *
     * span.end&#40;&#41;;
     *
     * </pre>
     * <!-- end com.azure.core.util.metrics.OpenTelemetryMeterProvider.createMeter#default -->
     *
     * It's also possible to pass custom OpenTelemetry SDK configuration
     * <!-- src_embed com.azure.core.util.metrics.OpenTelemetryMeterProvider.createMeter#custom -->
     * <pre>
     *
     * &#47;&#47; configure OpenTelemetry SDK
     * SdkTracerProvider tracerProvider = SdkTracerProvider.builder&#40;&#41;
     *     .addSpanProcessor&#40;BatchSpanProcessor.builder&#40;OtlpGrpcSpanExporter.builder&#40;&#41;.build&#40;&#41;&#41;.build&#40;&#41;&#41;
     *     .build&#40;&#41;;
     *
     * SdkMeterProvider meterProvider = SdkMeterProvider.builder&#40;&#41;
     *     .registerMetricReader&#40;PeriodicMetricReader.builder&#40;OtlpGrpcMetricExporter.builder&#40;&#41;.build&#40;&#41;&#41;.build&#40;&#41;&#41;
     *     .build&#40;&#41;;
     *
     * OpenTelemetry openTelemetry = OpenTelemetrySdk.builder&#40;&#41;
     *     .setTracerProvider&#40;tracerProvider&#41;
     *     .setMeterProvider&#40;meterProvider&#41;
     *     .setPropagators&#40;ContextPropagators.create&#40;W3CTraceContextPropagator.getInstance&#40;&#41;&#41;&#41;
     *     .build&#40;&#41;;
     *
     * Tracer tracer = openTelemetry.getTracer&#40;&quot;azure-core-samples&quot;&#41;;
     *
     * &#47;&#47; pass custom OpenTelemetry instance to MetricsOptions
     * MetricsOptions metricsOptions = new OpenTelemetryMetricsOptions&#40;&#41;
     *     .setOpenTelemetry&#40;openTelemetry&#41;;
     *
     * &#47;&#47; configure Azure Client to use customized MetricOptions
     * AzureClient sampleClient = new AzureClientBuilder&#40;&#41;
     *     .endpoint&#40;&quot;Https:&#47;&#47;my-client.azure.com&quot;&#41;
     *     .clientOptions&#40;new ClientOptions&#40;&#41;.setMetricsOptions&#40;metricsOptions&#41;&#41;
     *     .build&#40;&#41;;
     *
     * Span span = tracer.spanBuilder&#40;&quot;doWork&quot;&#41;.startSpan&#40;&#41;;
     * io.opentelemetry.context.Context otelContext = io.opentelemetry.context.Context.current&#40;&#41;.with&#40;span&#41;;
     *
     * &#47;&#47; do some work
     *
     * &#47;&#47; Context is used by OpenTelemetry metrics to populate exemplars, Context.current&#40;&#41; will be used if no
     * &#47;&#47; explicit context is provided.
     * String response = sampleClient.methodCall&#40;&quot;get items&quot;,
     *     new Context&#40;PARENT_TRACE_CONTEXT_KEY, otelContext&#41;&#41;;
     *
     * &#47;&#47; do more work
     * span.end&#40;&#41;;
     *
     * </pre>
     * <!-- end com.azure.core.util.metrics.OpenTelemetryMeterProvider.createMeter#custom -->
     *
     * @param libraryName Azure client library package name
     * @param libraryVersion Azure client library version
     * @param options instance of {@link MetricsOptions}
     * @return a meter instance.
     */
    @Override
    public Meter createMeter(String libraryName, String libraryVersion, MetricsOptions options) {
        Objects.requireNonNull(libraryName, "'libraryName' cannot be null.");
        return new OpenTelemetryMeter(libraryName, libraryVersion, options);
    }
}
