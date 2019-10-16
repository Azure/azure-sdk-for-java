// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Storage File supported by this client library.
 */
public enum FileServiceVersion implements ServiceVersion {
    V2019_02_02("2019-02-02");

    private final String version;

    FileServiceVersion(String version) {
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
     * @return the latest {@link FileServiceVersion}
     */
    public static FileServiceVersion getLatest() {
        return V2019_02_02;
    }
}
