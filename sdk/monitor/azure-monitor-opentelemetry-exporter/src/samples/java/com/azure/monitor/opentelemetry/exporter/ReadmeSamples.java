// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;


import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.monitor.opentelemetry.AzureMonitor;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.SdkLoggerProviderBuilder;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReaderBuilder;
import io.opentelemetry.sdk.trace.*;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import org.apache.commons.lang3.RandomStringUtils;

import java.time.Duration;
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
     * Sample for setting up exporter to export traces to Azure Monitor
     */
    public void setupExporter() {
        // BEGIN: readme-sample-setupExporter
        // Configure OpenTelemetry to export data to Azure Monitor
        // This should be done just once when application starts up
        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

        OpenTelemetry openTelemetry = new AzureMonitor("{connection-string}")
            .configure(sdkBuilder).build().getOpenTelemetrySdk();

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
     * Sample to use the Azure Monitor OpenTelemetry Exporter with the OpenTelemetry SDK auto-configuration
     */
    public void exporterAndOpenTelemetryAutoconfiguration() {
        // BEGIN: readme-sample-autoconfigure
        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

        OpenTelemetry openTelemetry = new AzureMonitor("{connection-string}")
            .configure(sdkBuilder).build().getOpenTelemetrySdk();
        // END: readme-sample-autoconfigure
    }

    /**
     * Sample to add a span processor to the OpenTelemetry SDK auto-configuration
     */
    public void spanProcessor() {
        // BEGIN: readme-sample-span-processor
        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

        SpanProcessor spanProcessor = new SpanProcessor() {
            @Override
            public void onStart(Context context, ReadWriteSpan span) {
                span.setAttribute("random", RandomStringUtils.random(10));
            }

            @Override
            public boolean isStartRequired() {
                return true;
            }

            @Override
            public void onEnd(ReadableSpan readableSpan) {
            }

            @Override
            public boolean isEndRequired() {
                return false;
            }
        };

        sdkBuilder.addTracerProviderCustomizer(
            (sdkTracerProviderBuilder, configProperties) -> sdkTracerProviderBuilder
                .addSpanProcessor(spanProcessor));
        // END: readme-sample-span-processor
    }
}
