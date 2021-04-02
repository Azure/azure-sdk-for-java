// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser;

import com.azure.core.util.ServiceVersion;

/**
 * The service API versions of Azure DigitalTwins Model Parser that are supported by this client.
 */
public enum ParserServiceVersion implements ServiceVersion {
    V2021_02_11("2021_02_11");

    private final String version;

    ParserServiceVersion(String version) {
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
     * Gets the latest service API version of Azure DigitalTwins Model Parser that is supported by this client.
     *
     * @return The latest service API version of Azure DigitalTwins Model Parser that is supported by this client.
     */
    public static ParserServiceVersion getLatest() {
        return V2021_02_11;
    }
}
