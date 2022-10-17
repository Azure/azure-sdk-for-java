// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.geolocation;

import com.azure.core.util.ServiceVersion;

/**
 * Contains the versions of the GeoLocation Service available for the clients.
 */
public enum GeolocationServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 1.0}.
     */
    V1_0("1.0");

    private final String version;

    /**
     * Creates a new {@link GeolocationServiceVersion} with a version string.
     *
     * @param version the version string
     */
    GeolocationServiceVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link GeolocationServiceVersion}
     */
    public static GeolocationServiceVersion getLatest() {
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
