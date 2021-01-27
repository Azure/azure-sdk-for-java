// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.opentelemetry.exporter.azuremonitor;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;

/**
 * Sample to demonstrate using {@link AzureMonitorExporter} to export telemetry events when setting a configuration
 * in App Configuration through the {@link ConfigurationClient}.
 */
public class AppConfigurationAzureMonitorExporterSample {

    private static final Tracer TRACER = configureAzureMonitorExporter();
    private static final String CONNECTION_STRING = "<YOUR_CONNECTION_STRING>";

    /**
     * The main method to run the application.
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        doClientWork();
    }

    /**
     * Configure the OpenTelemetry {@link AzureMonitorExporter} to enable tracing.
     * @return The OpenTelemetry {@link Tracer} instance.
     */
    private static Tracer configureAzureMonitorExporter() {
        AzureMonitorExporter exporter = new AzureMonitorExporterBuilder()
            .connectionString("{connection-string}")
            .buildExporter();

        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder().build();
        openTelemetry
            .getTracerManagement()
            .addSpanProcessor(SimpleSpanProcessor.builder(exporter).build());

        return openTelemetry.getTracer("Sample");
    }

    /**
     * Creates the {@link ConfigurationClient} and sets a configuration in Azure App Configuration with distributed
     * tracing enabled and using the Azure Monitor exporter to export telemetry events to Azure Monitor.
     */
    private static void doClientWork() {
        ConfigurationClient client = new ConfigurationClientBuilder()
            .connectionString(CONNECTION_STRING)
            .buildClient();

        Span span = TRACER.spanBuilder("user-parent-span").startSpan();
        final Scope scope = span.makeCurrent();
        try {
            // Thread bound (sync) calls will automatically pick up the parent span and you don't need to pass it explicitly.
            client.setConfigurationSetting("hello", "text", "World");
        } finally {
            span.end();
            scope.close();
        }
    }
}
