// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.search;

import com.azure.core.util.ServiceVersion;

/**
 * Contains the versions of the Search Service available for the clients.
 */
public enum MapsSearchServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 2.0}.
     */
    V2023_06_01("2023-06-01");

    private final String version;

    /**
     * Creates a new {@link MapsSearchServiceVersion} with a version string.
     *
     * @param version the version string
     */
    MapsSearchServiceVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link MapsSearchServiceVersion}
     */
    public static MapsSearchServiceVersion getLatest() {
        return V2023_06_01;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return this.version;
    }
}
