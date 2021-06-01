// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.containers.containerregistry;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Container Registry supported by this client library.
 */
public enum ContainerRegistryServiceVersion implements ServiceVersion {
    V1_0("1");

    private final String version;

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
        return V1_0;
    }
}

