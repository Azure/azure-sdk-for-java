// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a certificate with all of its properties including {@link CertificatePolicy}.
 */
public class KeyVaultCertificateWithPolicy extends KeyVaultCertificate {

    /**
     * The Certificate policy.
     */
    @JsonProperty("policy")
    private CertificatePolicy policy;

    /**
     * Create the certificate
     * @param name the name of the certificate.
     */
    KeyVaultCertificateWithPolicy(String name) {
        super(name);
    }

    KeyVaultCertificateWithPolicy() {
        super();
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
     * Get the certificate policy of the certificate
     * @return the cer content.
     */
    public CertificatePolicy getPolicy() {
        return this.policy;
    }

    /**
     * Set the certificate policy of the certificate
     *
     * @param certificatePolicy the policy to set.
     * @return the certificateWithPolicy object itself.
     */
    public KeyVaultCertificateWithPolicy setPolicy(CertificatePolicy certificatePolicy) {
        this.policy = certificatePolicy;
        return this;
    }
}
