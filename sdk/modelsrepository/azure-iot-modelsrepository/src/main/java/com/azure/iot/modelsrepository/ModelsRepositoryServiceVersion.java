// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.iot.modelsrepository;

import com.azure.core.util.ServiceVersion;

/**
 * The service API versions of Azure Models Repository that are supported by this client.
 */
public enum ModelsRepositoryServiceVersion implements ServiceVersion {
    V2021_02_11("2021_02_11");

    private final String version;

    ModelsRepositoryServiceVersion(String version) {
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
     * Gets the latest service API version of Azure Models Repository that is supported by this client.
     *
     * @return The latest service API version of Azure Models Repository that is supported by this client.
     */
    public static ModelsRepositoryServiceVersion getLatest() {
        return V2021_02_11;
    }
}
