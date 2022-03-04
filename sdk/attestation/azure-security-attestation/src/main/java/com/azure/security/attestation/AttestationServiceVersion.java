// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.core.util.ServiceVersion;

/** Service version of AttestationClient. */
public enum AttestationServiceVersion implements ServiceVersion {
    /** Enum value 2020-10-01. */
    V2020_10_01("2020-10-01");

    private final String version;

    AttestationServiceVersion(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the latest service version supported by this client library.
     *
     * @return The latest {@link AttestationServiceVersion}.
     */
    public static AttestationServiceVersion getLatest() {
        return V2020_10_01;
    }
}
