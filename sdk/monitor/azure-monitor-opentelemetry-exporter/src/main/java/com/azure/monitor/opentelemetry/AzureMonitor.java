// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry;

import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorExporterProviderKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorLogRecordExporterProvider;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorMetricExporterProvider;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorSpanExporterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.View;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to enable Azure Monitor for OpenTelemetry autoconfiguration.
 */
public final class AzureMonitor {

    private AzureMonitor() {
    }

    /**
     * Customizes an {@link AutoConfigurationCustomizer} for Azure Monitor. The connection string to the Application Insights resource is expected to be configured with the APPLICATIONINSIGHTS_CONNECTION_STRING environment variable.
     *
     * @param autoConfigurationCustomizer The OpenTelemetry autoconfiguration to set up.
     */
    public static void customize(AutoConfigurationCustomizer autoConfigurationCustomizer) {
        AzureMonitorExporterBuilder azureMonitorExporterBuilder = new AzureMonitorExporterBuilder();
        customize(autoConfigurationCustomizer, azureMonitorExporterBuilder);
    }

    /**
     * Customizes an {@link AutoConfigurationCustomizer} for Azure Monitor.
     * @param autoConfigurationCustomizer The OpenTelemetry autoconfiguration to set up.
     * @param connectionString The connection string to connect to an Application Insights resource.
     */
    public static void customize(AutoConfigurationCustomizer autoConfigurationCustomizer, String connectionString) {
        AzureMonitorExporterBuilder azureMonitorExporterBuilder
            = new AzureMonitorExporterBuilder().connectionString(connectionString);
        customize(autoConfigurationCustomizer, azureMonitorExporterBuilder);
    }

    /**
     * Customizes an {@link AutoConfigurationCustomizer} for Azure Monitor.
     * @param autoConfigurationCustomizer the {@link AutoConfigurationCustomizer} object.
     * @param azureMonitorExporterBuilder Advanced configuration to send the data to Azure Monitor.
     */
    public static void customize(AutoConfigurationCustomizer autoConfigurationCustomizer,
        AzureMonitorExporterBuilder azureMonitorExporterBuilder) {
        autoConfigurationCustomizer.addPropertiesSupplier(() -> {
            Map<String, String> props = new HashMap<>();
            props.put("otel.traces.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put("otel.metrics.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put("otel.logs.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put(AzureMonitorExporterProviderKeys.INTERNAL_USING_AZURE_MONITOR_EXPORTER_BUILDER, "true");
            return props;
        });
        autoConfigurationCustomizer.addSpanExporterCustomizer((spanExporter, configProperties) -> {
            if (spanExporter instanceof AzureMonitorSpanExporterProvider.MarkerSpanExporter) {
                spanExporter = azureMonitorExporterBuilder.buildSpanExporter(configProperties);
            }
            return spanExporter;
        });
        autoConfigurationCustomizer.addMetricExporterCustomizer((metricExporter, configProperties) -> {
            if (metricExporter instanceof AzureMonitorMetricExporterProvider.MarkerMetricExporter) {
                metricExporter = azureMonitorExporterBuilder.buildMetricExporter(configProperties);
            }
            return metricExporter;
        });
        autoConfigurationCustomizer.addLogRecordExporterCustomizer((logRecordExporter, configProperties) -> {
            if (logRecordExporter instanceof AzureMonitorLogRecordExporterProvider.MarkerLogRecordExporter) {
                logRecordExporter = azureMonitorExporterBuilder.buildLogRecordExporter(configProperties);
            }
            return logRecordExporter;
        });
        // TODO (trask)
        //        sdkBuilder.addTracerProviderCustomizer((sdkTracerProviderBuilder, configProperties) -> {
        //            QuickPulse quickPulse = QuickPulse.create(getHttpPipeline());
        //            return sdkTracerProviderBuilder.addSpanProcessor(
        //                ne
        autoConfigurationCustomizer
            .addMeterProviderCustomizer((sdkMeterProviderBuilder, config) -> sdkMeterProviderBuilder
                .registerView(InstrumentSelector.builder().setMeterName("io.opentelemetry.sdk.trace").build(),
                    View.builder().setAggregation(Aggregation.drop()).build())
                .registerView(InstrumentSelector.builder().setMeterName("io.opentelemetry.sdk.logs").build(),
                    View.builder().setAggregation(Aggregation.drop()).build()));
    }
}
