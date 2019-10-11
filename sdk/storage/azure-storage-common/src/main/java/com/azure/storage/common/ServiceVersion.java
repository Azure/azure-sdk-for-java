// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common;

/**
 * The versions of Azure Storage supported by this client library.
 */
public enum ServiceVersion {
    V2018_11_09("2011-08-18");

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
        return V2018_11_09;
    }
}
