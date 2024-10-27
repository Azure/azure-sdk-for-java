// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure AI Search supported by this client library.
 */
public enum SearchServiceVersion implements ServiceVersion {
    /**
     * {@code 2020-06-30} service version.
     */
    V2020_06_30("2020-06-30"),

    /**
     * {@code 2023-11-01} service version.
     */
    V2023_11_01("2023-11-01"),

    /**
     * {@code 2024-07-01} service version.
     */
    V2024_07_01("2024-07-01");

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
        return V2024_07_01;
    }
}
