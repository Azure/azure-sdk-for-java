// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.util.CoreUtils;

import java.util.Objects;

/**
 * Represents a certificate with all of its properties.
 */
public class KeyVaultCertificate {

    /**
     * CER contents of x509 certificate.
     */
    private final byte[] cer;

    /**
     * The key id.
     */
    private final String keyId;

    /**
     * The secret id.
     */
    private final String secretId;

    /**
     * The certificate properties
     */
    private CertificateProperties properties;

    KeyVaultCertificate() {
        this(null, null, null, new CertificateProperties());
    }

    KeyVaultCertificate(byte[] cer, String keyId, String secretId, CertificateProperties properties) {
        this.cer = CoreUtils.clone(cer);
        this.keyId = keyId;
        this.secretId = secretId;
        this.properties = properties;
    }

    /**
     * Get the certificate properties.
     * @return the certificate properties.
     */
    public CertificateProperties getProperties() {
        return properties;
    }

    /**
     * Set the certificate properties
     * @param properties the certificate properties
     * @throws NullPointerException if {@code certificateProperties} is null
     * @return the updated certificate object itself.
     */
    public KeyVaultCertificate setProperties(CertificateProperties properties) {
        Objects.requireNonNull(properties, "The certificate properties cannot be null");
        properties.setName(this.properties.getName());
        this.properties = properties;
        return this;
    }

    /**
     * Get the certificate identifier
     * @return the certificate identifier
     */
    public String getId() {
        return properties.getId();
    }

    /**
     * Get the certificate name
     * @return the certificate name
     */
    public String getName() {
        return properties.getName();
    }

    /**
     * Get the key id of the certificate
     * @return the key Id.
     */
    public String getKeyId() {
        return this.keyId;
    }

    /**
     * Get the secret id of the certificate
     * @return the secret Id.
     */
    public String getSecretId() {
        return this.secretId;
    }

    /**
     * Get the cer content of the certificate
     * @return the cer content.
     */
    public byte[] getCer() {
        return CoreUtils.clone(cer);
    }
}
