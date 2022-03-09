// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Container Registry supported by this client library.
 */
public enum ContainerRegistryServiceVersion implements ServiceVersion {
    /**
     * The api-version of 2021-07-01.
     */
    V2021_07_01("2021-07-01");

    private final String version;

    /**
     * The service version.
     * @param version The service version.
     */
    ContainerRegistryServiceVersion(String version) {
        this.version = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link ContainerRegistryServiceVersion }
     */
    public static ContainerRegistryServiceVersion getLatest() {
        return V2021_07_01;
    }
}

