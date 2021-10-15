// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Key Vault Certificate supported by this client library.
 */
public enum CertificateServiceVersion implements ServiceVersion {
    V7_0("7.0"),
    V7_1("7.1"),
    V7_2("7.2"),
    V7_3_PREVIEW("7.3-preview");

    private final String version;

    CertificateServiceVersion(String version) {
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
     * @return the latest {@link CertificateServiceVersion}
     */
    public static CertificateServiceVersion getLatest() {
        return V7_3_PREVIEW;
    }
}
