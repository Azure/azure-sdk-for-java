// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Communication Identity Service supported by this client library.
 */
public enum SmsServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 2024-01-14-preview}.
     */
    V2024_01_14_PREVIEW("2024-01-14-preview");

    private final String version;

    SmsServiceVersion(String version) {
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
     * @return the latest {@link SmsServiceVersion}
     */
    public static SmsServiceVersion getLatest() {
        return V2024_01_14_PREVIEW;
    }
}
