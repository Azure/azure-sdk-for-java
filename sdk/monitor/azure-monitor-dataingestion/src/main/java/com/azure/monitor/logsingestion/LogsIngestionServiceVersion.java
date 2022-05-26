package com.azure.monitor.logsingestion;

import com.azure.core.util.ServiceVersion;

public enum LogsIngestionServiceVersion implements ServiceVersion {
    /**
     * Service version {@code v1}.
     */
    V_1("v1");

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
        return V_1;
    }

    @Override
    public String getVersion() {
        return version;
    }
}
