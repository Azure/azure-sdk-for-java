// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.sas;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Storage Blob Sas supported by this client library.
 */
public enum BlobSasServiceVersion implements ServiceVersion {
    V2019_02_02("2019-02-02"),
    V2019_07_07("2019-07-07"),
    V2019_12_12("2019-12-12");

    private final String version;

    BlobSasServiceVersion(String version) {
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
     * Gets the latest Sas service version supported by this client library
     *
     * @return the latest {@link BlobSasServiceVersion}
     */
    public static BlobSasServiceVersion getLatest() {
        return V2019_12_12;
    }
}
