package com.azure.maps.timezone;

import com.azure.core.util.ServiceVersion;

/**
 * Timezone Service Version
 */
public enum TimezoneServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 1.0}.
     */
    V1_0("1.0");

    private final String version;

    /**
     * Creates a new {@link TimezoneServiceVersion} with a version string.
     *
     * @param version
     */
    TimezoneServiceVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link TimezoneServiceVersion}
     */
    public static TimezoneServiceVersion getLatest() {
        return V1_0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return this.version;
    }
}
