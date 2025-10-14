// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.autoconfigure;

import com.azure.monitor.opentelemetry.autoconfigure.implementation.AzureMonitorExporterProviderKeys;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.AzureMonitorLogRecordExporterProvider;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.AzureMonitorMetricExporterProvider;
import com.azure.monitor.opentelemetry.autoconfigure.implementation.AzureMonitorSpanExporterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.resources.Resource;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Class to enable Azure Monitor for OpenTelemetry autoconfiguration.
 */
public final class AzureMonitorAutoConfigure {

    private AzureMonitorAutoConfigure() {
    }

    /**
     * Customizes an {@link AutoConfigurationCustomizer} for Azure Monitor. The connection string to the Application Insights resource is expected to be configured with the APPLICATIONINSIGHTS_CONNECTION_STRING environment variable.
     *
     * @param autoConfigurationCustomizer The OpenTelemetry autoconfiguration to set up.
     */
    public static void customize(AutoConfigurationCustomizer autoConfigurationCustomizer) {
        AzureMonitorAutoConfigureOptions exporterOptions = new AzureMonitorAutoConfigureOptions();
        customize(autoConfigurationCustomizer, exporterOptions);
    }

    /**
     * Customizes an {@link AutoConfigurationCustomizer} for Azure Monitor.
     * @param autoConfigurationCustomizer The OpenTelemetry autoconfiguration to set up.
     * @param connectionString The connection string to connect to an Application Insights resource.
     */
    public static void customize(AutoConfigurationCustomizer autoConfigurationCustomizer, String connectionString) {
        AzureMonitorAutoConfigureOptions exporterOptions
            = new AzureMonitorAutoConfigureOptions().connectionString(connectionString);
        customize(autoConfigurationCustomizer, exporterOptions);
    }

    /**
     * Customizes an {@link AutoConfigurationCustomizer} for Azure Monitor.
     * @param autoConfigurationCustomizer the {@link AutoConfigurationCustomizer} object.
     * @param autoConfigureOptions Advanced configuration to send the data to Azure Monitor.
     */
    public static void customize(AutoConfigurationCustomizer autoConfigurationCustomizer,
        AzureMonitorAutoConfigureOptions autoConfigureOptions) {
        autoConfigurationCustomizer.addPropertiesSupplier(() -> {
            Map<String, String> props = new HashMap<>();
            props.put("otel.traces.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put("otel.metrics.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put("otel.logs.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put(AzureMonitorExporterProviderKeys.INTERNAL_USING_AZURE_MONITOR_EXPORTER_BUILDER, "true");
            return props;
        });
        final AtomicReference<Resource> otelResource = new AtomicReference<>();
        autoConfigurationCustomizer.addResourceCustomizer((resource, configProperties) -> {
            otelResource.set(resource);
            return resource;
        });
        AzureMonitorExporterBuilder azureMonitorExporterBuilder = new AzureMonitorExporterBuilder();
        autoConfigurationCustomizer.addSpanExporterCustomizer((spanExporter, configProperties) -> {
            if (spanExporter instanceof AzureMonitorSpanExporterProvider.MarkerSpanExporter) {
                azureMonitorExporterBuilder.initializeIfNot(autoConfigureOptions, configProperties, otelResource.get());
                spanExporter = azureMonitorExporterBuilder.buildSpanExporter();
            }
            return spanExporter;
        });
        autoConfigurationCustomizer.addMetricExporterCustomizer((metricExporter, configProperties) -> {
            if (metricExporter instanceof AzureMonitorMetricExporterProvider.MarkerMetricExporter) {
                azureMonitorExporterBuilder.initializeIfNot(autoConfigureOptions, configProperties, otelResource.get());
                metricExporter = azureMonitorExporterBuilder.buildMetricExporter();
            }
            return metricExporter;
        });
        autoConfigurationCustomizer.addLogRecordExporterCustomizer((logRecordExporter, configProperties) -> {
            if (logRecordExporter instanceof AzureMonitorLogRecordExporterProvider.MarkerLogRecordExporter) {
                azureMonitorExporterBuilder.initializeIfNot(autoConfigureOptions, configProperties, otelResource.get());
                logRecordExporter = azureMonitorExporterBuilder.buildLogRecordExporter();
            }
            return logRecordExporter;
        });
        autoConfigurationCustomizer.addTracerProviderCustomizer((sdkTracerProviderBuilder, configProperties) -> {
            azureMonitorExporterBuilder.initializeIfNot(autoConfigureOptions, configProperties, otelResource.get());
            if (LiveMetrics.isEnabled(configProperties)) {
                sdkTracerProviderBuilder.addSpanProcessor(azureMonitorExporterBuilder.buildLiveMetricsSpanProcesor());
            }
            return sdkTracerProviderBuilder;
        });
        autoConfigurationCustomizer
            .addMeterProviderCustomizer((sdkMeterProviderBuilder, config) -> sdkMeterProviderBuilder
                .registerView(InstrumentSelector.builder().setMeterName("io.opentelemetry.sdk.trace").build(),
                    View.builder().setAggregation(Aggregation.drop()).build())
                .registerView(InstrumentSelector.builder().setMeterName("io.opentelemetry.sdk.logs").build(),
                    View.builder().setAggregation(Aggregation.drop()).build()));
    }
}
