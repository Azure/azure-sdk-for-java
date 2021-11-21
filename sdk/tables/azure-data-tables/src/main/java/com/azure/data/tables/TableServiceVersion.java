// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Tables REST API supported by this client library.
 */
public enum TableServiceVersion implements ServiceVersion {
    /**
     * API version 2019-02-02
     */
    V2019_02_02("2019-02-02");

    private final String version;

    TableServiceVersion(String version) {
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
     * Gets the latest REST API version supported by this client library.
     *
     * @return The latest REST API version supported by this client library.
     */
    public static TableServiceVersion getLatest() {
        return V2019_02_02;
    }

    static TableServiceVersion fromString(String version) {
        for (TableServiceVersion value : TableServiceVersion.values()) {
            if (value.version.equals(version)) {
                return value;
            }
        }
        return null;
    }
}
