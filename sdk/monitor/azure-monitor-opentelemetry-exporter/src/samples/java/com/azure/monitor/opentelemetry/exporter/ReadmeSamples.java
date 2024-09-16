// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;


import com.azure.data.appconfiguration.ConfigurationClient;
import com.azure.data.appconfiguration.ConfigurationClientBuilder;
import com.azure.monitor.opentelemetry.AzureMonitor;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;

import io.opentelemetry.sdk.trace.*;

import org.apache.commons.lang3.RandomStringUtils;

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

        AzureMonitor.configure(sdkBuilder, "{connection-string}");

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
     * Sample to use the Azure Monitor OpenTelemetry Exporter with the OpenTelemetry SDK auto-configuration when the connection string is set with the APPLICATIONINSIGHTS_CONNECTION_STRING
     */
    public void exporterAndOpenTelemetryAutoconfigurationEnvVariable() {
        // BEGIN: readme-sample-autoconfigure-env-variable
        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
        AzureMonitor.configure(sdkBuilder);
        OpenTelemetry openTelemetry = sdkBuilder.build().getOpenTelemetrySdk();
        // END: readme-sample-autoconfigure-env-variable
    }

    /**
     * Sample to use the Azure Monitor OpenTelemetry Exporter with the OpenTelemetry SDK auto-configuration
     */
    public void exporterAndOpenTelemetryAutoconfiguration() {
        // BEGIN: readme-sample-autoconfigure
        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();
        AzureMonitor.configure(sdkBuilder, "{connection-string}");
        OpenTelemetry openTelemetry = sdkBuilder.build().getOpenTelemetrySdk();
        // END: readme-sample-autoconfigure
    }

    /**
     * Sample to create a span.
     */
    @SuppressWarnings("try")
    public void createSpan() {
        // BEGIN: readme-sample-create-span
        AutoConfiguredOpenTelemetrySdkBuilder otelSdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

        AzureMonitor.configure(otelSdkBuilder, "{connection-string}");

        OpenTelemetry openTelemetry = otelSdkBuilder.build().getOpenTelemetrySdk();
        Tracer tracer = openTelemetry.getTracer("Sample");

        Span span = tracer.spanBuilder("spanName").startSpan();

        // Make the span the current span
        try (Scope scope = span.makeCurrent()) {
            // Your application logic here
        } catch (Throwable t) {
            span.recordException(t);
            throw t;
        } finally {
            span.end();
        }
        // END: readme-sample-create-span
    }

    /**
     * Sample to add a span processor to the OpenTelemetry SDK auto-configuration
     */
    public void spanProcessor() {
        // BEGIN: readme-sample-span-processor
        AutoConfiguredOpenTelemetrySdkBuilder sdkBuilder = AutoConfiguredOpenTelemetrySdk.builder();

        AzureMonitor.configure(sdkBuilder);

        SpanProcessor spanProcessor = new SpanProcessor() {
            @Override
            public void onStart(Context context, ReadWriteSpan span) {
                span.setAttribute(AttributeKey.stringKey("random"), RandomStringUtils.random(10));
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
