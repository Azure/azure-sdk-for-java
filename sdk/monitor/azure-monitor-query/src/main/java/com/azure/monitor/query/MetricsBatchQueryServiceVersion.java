// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.query;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Monitor Metrics Batch Query supported by this client library.
 */
public enum MetricsBatchQueryServiceVersion implements ServiceVersion {
    /**
     * The preview version of the Metrics Batch Query service.
     */
    V2023_10_01("2023-10-01");

    private final String version;

    MetricsBatchQueryServiceVersion(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Gets the latest service version supported by this client library.
     * @return the latest {@link MetricsBatchQueryServiceVersion}.
     */
    public static MetricsBatchQueryServiceVersion getLatest() {
        return V2023_10_01;
    }
}
