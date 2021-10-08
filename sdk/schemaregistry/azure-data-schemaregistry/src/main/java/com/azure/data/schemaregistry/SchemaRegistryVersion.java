// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.schemaregistry;

import com.azure.core.util.ServiceVersion;

/**
 * Schema registry service version.
 */
public enum SchemaRegistryVersion implements ServiceVersion {
    /**
     * 2017 version
     */
    V2017_04("2017-04"),
    /**
     * 2020 version.
     */
    V2020_09_01_PREVIEW("2020-09-01-preview");

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
        return V2020_09_01_PREVIEW;
    }
}
