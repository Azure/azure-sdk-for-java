// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.timezone;

import com.azure.core.util.ServiceVersion;

/**
 * TimeZone Service Version
 */
public enum TimeZoneServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 1.0}.
     */
    V1_0("1.0");

    private final String version;

    /**
     * Creates a new {@link TimeZoneServiceVersion} with a version string.
     *
     * @param version service version
     */
    TimeZoneServiceVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link TimeZoneServiceVersion}
     */
    public static TimeZoneServiceVersion getLatest() {
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
