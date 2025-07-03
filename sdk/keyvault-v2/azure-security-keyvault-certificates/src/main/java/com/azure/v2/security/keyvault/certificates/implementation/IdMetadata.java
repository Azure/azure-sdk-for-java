// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.v2.security.keyvault.certificates.implementation;

/**
 * Model class containing metadata about a certificate identifier.
 * <p>
 * Creation of this class splits the ID into the sub parts vault URL, name, anc version.
 */
public final class IdMetadata {
    private final String id;
    private final String vaultUrl;
    private final String name;
    private final String version;

    IdMetadata(String id, String vaultUrl, String name, String version) {
        this.id = id;
        this.vaultUrl = vaultUrl;
        this.name = name;
        this.version = version;
    }

    /**
     * Get the full identifier of the certificate.
     *
     * @return the id of the certificate.
     */
    public String getId() {
        return id;
    }

    /**
     * Get the vault url of the certificate.
     *
     * @return the vault url.
     */
    public String getVaultUrl() {
        return vaultUrl;
    }

    /**
     * Get the name of the certificate.
     *
     * @return the name of the certificate.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the version of the certificate.
     *
     * @return the version of the certificate.
     */
    public String getVersion() {
        return version;
    }
}
