package com.azure.monitor.opentelemetry.exporter.implementation;

import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.util.Collection;

public class AzureMonitorMetricExporterProvider implements ConfigurableMetricExporterProvider {
    @Override
    public MetricExporter createExporter(ConfigProperties configProperties) {
        if (configProperties.getBoolean("_internal_azuremonitorexporterbuilder", false)) {
            return AzureMonitorMetricExporterProvider.MarkerMetricExporter.INSTANCE;
        }
        return new AzureMonitorExporterBuilder().buildMetricExporter();
    }

    @Override
    public String getName() {
        return "azmon";
    }

    public enum MarkerMetricExporter implements MetricExporter {

        INSTANCE;

        @Override
        public CompletableResultCode export(Collection<MetricData> collection) {
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode flush() {
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public CompletableResultCode shutdown() {
            return CompletableResultCode.ofSuccess();
        }

        @Override
        public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
            return AggregationTemporality.DELTA;
        }
    }
}
