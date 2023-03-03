// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;

import static java.util.concurrent.TimeUnit.MINUTES;

public class AzureMonitorMetricExporterSample {

    private static final String APPINSIGHTS_CONNECTION_STRING = "<YOUR_CONNECTION_STRING>";

    public static void main(String[] args) {
        sendDoubleHistogram();
    }

    private static void sendDoubleHistogram() {
        try {
            MetricExporter exporter = new AzureMonitorExporterBuilder()
                    .connectionString(APPINSIGHTS_CONNECTION_STRING)
                    .buildMetricExporter();
            PeriodicMetricReader periodicMetricReader = PeriodicMetricReader
                    .builder(exporter)
                    .build();
            SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                    .registerMetricReader(periodicMetricReader)
                    .build();
            OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                    .setMeterProvider(sdkMeterProvider)
                    .buildAndRegisterGlobal();
            Meter meter = openTelemetry.meterBuilder("OTEL.AzureMonitor.Demo")
                    .build();
            DoubleHistogram histogram = meter.histogramBuilder("histogram").build();
            histogram.record(1.0);
            histogram.record(100.0);
            histogram.record(30.0);

            // metrics are exported every 60 seconds by default
            MINUTES.sleep(5);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void sendLongCounter() {
        try {
            MetricExporter exporter = new AzureMonitorExporterBuilder()
                    .connectionString(APPINSIGHTS_CONNECTION_STRING)
                    .buildMetricExporter();
            PeriodicMetricReader periodicMetricReader = PeriodicMetricReader
                    .builder(exporter)
                    .build();
            SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                    .registerMetricReader(periodicMetricReader)
                    .build();
            OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                    .setMeterProvider(sdkMeterProvider)
                    .buildAndRegisterGlobal();
            Meter meter = openTelemetry.meterBuilder("OTEL.AzureMonitor.Demo")
                    .build();

            LongCounter myFruitCounter = meter
                    .counterBuilder("MyFruitCounter")
                    .build();

            myFruitCounter.add(1, Attributes.of(AttributeKey.stringKey("name"), "apple", AttributeKey.stringKey("color"), "red"));
            myFruitCounter.add(2, Attributes.of(AttributeKey.stringKey("name"), "lemon", AttributeKey.stringKey("color"), "yellow"));
            myFruitCounter.add(1, Attributes.of(AttributeKey.stringKey("name"), "lemon", AttributeKey.stringKey("color"), "yellow"));
            myFruitCounter.add(2, Attributes.of(AttributeKey.stringKey("name"), "apple", AttributeKey.stringKey("color"), "green"));
            myFruitCounter.add(5, Attributes.of(AttributeKey.stringKey("name"), "apple", AttributeKey.stringKey("color"), "red"));
            myFruitCounter.add(4, Attributes.of(AttributeKey.stringKey("name"), "lemon", AttributeKey.stringKey("color"), "yellow"));

            // metrics are exported every 60 seconds by default
            MINUTES.sleep(5);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void sendGaugeMetric() {
        try {
            MetricExporter exporter = new AzureMonitorExporterBuilder()
                    .connectionString(APPINSIGHTS_CONNECTION_STRING)
                    .buildMetricExporter();
            PeriodicMetricReader periodicMetricReader = PeriodicMetricReader
                    .builder(exporter)
                    .build();
            SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
                    .registerMetricReader(periodicMetricReader)
                    .build();
            OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                    .setMeterProvider(sdkMeterProvider)
                    .buildAndRegisterGlobal();
            Meter meter = openTelemetry.getMeter("OTEL.AzureMonitor.Demo");

            meter.gaugeBuilder("gauge")
                    .buildWithCallback(
                            observableMeasurement -> {
                                double randomNumber = Math.floor(Math.random() * 100);
                                observableMeasurement.record(randomNumber, Attributes.of(AttributeKey.stringKey("testKey"), "testValue"));
                            });

            // metrics are exported every 60 seconds by default
            MINUTES.sleep(5);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
