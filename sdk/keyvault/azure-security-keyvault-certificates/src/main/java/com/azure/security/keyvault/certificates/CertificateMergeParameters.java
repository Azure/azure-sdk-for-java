// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * The certificate merge parameters.
 */
class CertificateMergeParameters {
    /**
     * The certificate or the certificate chain to merge.
     */
    @JsonProperty(value = "x5c", required = true)
    private List<byte[]> x509Certificates;

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
     * Get the x509Certificates value.
     *
     * @return the x509Certificates value
     */
    List<byte[]> x509Certificates() {
        return this.x509Certificates;
    }

    /**
     * Set the x509Certificates value.
     *
     * @param x509Certificates the x509Certificates value to set
     * @return the CertificateMergeParameters object itself.
     */
    CertificateMergeParameters x509Certificates(List<byte[]> x509Certificates) {
        this.x509Certificates = x509Certificates;
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
     * @return the CertificateMergeParameters object itself.
     */
    CertificateMergeParameters certificateAttributes(CertificateRequestAttributes certificateAttributes) {
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
     * @return the CertificateMergeParameters object itself.
     */
    CertificateMergeParameters tags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

}
