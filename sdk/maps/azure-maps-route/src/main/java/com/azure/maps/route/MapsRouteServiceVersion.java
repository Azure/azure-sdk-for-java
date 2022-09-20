// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.route;

import com.azure.core.util.ServiceVersion;

/**
 * Contains the versions of the Search Service available for the clients.
 */
public enum MapsRouteServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 1.0}.
     */
    V1_0("1.0");

    private final String version;

    /**
     * Creates a new {@link MapsRouteServiceVersion} with a version string.
     *
     * @param version the version of the service
     */
    MapsRouteServiceVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link MapsRouteServiceVersion}
     */
    public static MapsRouteServiceVersion getLatest() {
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
