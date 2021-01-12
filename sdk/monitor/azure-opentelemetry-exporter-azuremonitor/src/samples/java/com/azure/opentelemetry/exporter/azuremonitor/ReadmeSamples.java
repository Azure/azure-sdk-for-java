// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.opentelemetry.exporter.azuremonitor;


import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;

import java.util.Collection;
import java.util.Collections;

/**
 * WARNING: MODIFYING THIS FILE WILL REQUIRE CORRESPONDING UPDATES TO README.md FILE. LINE NUMBERS
 * ARE USED TO EXTRACT APPROPRIATE CODE SEGMENTS FROM THIS FILE. ADD NEW CODE AT THE BOTTOM TO AVOID CHANGING
 * LINE NUMBERS OF EXISTING CODE SAMPLES.
 *
 * Code samples for the README.md
 */
public class ReadmeSamples {

    /**
     * Sample for creating Azure Monitor Exporter.
     */
    public void createExporter() {
        AzureMonitorExporter azureMonitorExporter = new AzureMonitorExporterBuilder()
            .connectionString("{connection-string}")
            .buildExporter();
    }

    /**
     * Sample for exporting span data to Azure Monitor.
     */
    public void exportSpanData() {
        AzureMonitorExporter azureMonitorExporter = new AzureMonitorExporterBuilder()
            .connectionString("{connection-string}")
            .buildExporter();

        CompletableResultCode resultCode = azureMonitorExporter.export(getSpanDataCollection());
        System.out.println(resultCode.isSuccess());
    }

    /**
     * Method to make the sample compilable but is not visible in README code snippet.
     * @return An empty collection.
     */
    private Collection<SpanData> getSpanDataCollection() {
        return Collections.emptyList();
    }

}
