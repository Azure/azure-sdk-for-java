// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;


import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.trace.data.SpanData;

import java.util.Collection;
import java.util.Collections;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 * <p>
 * Code samples for the README.md
 */
public class ReadmeSamples {

    /**
     * Sample for creating Azure Monitor exporter builder.
     */
    public void createExporterBuilder() {
        // BEGIN: readme-sample-createExporterBuilder
        AzureMonitorExporterBuilder azureMonitorExporterBuilder = new AzureMonitorExporterBuilder()
            .connectionString("{connection-string}");
        // END: readme-sample-createExporterBuilder
    }

    /**
     * Sample for setting up exporter to export traces to Azure Monitor
     */
    public void setupExporter() {
        // BEGIN: readme-sample-setupExporter
        // Create Azure Monitor exporter and initialize OpenTelemetry SDK
        // This should be done just once when application starts up
        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

        new AzureMonitorExporterBuilder()
            .connectionString("{connection-string}")
            .build(sdkBuilder);

        OpenTelemetry openTelemetry = sdkBuilder.build().getOpenTelemetrySdk();

        Tracer tracer = openTelemetry.getTracer("Sample");
        // END: readme-sample-setupExporter

        // BEGIN: readme-sample-createSpans
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
        // END: readme-sample-createSpans
    }

    /**
     * Method to make the sample compilable but is not visible in README code snippet.
     *
     * @return An empty collection.
     */
    private Collection<SpanData> getSpanDataCollection() {
        return Collections.emptyList();
    }

}
