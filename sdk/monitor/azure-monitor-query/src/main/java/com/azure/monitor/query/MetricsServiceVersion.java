// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Monitor Metrics Query supported by this client library.
 */
public enum MetricsServiceVersion implements ServiceVersion {
    /**
     * The preview version of the Metrics Query service.
     */
    V2024_02_01("2024-02-01");

    private final String version;

    MetricsServiceVersion(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Gets the latest service version supported by this client library.
     * @return the latest {@link MetricsServiceVersion}.
     */
    public static MetricsServiceVersion getLatest() {
        return V2024_02_01;
    }
}
