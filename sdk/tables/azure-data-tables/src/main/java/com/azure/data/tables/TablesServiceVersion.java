// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.tables;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Storage Tables supported by this client library.
 */
public enum TablesServiceVersion implements ServiceVersion {
    V2019_02_02("no yet implemented");
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
     * Gets the latest service version supported by this client library
     *
     * @return the latest TablesServiceVersion
     */
    public static TablesServiceVersion getLatest() {
        return V2019_02_02;
    }

}
