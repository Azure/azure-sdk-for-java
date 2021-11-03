// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Text Analytics supported by this client library.
 */
public enum TextAnalyticsServiceVersion implements ServiceVersion {
    /** The `v3.0` service version for Text Analytics **/
    V3_0("v3.0"),

    /** The `v3.1` service version for Text Analytics **/
    V3_1("v3.1"),

    /** The `v3.2-preview.2` service version for Text Analytics **/
    V3_2_PREVIEW_2("v3.2-preview.2");

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
        return V3_2_PREVIEW_2;
    }

}
