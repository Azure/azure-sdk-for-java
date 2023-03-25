// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Communication Rooms Service supported by this client library.
 */
public enum RoomsServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 2021-04-07}
     */
    V2021_04_07("2021-04-07"),

    /**
     * Service version {@code 2022-02-01}
     */
    V2022_02_01("2022-02-01"),

    /**
     * Service version {@code 2023-03-31}
     */
    V2023_03_31("2023-03-31-preview");

    private final String version;

    RoomsServiceVersion(String version) {
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
     * @return the latest {@link RoomsServiceVersion}
     */
    public static RoomsServiceVersion getLatest() {
        return V2023_03_31;
    }
}
