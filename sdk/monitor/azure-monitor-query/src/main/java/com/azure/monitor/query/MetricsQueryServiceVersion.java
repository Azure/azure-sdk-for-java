// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.util.ServiceVersion;

/**
 * The service version of the Metrics service that can be queried to retrieved Azure Monitor metrics.
 */
public enum MetricsQueryServiceVersion implements ServiceVersion {
    V2018_01_01("2018-01-01");

    String version;

    /**
     * The service version.
     * @param version The service version.
     */
    MetricsQueryServiceVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the latest supported service version by this library.
     * @return The latest supported service version by this library.
     */
    public static MetricsQueryServiceVersion getLatest() {
        return V2018_01_01;
    }

    @Override
    public String getVersion() {
        return version;
    }
}
