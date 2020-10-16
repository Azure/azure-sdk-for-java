// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core;

import com.azure.core.util.ServiceVersion;

/**
 * The service API versions of Azure Digital Twins that are supported by this client.
 */
public enum DigitalTwinsServiceVersion implements ServiceVersion {
    V2020_10_31("2020-10-31");

    private final String version;

    DigitalTwinsServiceVersion(String version) {
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
     * Gets the latest service API version of Azure Digital Twins that is supported by this client.
     * @return The latest service API version of Azure Digital Twins that is supported by this client.
     */
    public static DigitalTwinsServiceVersion getLatest() {
        return V2020_10_31;
    }
}
