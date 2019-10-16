// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Key Vault Key supported by this client library.
 */
public enum KeyServiceVersion implements ServiceVersion {
    V7_0("7.0");

    private final String version;

    KeyServiceVersion(String version) {
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
     * @return the latest {@link KeyServiceVersion}
     */
    public static KeyServiceVersion getLatest() {
        return V7_0;
    }
}
