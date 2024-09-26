// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation;

import com.azure.monitor.opentelemetry.exporter.AzureMonitorExporterBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.logs.ConfigurableLogRecordExporterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;

import java.util.Collection;

public final class AzureMonitorLogRecordExporterProvider implements ConfigurableLogRecordExporterProvider {
    @Override
    public LogRecordExporter createExporter(ConfigProperties configProperties) {
        if (configProperties.getBoolean(AzureMonitorExporterProviderKeys.INTERNAL_USING_AZURE_MONITOR_EXPORTER_BUILDER,
            false)) {
            return AzureMonitorLogRecordExporterProvider.MarkerLogRecordExporter.INSTANCE;
        }
        throw new IllegalStateException(
            getName() + " currently only supports usage via " + AzureMonitorExporterBuilder.class.getName());
    }

    @Override
    public String getName() {
        return AzureMonitorExporterProviderKeys.EXPORTER_NAME;
    }

    public enum MarkerLogRecordExporter implements LogRecordExporter {

        INSTANCE;

        @Override
        public CompletableResultCode export(Collection<LogRecordData> collection) {
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
