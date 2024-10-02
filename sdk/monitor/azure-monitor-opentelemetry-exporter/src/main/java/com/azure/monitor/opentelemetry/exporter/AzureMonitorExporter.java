// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

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
public final class AzureMonitorExporter {

    private AzureMonitorExporter() {
    }

    /**
     * Customizes an {@link AutoConfigurationCustomizer} for Azure Monitor. The connection string to the Application Insights resource is expected to be configured with the APPLICATIONINSIGHTS_CONNECTION_STRING environment variable.
     *
     * @param autoConfigurationCustomizer The OpenTelemetry autoconfiguration to set up.
     */
    public static void customize(AutoConfigurationCustomizer autoConfigurationCustomizer) {
        AzureMonitorExporterOptions exporterOptions = new AzureMonitorExporterOptions();
        customize(autoConfigurationCustomizer, exporterOptions);
    }

    /**
     * Customizes an {@link AutoConfigurationCustomizer} for Azure Monitor.
     * @param autoConfigurationCustomizer The OpenTelemetry autoconfiguration to set up.
     * @param connectionString The connection string to connect to an Application Insights resource.
     */
    public static void customize(AutoConfigurationCustomizer autoConfigurationCustomizer, String connectionString) {
        AzureMonitorExporterOptions exporterOptions
            = new AzureMonitorExporterOptions().connectionString(connectionString);
        customize(autoConfigurationCustomizer, exporterOptions);
    }

    /**
     * Customizes an {@link AutoConfigurationCustomizer} for Azure Monitor.
     * @param autoConfigurationCustomizer the {@link AutoConfigurationCustomizer} object.
     * @param exporterOptions Advanced configuration to send the data to Azure Monitor.
     */
    public static void customize(AutoConfigurationCustomizer autoConfigurationCustomizer,
        AzureMonitorExporterOptions exporterOptions) {
        autoConfigurationCustomizer.addPropertiesSupplier(() -> {
            Map<String, String> props = new HashMap<>();
            props.put("otel.traces.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put("otel.metrics.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put("otel.logs.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put(AzureMonitorExporterProviderKeys.INTERNAL_USING_AZURE_MONITOR_EXPORTER_BUILDER, "true");
            return props;
        });
        AzureMonitorExporterBuilder azureMonitorExporterBuilder = new AzureMonitorExporterBuilder();
        autoConfigurationCustomizer.addSpanExporterCustomizer((spanExporter, configProperties) -> {
            if (spanExporter instanceof AzureMonitorSpanExporterProvider.MarkerSpanExporter) {
                azureMonitorExporterBuilder.initializeIfNot(exporterOptions, configProperties);
                spanExporter = azureMonitorExporterBuilder.buildSpanExporter();
            }
            return spanExporter;
        });
        autoConfigurationCustomizer.addMetricExporterCustomizer((metricExporter, configProperties) -> {
            if (metricExporter instanceof AzureMonitorMetricExporterProvider.MarkerMetricExporter) {
                azureMonitorExporterBuilder.initializeIfNot(exporterOptions, configProperties);
                metricExporter = azureMonitorExporterBuilder.buildMetricExporter();
            }
            return metricExporter;
        });
        autoConfigurationCustomizer.addLogRecordExporterCustomizer((logRecordExporter, configProperties) -> {
            if (logRecordExporter instanceof AzureMonitorLogRecordExporterProvider.MarkerLogRecordExporter) {
                azureMonitorExporterBuilder.initializeIfNot(exporterOptions, configProperties);
                logRecordExporter = azureMonitorExporterBuilder.buildLogRecordExporter();
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
