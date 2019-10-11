// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.appconfiguration;

/**
 * The versions of Azure App Configuration supported by this client library.
 */
public enum ServiceVersion {
    V1_0("1.0");

    private final String version;

    ServiceVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the string representation of the {@link ServiceVersion}
     *
     * @return the string representation of the {@link ServiceVersion}
     */
    public String getVersionString() {
        return this.version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link ServiceVersion}
     */
    public static ServiceVersion getLatest() {
        return V1_0;
    }
}
