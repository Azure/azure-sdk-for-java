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
        if (configProperties.getBoolean(AzureMonitorExporterProviderKeys.INTERNAL_USING_BUILDER, false)) {
            return MarkerSpanExporter.INSTANCE;
        }
        throw new IllegalStateException(getName() + " currently only supports usage via " + AzureMonitorExporterBuilder.class.getName());
    }

    @Override
    public String getName() {
        return AzureMonitorExporterProviderKeys.EXPORTER_NAME;
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
