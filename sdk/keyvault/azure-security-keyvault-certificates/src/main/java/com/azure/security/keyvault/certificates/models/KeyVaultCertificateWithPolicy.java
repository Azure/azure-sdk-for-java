// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.implementation.util.ImplUtils;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Represents a certificate with all of its properties including {@link CertificatePolicy}.
 */
public class KeyVaultCertificateWithPolicy extends KeyVaultCertificate {

    /**
     * The Certificate policy.
     */
    @JsonProperty("policy")
    private CertificatePolicy certificatePolicy;

    /**
     * Create the certificate
     * @param name the name of the certificate.
     */
    public KeyVaultCertificateWithPolicy(String name) {
        super(name);
    }

    KeyVaultCertificateWithPolicy() {
        super();
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
     * @return the updated certificateWithPolicy object itself.
     */
    public KeyVaultCertificateWithPolicy setProperties(CertificateProperties properties) {
        super.setProperties(properties);
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
     * @return the key Id.
     */
    public String getSecretId() {
        return this.secretId;
    }

    /**
     * Get the cer content of the certificate
     * @return the cer content.
     */
    public byte[] getCer() {
        return ImplUtils.clone(cer);
    }

    /**
     * Get the certificate policy of the certificate
     * @return the cer content.
     */
    public CertificatePolicy getCertificatePolicy() {
        return this.certificatePolicy;
    }

    /**
     * Set the certificate policy of the certificate
     *
     * @param certificatePolicy the policy to set.
     * @return the certificateWithPolicy object itself.
     */
    public KeyVaultCertificateWithPolicy setCertificatePolicy(CertificatePolicy certificatePolicy) {
        this.certificatePolicy = certificatePolicy;
        return this;
    }
}
