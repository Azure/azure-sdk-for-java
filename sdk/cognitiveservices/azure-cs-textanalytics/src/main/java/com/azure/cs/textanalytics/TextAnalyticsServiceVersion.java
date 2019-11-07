// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cs.textanalytics;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Text Analytics supported by this client library.
 */
public enum TextAnalyticsServiceVersion implements ServiceVersion {
    V1_0("1.0");

    private final String version;

    TextAnalyticsServiceVersion(String version) {
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
     * @return the latest {@link TextAnalyticsServiceVersion}
     */
    public static TextAnalyticsServiceVersion getLatest() {
        return V1_0;
    }

}
