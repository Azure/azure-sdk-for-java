// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Chat Service supported by this client library.
 */
public enum JobRouterServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 2022-07-18-preview}.
     */
    V2022_07_18("2022-07-18-preview");

    private final String version;

    JobRouterServiceVersion(String version) {
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
     * @return the latest {@link JobRouterServiceVersion}
     */
    public static JobRouterServiceVersion getLatest() {

        return V2022_07_18;
    }
}
