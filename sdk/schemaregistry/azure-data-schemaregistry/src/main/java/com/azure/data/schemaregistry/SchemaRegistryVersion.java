// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.util.ServiceVersion;

/**
 * Schema registry service version.
 */
public enum SchemaRegistryVersion implements ServiceVersion {
    /**
     * 2021 version.
     */
    V2021_10("2021-10");

    private final String version;

    SchemaRegistryVersion(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return version;
    }

    /**
     * Gets the latest version.
     *
     * @return The latest version.
     */
    public static SchemaRegistryVersion getLatest() {
        return V2021_10;
    }
}
