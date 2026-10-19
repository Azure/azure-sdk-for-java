// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.core.util.ServiceVersion;

/**
 * Versions of CallingServer service supported by this client library.
 */
public enum CallingServerServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 2022-04-07-preview}.
     */
    V2022_04_07_PREVIEW("2022-04-07-preview"),
    /**
     * Service version {@code 2021-11-15-preview}.
     */
    V2021_11_15_PREVIEW("2021-11-15-preview");

    private final String version;

    CallingServerServiceVersion(String version) {
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
     * @return The latest {@link CallingServerServiceVersion} object.
     */
    public static CallingServerServiceVersion getLatest() {
        return V2022_04_07_PREVIEW;
    }
}
