// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.implementation.util.ImplUtils;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a certificate with all of its properties.
 */
public class Certificate extends CertificateBase {

    /**
     * CER contents of x509 certificate.
     */
    @JsonProperty(value = "cer")
    private byte[] cer;

    /**
     * The key id.
     */
    @JsonProperty(value = "kid", access = JsonProperty.Access.WRITE_ONLY)
    private String keyId;

    /**
     * The secret id.
     */
    @JsonProperty(value = "sid", access = JsonProperty.Access.WRITE_ONLY)
    private String secretId;

    /**
     * The Certificate policy.
     */
    @JsonProperty("policy")
    private CertificatePolicy certificatePolicy;

    /**
     * Create the certificate
     * @param name the name of the certificate.
     */
    public Certificate(String name) {
        super.name = name;
    }

    Certificate() {

    }

    /**
     * Get the key id of the certificate
     * @return the key Id.
     */
    public String keyId() {
        return this.keyId;
    }

    /**
     * Get the secret id of the certificate
     * @return the key Id.
     */
    public String secretId() {
        return this.secretId;
    }

    /**
     * Get the cer content of the certificate
     * @return the cer content.
     */
    public byte[] cer() {
        return ImplUtils.clone(cer);
    }

    /**
     * Get the certificate policy of the certificate
     * @return the cer content.
     */
    public CertificatePolicy certificatePolicy() {
        return this.certificatePolicy;
    }

    /**
     * Set the certificate policy of the certificate
     *
     * @param certificatePolicy the policy to set.
     * @return the certificate object itself.
     */
    public Certificate certificatePolicy(CertificatePolicy certificatePolicy) {
        this.certificatePolicy = certificatePolicy;
        return this;
    }
}
