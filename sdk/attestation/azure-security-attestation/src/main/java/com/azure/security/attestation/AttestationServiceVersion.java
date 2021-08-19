// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.attestation;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Metrics Advisor supported by this client library.
 */
public enum AttestationServiceVersion implements ServiceVersion {
    V2020_10_01("2020-10-01");

    private final String version;

    AttestationServiceVersion(String version) {
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
     * @return the latest {@link AttestationServiceVersion}
     */
    public static AttestationServiceVersion getLatest() {
        return V2020_10_01;
    }

}
