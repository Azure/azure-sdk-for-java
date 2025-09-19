// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.sms;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Communication SMS Service supported by this client library.
 */
public enum SmsServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 2021-03-07}.
     */
    V2021_03_07("2021-03-07"),

    /**
     * Service version {@code 2025-08-01-preview}.
     */
    V2025_08_01_PREVIEW("2025-08-01-preview"),

    /**
     * Service version {@code 2026-01-23}.
     */
    V2026_01_23("2026-01-23");

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
        return V2025_08_01_PREVIEW;
    }
}
