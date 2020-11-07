// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.administration;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Key Vault Administration service supported by this client library.
 */
public enum KeyVaultAdministrationServiceVersion implements ServiceVersion {
    V7_0("7.0"),
    V7_1("7.1"),
    V7_2_PREVIEW("7.2-preview");

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
        return V7_2_PREVIEW;
    }
}
