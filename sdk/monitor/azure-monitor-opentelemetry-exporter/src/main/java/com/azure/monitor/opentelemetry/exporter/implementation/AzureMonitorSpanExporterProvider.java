package com.azure.monitor.opentelemetry.exporter.implementation;

import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.traces.ConfigurableSpanExporterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.util.Collection;

public class AzureMonitorSpanExporterProvider implements ConfigurableSpanExporterProvider {
    @Override
    public SpanExporter createExporter(ConfigProperties configProperties) {
        if (configProperties.getBoolean("_internal_azuremonitorexporterbuilder", false)) {
            return MarkerSpanExporter.INSTANCE;
        }
        return new AzureMonitorExporterBuilder().buildTraceExporter();
    }

    @Override
    public String getName() {
        return "azmon";
    }

    public enum MarkerSpanExporter implements SpanExporter {

        INSTANCE;

        @Override
        public CompletableResultCode export(Collection<SpanData> collection) {
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
    }
}
