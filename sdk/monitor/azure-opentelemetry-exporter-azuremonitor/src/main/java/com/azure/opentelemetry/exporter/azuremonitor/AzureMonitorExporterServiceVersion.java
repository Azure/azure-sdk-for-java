// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.opentelemetry.exporter.azuremonitor;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Monitor service supported by this client library.
 */
public enum AzureMonitorExporterServiceVersion implements ServiceVersion {
    V2("2");

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
        return V2;
    }
}
