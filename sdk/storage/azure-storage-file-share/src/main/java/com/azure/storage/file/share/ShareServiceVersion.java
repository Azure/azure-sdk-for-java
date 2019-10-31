// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;


import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Storage File supported by this client library.
 */
public enum ShareServiceVersion implements ServiceVersion {
    V2019_02_02("2019-02-02");

    private final String version;

    ShareServiceVersion(String version) {
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
     * @return the latest {@link ShareServiceVersion}
     */
    public static ShareServiceVersion getLatest() {
        return V2019_02_02;
    }
}
