// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.traffic;

import com.azure.core.util.ServiceVersion;

/**
 * Contains the versions of the Traffic Service available for the clients.
 */
public enum TrafficServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 1.0}.
     */
    V1_0("1.0");

    private final String version;

    /**
     * Creates a new {@link TrafficServiceVersion} with a version string.
     *
     * @param version the service version
     */
    TrafficServiceVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link TrafficServiceVersion}
     */
    public static TrafficServiceVersion getLatest() {
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
