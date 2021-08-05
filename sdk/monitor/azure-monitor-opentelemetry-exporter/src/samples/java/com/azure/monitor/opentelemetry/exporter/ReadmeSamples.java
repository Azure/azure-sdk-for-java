// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;


import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

import java.util.Collection;
import java.util.Collections;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {

    /**
     * Sample for creating Azure Monitor Exporter.
     */
    public void createExporter() {
        AzureMonitorTraceExporter azureMonitorTraceExporter = new AzureMonitorExporterBuilder()
            .connectionString("{connection-string}")
            .buildTraceExporter();
    }

    /**
     * Sample for setting up exporter to export traces to Azure Monitor
     */
    public void setupExporter() {

        // Create Azure Monitor exporter and configure OpenTelemetry tracer to use this exporter
        // This should be done just once when application starts up
        AzureMonitorTraceExporter exporter = new AzureMonitorExporterBuilder()
            .connectionString("{connection-string}")
            .buildTraceExporter();

        SdkTracerProvider tracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(SimpleSpanProcessor.create(exporter))
            .build();

        OpenTelemetrySdk openTelemetrySdk = OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal();

        Tracer tracer = openTelemetrySdk.getTracer("Sample");

        // Make service calls by adding new parent spans
        ConfigurationClient client = new ConfigurationClientBuilder()
            .connectionString("{app-config-connection-string}")
            .buildClient();

        Span span = tracer.spanBuilder("user-parent-span").startSpan();
        final Scope scope = span.makeCurrent();
        try {
            // Thread bound (sync) calls will automatically pick up the parent span and you don't need to pass it explicitly.
            client.setConfigurationSetting("hello", "text", "World");
        } finally {
            span.end();
            scope.close();
        }
    }

    /**
     * Method to make the sample compilable but is not visible in README code snippet.
     * @return An empty collection.
     */
    private Collection<SpanData> getSpanDataCollection() {
        return Collections.emptyList();
    }

}
