// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.networktraversal;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Communication Relay Service supported by this client library.
 */
public enum CommunicationRelayServiceVersion implements ServiceVersion {
    V2021_08_13("2021_08_13");

    private final String version;

    CommunicationRelayServiceVersion(String version) {

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
     * @return the latest {@link CommunicationRelayServiceVersion}
     */
    public static CommunicationRelayServiceVersion getLatest() {

        return V2021_08_13;
    }
}
