// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.core.util.ServiceVersion;

/**
 * Versions of CallingServer service supported by this client library.
 */
public enum CallAutomationServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 2023-01-15-preview}.
     */
    V2023_01_15_PREVIEW("2023-01-15-preview");

    private final String version;

    CallAutomationServiceVersion(String version) {
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
     * @return The latest {@link CallAutomationServiceVersion} object.
     */
    public static CallAutomationServiceVersion getLatest() {
        return V2023_01_15_PREVIEW;
    }
}
