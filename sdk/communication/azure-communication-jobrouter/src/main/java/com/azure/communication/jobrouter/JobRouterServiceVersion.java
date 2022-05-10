// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Chat Service supported by this client library.
 */
public enum JobRouterServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 2021-04-07-preview1}.
     */
    V2021_04_07("2021-04-07-preview1"),

    /**
     * Service version {@code 2021-10-20-preview2}.
     */
    V2021_10_20("2021-10-20-preview2");

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

        return V2021_10_20;
    }
}
