// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure App Configuration supported by this client library.
 */
public enum ConfigurationServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 1.0}.
     */
    V1_0("1.0"),

    /**
     * Service version {@code 2023-10-01}.
     */
    V2023_10_01("2023-10-01"),

    /**
     * Service version {@code 2023-11-01}.
     */
    V2023_11_01("2023-11-01");

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
        return V2023_11_01;
    }
}
