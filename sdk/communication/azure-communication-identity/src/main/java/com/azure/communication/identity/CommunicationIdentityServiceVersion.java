// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.identity;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Communication Identity Service supported by this client library.
 */
public enum CommunicationIdentityServiceVersion implements ServiceVersion {
    V2021_03_07("2021-03-07");

    private final String version;

    CommunicationIdentityServiceVersion(String version) {

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
     * @return the latest {@link CommunicationIdentityServiceVersion}
     */
    public static CommunicationIdentityServiceVersion getLatest() {

        return V2021_03_07;
    }
}
