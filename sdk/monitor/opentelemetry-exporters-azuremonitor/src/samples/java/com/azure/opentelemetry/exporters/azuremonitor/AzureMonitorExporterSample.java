// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.opentelemetry.exporters.azuremonitor;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;

import java.util.Collections;

/**
 * Sample to demonstrate exporting OpenTelemetry {@link SpanData} to Azure Monitor through {@link AzureMonitorExporter}.
 */
public class AzureMonitorExporterSample {

    /**
     * Main method to start the sample application
     * @param args Ignore args.
     */
    public static void main(String[] args) {
        AzureMonitorExporter azureMonitorExporter = new AzureMonitorExporterBuilder()
            .instrumentationKey("{instrumentation-key}")
            .buildExporter();
        CompletableResultCode resultCode =
            azureMonitorExporter.export(Collections.singleton(new AzureMonitorExporterTest.RequestSpanData()));
        System.out.println("Export operation status: " + resultCode.isSuccess());
    }
}
