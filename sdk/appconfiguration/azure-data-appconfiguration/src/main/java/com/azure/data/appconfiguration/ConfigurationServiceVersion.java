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
     * Service version {@code 2022-11-01-preview}.
     */
    V2022_11_01_PREVIEW("2022-11-01-preview");

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
        return V2022_11_01_PREVIEW;
    }
}
