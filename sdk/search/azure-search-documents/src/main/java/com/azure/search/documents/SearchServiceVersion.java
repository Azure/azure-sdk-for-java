// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Cognitive Search supported by this client library.
 */
public enum SearchServiceVersion implements ServiceVersion {
    V2020_06_30("2020-06-30");

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
     * Gets the latest service version supported by this client library.
     *
     * @return The latest version supported by this client library.
     */
    public static SearchServiceVersion getLatest() {
        return V2020_06_30;
    }
}
