// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.administration;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Communication Identity Service supported by this client library.
 */
public enum CommunicationIdentityServiceVersion implements ServiceVersion {
    V2020_09_21_PREVIEW_2("2020-07-20-preview2");

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

        return V2020_09_21_PREVIEW_2;
    }
}
