// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation;

import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.metrics.ConfigurableMetricExporterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

import java.util.Collection;

public final class AzureMonitorMetricExporterProvider implements ConfigurableMetricExporterProvider {
    @Override
    public MetricExporter createExporter(ConfigProperties configProperties) {
        if (configProperties.getBoolean(AzureMonitorExporterProviderKeys.INTERNAL_USING_AZURE_MONITOR_EXPORTER_BUILDER,
            false)) {
            return AzureMonitorMetricExporterProvider.MarkerMetricExporter.INSTANCE;
        }
        throw new IllegalStateException(
            getName() + " currently only supports usage via " + AzureMonitorExporterBuilder.class.getName());
    }

    @Override
    public String getName() {
        return AzureMonitorExporterProviderKeys.EXPORTER_NAME;
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
