// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * The certificate import parameters.
 */
class CertificateImportParameters {
    /**
     * Base64 encoded representation of the certificate object to import. This
     * certificate needs to contain the private key.
     */
    @JsonProperty(value = "value", required = true)
    private String base64EncodedCertificate;

    /**
     * If the private key in base64EncodedCertificate is encrypted, the
     * password used for encryption.
     */
    @JsonProperty(value = "pwd")
    private String password;

    /**
     * The management policy for the certificate.
     */
    @JsonProperty(value = "policy")
    private CertificatePolicy certificatePolicy;

    /**
     * The attributes of the certificate (optional).
     */
    @JsonProperty(value = "attributes")
    private CertificateRequestAttributes certificateAttributes;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    @JsonProperty(value = "tags")
    private Map<String, String> tags;

    /**
     * Get the base64EncodedCertificate value.
     *
     * @return the base64EncodedCertificate value
     */
    String base64EncodedCertificate() {
        return this.base64EncodedCertificate;
    }

    /**
     * Set the base64EncodedCertificate value.
     *
     * @param base64EncodedCertificate the base64EncodedCertificate value to set
     * @return the CertificateImportParameters object itself.
     */
    CertificateImportParameters base64EncodedCertificate(String base64EncodedCertificate) {
        this.base64EncodedCertificate = base64EncodedCertificate;
        return this;
    }

    /**
     * Get the password value.
     *
     * @return the password value
     */
    String password() {
        return this.password;
    }

    /**
     * Set the password value.
     *
     * @param password the password value to set
     * @return the CertificateImportParameters object itself.
     */
    CertificateImportParameters password(String password) {
        this.password = password;
        return this;
    }

    /**
     * Get the certificatePolicy value.
     *
     * @return the certificatePolicy value
     */
    CertificatePolicy certificatePolicy() {
        return this.certificatePolicy;
    }

    /**
     * Set the certificatePolicy value.
     *
     * @param certificatePolicy the certificatePolicy value to set
     * @return the CertificateImportParameters object itself.
     */
    CertificateImportParameters certificatePolicy(CertificatePolicy certificatePolicy) {
        this.certificatePolicy = certificatePolicy;
        return this;
    }

    /**
     * Get the certificateAttributes value.
     *
     * @return the certificateAttributes value
     */
    CertificateRequestAttributes certificateAttributes() {
        return this.certificateAttributes;
    }

    /**
     * Set the certificateAttributes value.
     *
     * @param certificateAttributes the certificateAttributes value to set
     * @return the CertificateImportParameters object itself.
     */
    CertificateImportParameters certificateAttributes(CertificateRequestAttributes certificateAttributes) {
        this.certificateAttributes = certificateAttributes;
        return this;
    }

    /**
     * Get the tags value.
     *
     * @return the tags value
     */
    Map<String, String> tags() {
        return this.tags;
    }

    /**
     * Set the tags value.
     *
     * @param tags the tags value to set
     * @return the CertificateImportParameters object itself.
     */
    CertificateImportParameters tags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }
}
