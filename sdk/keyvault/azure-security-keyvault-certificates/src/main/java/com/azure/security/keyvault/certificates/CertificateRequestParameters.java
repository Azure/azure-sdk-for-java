// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * The certificate create parameters.
 */
class CertificateRequestParameters {
    /**
     * The management policy for the certificate.
     */
    @JsonProperty(value = "policy")
    private CertificatePolicyRequest certificatePolicyRequest;

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
     * Get the certificatePolicyRequest value.
     *
     * @return the certificatePolicyRequest value
     */
    CertificatePolicyRequest certificatePolicy() {
        return this.certificatePolicyRequest;
    }

    /**
     * Set the certificatePolicyRequest value.
     *
     * @param certificatePolicyRequest the certificatePolicyRequest value to set
     * @return the CertificateCreateParameters object itself.
     */
    CertificateRequestParameters certificatePolicy(CertificatePolicyRequest certificatePolicyRequest) {
        this.certificatePolicyRequest = certificatePolicyRequest;
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
     * @return the CertificateCreateParameters object itself.
     */
    CertificateRequestParameters certificateAttributes(CertificateRequestAttributes certificateAttributes) {
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
     * @return the CertificateCreateParameters object itself.
     */
    CertificateRequestParameters tags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }
}
