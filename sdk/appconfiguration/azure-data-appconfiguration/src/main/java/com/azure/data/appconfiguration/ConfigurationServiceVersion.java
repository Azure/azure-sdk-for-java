// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure App Configuration supported by this client library.
 */
public enum ConfigurationServiceVersion implements ServiceVersion {
    V1_0("1.0");

    private final String version;

    ConfigurationServiceVersion(String version) {
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
     * @return the latest {@link ConfigurationServiceVersion}
     */
    public static ConfigurationServiceVersion getLatest() {
        return V1_0;
    }
}
