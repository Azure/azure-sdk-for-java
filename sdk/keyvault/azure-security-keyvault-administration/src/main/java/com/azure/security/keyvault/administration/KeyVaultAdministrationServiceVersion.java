// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Key Vault Administration service supported by this client library.
 */
public enum KeyVaultAdministrationServiceVersion implements ServiceVersion {
    /**
     * Service version {@code 7.2}.
     */
    V7_2("7.2"),

    /**
     * Service version {@code 7.3}.
     */
    V7_3("7.3"),

    /**
     * Service version {@code 7.3}.
     */
    V7_4("7.4");

    private final String version;

    KeyVaultAdministrationServiceVersion(String version) {
        this.version = version;
    }

    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the latest service version supported by this client library.
     *
     * @return The latest {@link KeyVaultAdministrationServiceVersion}.
     */
    public static KeyVaultAdministrationServiceVersion getLatest() {
        return V7_4;
    }
}
