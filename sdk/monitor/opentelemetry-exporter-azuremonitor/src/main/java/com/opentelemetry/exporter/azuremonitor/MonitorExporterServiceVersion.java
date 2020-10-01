// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.opentelemetry.exporter.azuremonitor;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Monitor Exporter supported by this client library.
 */
public enum MonitorExporterServiceVersion implements ServiceVersion {
    V2020_09_15_PREVIEW("2020-09-15_Preview");

    private final String version;

    MonitorExporterServiceVersion(String version) {
        this.version = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link MonitorExporterServiceVersion}
     */
    public static MonitorExporterServiceVersion getLatest() {
        return V2020_09_15_PREVIEW;
    }
}
