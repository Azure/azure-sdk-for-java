// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.TracingOptions;
import io.opentelemetry.api.trace.TracerProvider;

/**
 * OpenTelemetry-specific Azure SDK tracing options.
 */
public class OpenTelemetryTracingOptions extends TracingOptions {
    private TracerProvider provider;
    private OpenTelemetrySchemaVersion schemaVersion;

    /**
     * Gets implementation-specific state containing all configuration needed for the implementation such as OpenTelemetry MeterProvider.
     * Check out OpenTelemetry Metrics plugin documentation for the details and examples.
     *
     * @return the value of implementation-specific metric provider, {@code null} by default.
     */
    public TracerProvider getProvider() {
        return provider;
    }

    /**
     * Sets implementation-specific state containing all configuration needed for the implementation such as OpenTelemetry MeterProvider.
     * Check out OpenTelemetry Metrics plugin documentation for the details and examples.
     *
     * <!-- src_embed com.azure.core.tracing.TracingOptions#custom -->
     * <pre>
     *
     * &#47;&#47; configure OpenTelemetry SDK explicitly per https:&#47;&#47;opentelemetry.io&#47;docs&#47;instrumentation&#47;java&#47;manual&#47;
     * SdkTracerProvider tracerProvider = SdkTracerProvider.builder&#40;&#41;
     *     .addSpanProcessor&#40;SimpleSpanProcessor.create&#40;LoggingSpanExporter.create&#40;&#41;&#41;&#41;
     *     .build&#40;&#41;;
     *
     * &#47;&#47; Pass OpenTelemetry tracerProvider to TracingOptions.
     * TracingOptions customTracingOptions = new OpenTelemetryTracingOptions&#40;&#41;
     *     .setProvider&#40;tracerProvider&#41;;
     *
     * &#47;&#47; configure Azure Client to use customTracingOptions - it will use tracerProvider
     * &#47;&#47; to create tracers
     * AzureClient sampleClient = new AzureClientBuilder&#40;&#41;
     *     .endpoint&#40;&quot;https:&#47;&#47;my-client.azure.com&quot;&#41;
     *     .clientOptions&#40;new ClientOptions&#40;&#41;.setTracingOptions&#40;customTracingOptions&#41;&#41;
     *     .build&#40;&#41;;
     *
     * &#47;&#47; use client as usual, if it emits spans, they will be exported
     * sampleClient.methodCall&#40;&quot;get items&quot;&#41;;
     *
     * </pre>
     * <!-- end com.azure.core.tracing.TracingOptions#custom -->
     *
     * @param provider Instance of {@link TracerProvider}
     * @return the updated {@code MetricsOptions} object.
     */
    public OpenTelemetryTracingOptions setProvider(TracerProvider provider) {
        this.provider = provider;
        return this;
    }

    /**
     * Gets schema version.
     * @return schema version.
     */
    public OpenTelemetrySchemaVersion getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * Sets schema version.
     *
     * @param schemaVersion schema version.
     * @return updated OpenTelemetryTracingOptions.
     */
    public OpenTelemetryTracingOptions setSchemaVersion(OpenTelemetrySchemaVersion schemaVersion) {
        this.schemaVersion = schemaVersion;
        return this;
    }

}
