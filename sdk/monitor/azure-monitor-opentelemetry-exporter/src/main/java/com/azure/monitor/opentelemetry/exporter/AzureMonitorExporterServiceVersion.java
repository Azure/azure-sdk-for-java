// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Monitor service supported by this client library.
 */
public enum AzureMonitorExporterServiceVersion implements ServiceVersion {
    V2020_09_15_PREVIEW("2020-09-15_Preview");

    private final String version;

    AzureMonitorExporterServiceVersion(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Gets the latest service version supported by this client library.
     * @return the latest service version.
     */
    public static AzureMonitorExporterServiceVersion getLatest() {
        return V2020_09_15_PREVIEW;
    }
}
