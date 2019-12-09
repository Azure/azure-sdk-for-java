// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import java.util.List;
import java.util.Map;

/**
 * Represents Merge Certificate Configuration to merge certificates in key vault.
 */
public final class MergeCertificateOptions {

    /**
     * The name of the certificate.
     */
    private final String certificateName;

    /**
     * The certificate or the certificate chain to merge.
     */
    private final List<byte[]> x509Certificates;

    /**
     * Determines whether the object is enabled.
     */
    private boolean enabled;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    private Map<String, String> tags;

    /**
     * Creates a new MergeCertificationOptions instance.
     *
     * @param certificateName The name of the certificate.
     * @param x509Certificates The certificate or the certificate chain to merge.
     */
    public MergeCertificateOptions(String certificateName, List<byte[]> x509Certificates) {
        this.certificateName = certificateName;
        this.x509Certificates = x509Certificates;
    }

    /**
     * Set the tags to be associated with the secret.
     *
     * @param tags The tags to set
     * @return the MergeCertificateOptions object itself.
     */
    public MergeCertificateOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the tags value.
     *
     * @return the tags value
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Set the enabled status.
     * @param enabled The enabled status to set.
     * @return the MergeCertificateOptions object itself.
     */
    public MergeCertificateOptions setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the enabled status.
     *
     * @return the enabled status
     */
    public Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Get the certificate name.
     *
     * @return the certificate name.
     */
    public String getName() {
        return this.certificateName;
    }

    /**
     * Get the certificate or certificate chain to merge.
     *
     * @return the x509 certficiates.
     */
    public List<byte[]> getX509Certificates() {
        return this.x509Certificates;
    }
}
