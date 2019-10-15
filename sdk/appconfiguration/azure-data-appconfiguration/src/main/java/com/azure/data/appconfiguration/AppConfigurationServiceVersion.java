// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

/**
 * The versions of Azure App Configuration supported by this client library.
 */
public enum AppConfigurationServiceVersion implements com.azure.core.http.ServiceVersion {
    V1_0("1.0");

    private final String version;

    AppConfigurationServiceVersion(String version) {
        this.version = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersionString() {
        return this.version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link AppConfigurationServiceVersion}
     */
    public static AppConfigurationServiceVersion getLatest() {
        return V1_0;
    }
}
