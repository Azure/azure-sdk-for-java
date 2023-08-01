// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Text Analytics supported by this client library.
 */
public enum TextAnalyticsServiceVersion implements ServiceVersion {
    /**
     * Service version {@code v3.0}.
     */
    V3_0("v3.0"),

    /**
     * Service version {@code v3.1}.
     */
    V3_1("v3.1"),

    /**
     * Service Version {@code 2022-05-01}. A newer version than V3_0 and V3_1.
     */
    V2022_05_01("2022-05-01"),

    /**
     * Service Version {@code 2023-04-01}. The latest version.
     */
    V2023_04_01("2023-04-01");

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
        return V2023_04_01;
    }

}
