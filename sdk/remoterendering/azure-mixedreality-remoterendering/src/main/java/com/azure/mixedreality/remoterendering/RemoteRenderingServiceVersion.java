// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of the RemoteRenderingService supported by this client library.
 */
public enum RemoteRenderingServiceVersion implements ServiceVersion {
    V2021_01_01("2021-01-01");

    private final String version;

    RemoteRenderingServiceVersion(String version) {
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
     * Gets the latest service version supported by this client library.
     *
     * @return the latest {@link RemoteRenderingServiceVersion}
     */
    public static RemoteRenderingServiceVersion getLatest() {
        return V2021_01_01;
    }
}
