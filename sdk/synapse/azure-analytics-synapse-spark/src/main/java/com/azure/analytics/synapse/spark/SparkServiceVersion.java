// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.synapse.spark;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Synapse Analytics Access Control supported by this client library.
 */
public enum SparkServiceVersion implements ServiceVersion {
    V2019_11_01_preview("2019-11-01-preview");

    private final String version;

    SparkServiceVersion(String version) {
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
     * @return the latest {@link SparkServiceVersion}
     */
    public static SparkServiceVersion getLatest() {
        return V2019_11_01_preview;
    }
}
