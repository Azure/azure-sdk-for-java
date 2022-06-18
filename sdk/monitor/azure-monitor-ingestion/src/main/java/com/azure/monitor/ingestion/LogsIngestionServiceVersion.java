// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.ingestion;

import com.azure.core.util.ServiceVersion;

/**
 * The service version of the Azure Monitor service to upload logs.
 */
public enum LogsIngestionServiceVersion implements ServiceVersion {
    /**
     * Service version {@code v1}.
     */
    V2021_11_01_PREVIEW("2021-11-01-preview");

    String version;

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
        return V2021_11_01_PREVIEW;
    }

    @Override
    public String getVersion() {
        return version;
    }
}
