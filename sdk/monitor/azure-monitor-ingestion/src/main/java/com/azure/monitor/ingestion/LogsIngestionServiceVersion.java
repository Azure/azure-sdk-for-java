// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.util.ServiceVersion;

/**
 * The service version of the Azure Monitor service to upload logs.
 */
public enum LogsIngestionServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 2023-01-01}.
     */
    V2023_01_01("2023-01-01");

    final String version;

    /**
     * The service version.
     * @param version The service version.
     */
    LogsIngestionServiceVersion(String version) {
        this.version = version;
    }

    /**
     * Returns the latest supported service version by this library.
     * @return The latest supported service version by this library.
     */
    public static LogsIngestionServiceVersion getLatest() {
        return V2023_01_01;
    }

    @Override
    public String getVersion() {
        return version;
    }
}
