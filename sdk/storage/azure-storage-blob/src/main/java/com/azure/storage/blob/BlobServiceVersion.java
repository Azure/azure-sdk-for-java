// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Storage Blob supported by this client library.
 */
public enum BlobServiceVersion implements ServiceVersion {
    V2019_02_02("2019-02-02");

    private final String version;

    BlobServiceVersion(String version) {
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
     * @return the latest {@link BlobServiceVersion}
     */
    public static BlobServiceVersion getLatest() {
        return V2019_02_02;
    }
}
