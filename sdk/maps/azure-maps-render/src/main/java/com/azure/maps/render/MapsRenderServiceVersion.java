// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.maps.render;

import com.azure.core.util.ServiceVersion;

/**
 * Contains the versions of the Render Service available for the clients.
 */
public enum MapsRenderServiceVersion implements ServiceVersion {
    /**
    * Service version {@code 2}.
    */
    V2("2024-04-01");

    private final String version;

    /**
     * Creates a new {@link MapsRenderServiceVersion} with a version string.
     *
     * @param version the service version
     */
    MapsRenderServiceVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link MapsRenderServiceVersion}
     */
    public static MapsRenderServiceVersion getLatest() {
        return V2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return this.version;
    }
}
