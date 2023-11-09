// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

/**
 * This class is used to instantiate an instance of {@link AzureMonitorExporterBuilder}
 */
public final class AzureMonitorExporter {

    private static final AzureMonitorExporterBuilder builder = new AzureMonitorExporterBuilder();

    /**
     *
     * @return an instance of {@link AzureMonitorExporterBuilder}.
     */
    public static AzureMonitorExporterBuilder builder() {
        return builder;
    }
}
