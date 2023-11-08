// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.weather;

import com.azure.core.util.ServiceVersion;

/**
 * Contains the versions of the Search Service available for the clients.
 */
public enum WeatherServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 1.0}.
     */
    V1_0("1.0"),

    /**
     * Service version {@code 1.0}.
     */
    V1_1("1.1");

    private final String version;

    /**
     * Creates a new {@link WeatherServiceVersion} with a version string.
     *
     * @param version Service version
     */
    WeatherServiceVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link WeatherServiceVersion}
     */
    public static WeatherServiceVersion getLatest() {
        return V1_1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return this.version;
    }
}
