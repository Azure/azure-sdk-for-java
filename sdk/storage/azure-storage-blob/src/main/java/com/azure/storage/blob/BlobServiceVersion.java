// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Storage Blob supported by this client library.
 */
public enum BlobServiceVersion implements ServiceVersion {
    V2019_02_02("2019-02-02"),
    V2019_07_07("2019-07-07"),
    V2019_12_12("2019-12-12"),
    V2020_02_10("2020-02-10"),
    V2020_04_08("2020-04-08"),
    V2020_06_12("2020-06-12"),
    V2020_08_04("2020-08-04");

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
        return V2020_08_04;
    }
}
