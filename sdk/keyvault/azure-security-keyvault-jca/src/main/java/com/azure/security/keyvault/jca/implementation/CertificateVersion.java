// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.jca.implementation;

/**
 * Immutable references and metadata resolved from a single Azure Key Vault certificate version.
 *
 * <p>The certificate data, secret ID, and key ID originate from the same certificate bundle response. Keeping them
 * together ensures that certificate material loaded later remains pinned to that version. This object is a version
 * anchor; the certificate, chain, and key remain independently lazy-loaded.</p>
 */
public final class CertificateVersion {

    private final String alias;

    private final String certificateData;

    private final String keyId;

    private final String secretId;

    private final boolean exportable;

    private final String keyType;

    CertificateVersion(String alias, String certificateData, String keyId, String secretId, boolean exportable,
        String keyType) {
        this.alias = alias;
        this.certificateData = certificateData;
        this.keyId = keyId;
        this.secretId = secretId;
        this.exportable = exportable;
        this.keyType = keyType;
    }

    /**
     * Gets the certificate alias.
     *
     * @return The certificate alias.
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Gets the Base64-encoded DER certificate data.
     *
     * @return The Base64-encoded DER certificate data, or {@code null} when unavailable.
     */
    public String getCertificateData() {
        return certificateData;
    }

    /**
     * Gets the versioned Key Vault key ID.
     *
     * @return The versioned key ID, or {@code null} when unavailable.
     */
    public String getKeyId() {
        return keyId;
    }

    /**
     * Gets the versioned Key Vault secret ID.
     *
     * @return The versioned secret ID, or {@code null} when unavailable.
     */
    public String getSecretId() {
        return secretId;
    }

    /**
     * Indicates whether the private key can be exported through the certificate's secret.
     *
     * @return {@code true} if the private key is exportable; otherwise, {@code false}.
     */
    public boolean isExportable() {
        return exportable;
    }

    /**
     * Gets the Key Vault key type.
     *
     * @return The key type, or {@code null} when unavailable.
     */
    public String getKeyType() {
        return keyType;
    }
}
