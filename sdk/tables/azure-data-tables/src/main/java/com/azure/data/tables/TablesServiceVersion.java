// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Tables REST API supported by this client library.
 */
public enum TablesServiceVersion implements ServiceVersion {
    /**
     * API version 2019-02-02
     */
    V2019_02_02("2019-02-02");

    private final String version;

    TablesServiceVersion(String version) {
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
    public static TablesServiceVersion getLatest() {
        return V2019_02_02;
    }

    static TablesServiceVersion fromString(String version) {
        for (TablesServiceVersion value : TablesServiceVersion.values()) {
            if (value.version.equals(version)) {
                return value;
            }
        }
        return null;
    }
}
