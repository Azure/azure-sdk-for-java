// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry.samples;

import com.azure.core.tracing.opentelemetry.OpenTelemetryTracingOptions;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.TracingOptions;
import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;

/**
 * Sample to demonstrate using {@link LoggingSpanExporter} to export telemetry events when creating a configuration
 * in App Configuration through the {@link ConfigurationClient}.
 */
public class CreateConfigurationSettingLoggingExporterSample {
    private static final String CONNECTION_STRING = "<YOUR_CONNECTION_STRING>";

    /**
     * The main method to run the application.
     *
     * @param args Ignored args.
     */
    @SuppressWarnings("try")
    public static void main(String[] args) {
        OpenTelemetrySdk openTelemetry = configureTracing();

        // In this sample we configured OpenTelemetry without registering global instance, so we need to pass it explicitly to the Azure SDK.
        // If we used ApplicationInsights or OpenTelemetry agent, or registered global instance, we would not need to pass it explicitly.
        TracingOptions tracingOptions = new OpenTelemetryTracingOptions().setOpenTelemetry(openTelemetry);

        ConfigurationClient client = new ConfigurationClientBuilder()
            .connectionString(CONNECTION_STRING)
            .clientOptions(new ClientOptions().setTracingOptions(tracingOptions))
            .buildClient();

        Tracer tracer = openTelemetry.getTracer("sample");

        Span span = tracer.spanBuilder("my-span").startSpan();
        try (Scope s = span.makeCurrent()) {
            // current span propagates into synchronous calls automatically. ApplicationInsights or OpenTelemetry agent
            // also propagate context through async reactor calls.
            client.setConfigurationSetting("hello", "text", "World");
        } finally {
            span.end();
        }

        openTelemetry.close();
    }

    /**
     * Configure the OpenTelemetry to print traces with {@link LoggingSpanExporter}.
     */
    private static OpenTelemetrySdk configureTracing() {
        // configure OpenTelemetry explicitly or with io.opentelemetry:opentelemetry-sdk-extension-autoconfigure package
        SdkTracerProvider tracerProvider =
            SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(LoggingSpanExporter.create()).build())
                .build();

        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .build();
    }
}
