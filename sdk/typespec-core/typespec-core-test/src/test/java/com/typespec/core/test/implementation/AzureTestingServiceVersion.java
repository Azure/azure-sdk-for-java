// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.test.implementation;

import com.typespec.core.util.ServiceVersion;

/**
 * Testing implementation of {@link ServiceVersion}.
 */
public enum AzureTestingServiceVersion implements ServiceVersion {
    ALPHA("alpha"),
    BETA("beta"),
    GA("ga");

    private final String version;

    AzureTestingServiceVersion(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Gets the latest {@link AzureTestingServiceVersion}.
     *
     * @return The latest {@link AzureTestingServiceVersion}.
     */
    public static AzureTestingServiceVersion getLatest() {
        return GA;
    }
}
