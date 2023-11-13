package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.http.HttpPipeline;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorExporterProviderKeys;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorLogRecordExporterProvider;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorMetricExporterProvider;
import com.azure.monitor.opentelemetry.exporter.implementation.AzureMonitorSpanExporterProvider;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.View;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Use @link AutoConfiguredOpenTelemetrySdkBuilder} to install the azure monitor exporters.
 */
public class AzureMonitorInstaller {

    private final AutoConfiguredOpenTelemetrySdkBuilder autoConfiguredOpenTelemetrySdkBuilder;
    private final AzureMonitorExporterBuilder azureMonitorExporterBuilder = new AzureMonitorExporterBuilder();

    /**
     * Construct an instance of AzureMonitorInstaller
     * @param autoConfiguredOpenTelemetrySdkBuilder {@link AutoConfiguredOpenTelemetrySdkBuilder} installs the azure monitor exporters.
     */
    public AzureMonitorInstaller(AutoConfiguredOpenTelemetrySdkBuilder autoConfiguredOpenTelemetrySdkBuilder) {
        this.autoConfiguredOpenTelemetrySdkBuilder = autoConfiguredOpenTelemetrySdkBuilder;
    }

    /**
     * Sets the connection string to use for exporting telemetry events to Azure Monitor.
     *
     * @param connectionString The connection string for the Azure Monitor resource.
     * @return the updated {@link AzureMonitorInstaller} object.
     */
    public AzureMonitorInstaller connectionString(String connectionString) {
        azureMonitorExporterBuilder.connectionString(connectionString);
        return this;
    }

    /**
     * Customize a new instance of {@link AzureMonitorExporterBuilder}
     * @param consumer the consumer value to set.
     * @return the updated {@link AzureMonitorInstaller} object.
     */
    public AzureMonitorInstaller exporterCustomizer(Consumer<AzureMonitorExporterBuilder> consumer) {
        consumer.accept(azureMonitorExporterBuilder);
        return this;
    }

    /**
     * Configures an {@link AutoConfiguredOpenTelemetrySdkBuilder} based on the options set in the builder.
     */
    public void install() {
        autoConfiguredOpenTelemetrySdkBuilder.addPropertiesSupplier(() -> {
            Map<String, String> props = new HashMap<>();
            props.put("otel.traces.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put("otel.metrics.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put("otel.logs.exporter", AzureMonitorExporterProviderKeys.EXPORTER_NAME);
            props.put(AzureMonitorExporterProviderKeys.INTERNAL_USING_AZURE_MONITOR_EXPORTER_BUILDER, "true");
            return props;
        });
        autoConfiguredOpenTelemetrySdkBuilder.addSpanExporterCustomizer(
            (spanExporter, configProperties) -> {
                if (spanExporter instanceof AzureMonitorSpanExporterProvider.MarkerSpanExporter) {
                    azureMonitorExporterBuilder.internalBuildAndFreeze(configProperties);
                    spanExporter = azureMonitorExporterBuilder.buildTraceExporter(configProperties);
                }
                return spanExporter;
            });
        autoConfiguredOpenTelemetrySdkBuilder.addMetricExporterCustomizer(
            (metricExporter, configProperties) -> {
                if (metricExporter instanceof AzureMonitorMetricExporterProvider.MarkerMetricExporter) {
                    azureMonitorExporterBuilder.internalBuildAndFreeze(configProperties);
                    metricExporter = azureMonitorExporterBuilder.buildMetricExporter(configProperties);
                }
                return metricExporter;
            });
        autoConfiguredOpenTelemetrySdkBuilder.addLogRecordExporterCustomizer(
            (logRecordExporter, configProperties) -> {
                if (logRecordExporter instanceof AzureMonitorLogRecordExporterProvider.MarkerLogRecordExporter) {
                    azureMonitorExporterBuilder.internalBuildAndFreeze(configProperties);
                    logRecordExporter = azureMonitorExporterBuilder.buildLogRecordExporter(configProperties);
                }
                return logRecordExporter;
            });
        // TODO (trask)
//        sdkBuilder.addTracerProviderCustomizer((sdkTracerProviderBuilder, configProperties) -> {
//            QuickPulse quickPulse = QuickPulse.create(getHttpPipeline());
//            return sdkTracerProviderBuilder.addSpanProcessor(
//                new LiveMetricsSpanProcessor(quickPulse, createSpanDataMapper()));
//        });
        autoConfiguredOpenTelemetrySdkBuilder.addMeterProviderCustomizer((sdkMeterProviderBuilder, config) ->
            sdkMeterProviderBuilder.registerView(
                InstrumentSelector.builder()
                    .setMeterName("io.opentelemetry.sdk.trace")
                    .build(),
                View.builder()
                    .setAggregation(Aggregation.drop())
                    .build()
            ).registerView(
                InstrumentSelector.builder()
                    .setMeterName("io.opentelemetry.sdk.logs")
                    .build(),
                View.builder()
                    .setAggregation(Aggregation.drop())
                    .build()
            ));
    }
}
