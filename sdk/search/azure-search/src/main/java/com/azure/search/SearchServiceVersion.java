// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Search supported by this client library.
 */
public enum SearchServiceVersion implements ServiceVersion {
    V2019_05_06("2019-05-06");

    private final String version;

    SearchServiceVersion(String version) {
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
     * @return the latest {@link SearchServiceVersion}
     */
    public static SearchServiceVersion getLatest() {
        return V2019_05_06;
    }
}
