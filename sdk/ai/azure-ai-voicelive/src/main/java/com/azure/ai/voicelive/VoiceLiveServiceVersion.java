// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.core.util.ServiceVersion;

/**
 * Service version of VoiceLive.
 */
public enum VoiceLiveServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 2025-05-01-preview}.
     */
    V2025_05_01_PREVIEW("2025-05-01-preview"),

    /**
     * Service version {@code 2025-10-01}.
     */
    V2025_10_01("2025-10-01");

    private final String version;

    VoiceLiveServiceVersion(String version) {
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
     * @return The latest {@link VoiceLiveServiceVersion}.
     */
    public static VoiceLiveServiceVersion getLatest() {
        return V2025_10_01;
    }
}
