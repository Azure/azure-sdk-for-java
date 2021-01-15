// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.administration;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Phone Number Admin Service supported by this client library.
 */
public enum PhoneNumberServiceVersion implements ServiceVersion {
    V2020_07_20_PREVIEW_1("2020-07-20-preview1");

    private final String version;

    PhoneNumberServiceVersion(String version) {

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
     * @return the latest {@link PhoneNumberServiceVersion}
     */
    public static PhoneNumberServiceVersion getLatest() {

        return V2020_07_20_PREVIEW_1;
    }
}
