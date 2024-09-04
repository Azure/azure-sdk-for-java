// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.monitor.opentelemetry.AzureMonitor;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.trace.export.SpanExporter;

/**
 * Sample to demonstrate using {@link AzureMonitorTraceExporter} to export telemetry events when setting a configuration
 * in App Configuration through the {@link ConfigurationClient}.
 */
public class AppConfigurationAzureMonitorExporterSample {

    private static final Tracer TRACER = configureAzureMonitorExporter();
    private static final String CONNECTION_STRING = "<YOUR_CONNECTION_STRING>";

    /**
     * The main method to run the application.
     *
     * @param args Ignored args.
     */
    public static void main(String[] args) {
        doClientWork();
    }

    /**
     * Configure the OpenTelemetry {@link SpanExporter} to enable tracing.
     *
     * @return The OpenTelemetry {@link Tracer} instance.
     */
    private static Tracer configureAzureMonitorExporter() {
        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

        OpenTelemetry openTelemetry = new AzureMonitor("{connection-string}")
            .configure(sdkBuilder).build().getOpenTelemetrySdk();

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
