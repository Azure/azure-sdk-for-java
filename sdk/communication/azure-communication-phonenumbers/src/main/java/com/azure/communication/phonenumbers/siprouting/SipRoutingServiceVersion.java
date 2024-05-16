// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.phonenumbers.siprouting;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of SIP Routing Service supported by this client library.
 */
public enum SipRoutingServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 2023-03-01}.
     */
    V2023_03_01("2023-03-01");

    private final String version;

    SipRoutingServiceVersion(String version) {
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
     * @return the latest {@link SipRoutingServiceVersion}
     */
    public static SipRoutingServiceVersion getLatest() {
        return V2023_03_01;
    }
}
