package com.azure.monitor.query;

import com.azure.core.util.ServiceVersion;

/**
 * The service version of the batch metrics service that can be queried to retrieved Azure Monitor metrics.
 */
public enum MetricsBatchQueryServiceVersion  implements ServiceVersion {

    V2023_05_01_PREVIEW("2023-05-01-preview");

    String version;

    /**
     * The service version.
     * @param version The service version.
     */
    MetricsBatchQueryServiceVersion(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return null;
    }

    /**
     * Returns the latest supported service version by this library.
     * @return The latest supported service version by this library.
     */
    public static MetricsBatchQueryServiceVersion getLatest() {
        return V2023_05_01_PREVIEW;
    }
}
