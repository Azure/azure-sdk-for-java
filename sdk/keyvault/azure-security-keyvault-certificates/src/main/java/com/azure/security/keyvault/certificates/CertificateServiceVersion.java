// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Key Vault Certificate supported by this client library.
 */
public enum CertificateServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 7.0}.
     */
    V7_0("7.0"),

    /**
     * Service version {@code 7.1}.
     */
    V7_1("7.1"),

    /**
     * Service version {@code 7.2}.
     */
    V7_2("7.2"),

    /**
     * Service version {@code 7.3}.
     */
    V7_3("7.3"),

    /**
     * Service version {@code 7.4}.
     */
    V7_4("7.4"),

    /**
     * Service version {@code 7.5}.
     */
    V7_5("7.5");

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
        return V7_5;
    }
}
