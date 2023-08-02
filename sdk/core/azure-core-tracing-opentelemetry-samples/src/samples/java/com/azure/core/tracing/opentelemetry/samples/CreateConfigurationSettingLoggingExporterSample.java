// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry.samples;

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

        ConfigurationClient client = new ConfigurationClientBuilder()
            .connectionString(CONNECTION_STRING)
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
        SdkTracerProvider tracerProvider =
            SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(LoggingSpanExporter.create()).build())
                .build();

        return OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal();
    }
}
