// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.extension.annotations.WithSpan;
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
    public static void main(String[] args) {
        configureLoggingExporter();

        ConfigurationClient client = new ConfigurationClientBuilder()
            .connectionString(CONNECTION_STRING)
            .buildClient();

        doClientWork(client);
    }

    /**
     * Configure the OpenTelemetry {@link LoggingSpanExporter} to enable tracing.
     *
     * @return The OpenTelemetry {@link Tracer} instance.
     */
    private static void configureLoggingExporter() {
        // WithSpan annotation creates a parent span and makes it current, which propagates into synchronous calls
        // automatically.
        SdkTracerProvider tracerProvider =
            SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(LoggingSpanExporter.create()).build())
                .build();

        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal();
    }

    /**
     * Creates the {@link ConfigurationClient} and creates a configuration in Azure App Configuration with distributed
     * tracing enabled and using the Logging exporter to export telemetry events.
     */
    @WithSpan
    private static void doClientWork(ConfigurationClient client) {
        // WithSpan annotation creates a parent span and makes it current. Current context propagates into synchronous
        // calls automatically.
        client.setConfigurationSetting("hello", "text", "World");
    }
}
