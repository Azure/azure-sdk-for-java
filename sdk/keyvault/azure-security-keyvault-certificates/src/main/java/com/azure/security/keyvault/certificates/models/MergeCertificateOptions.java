// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import java.util.List;
import java.util.Map;

/**
 * Represents the configuration used to merge a certificate in the key vault.
 */
public final class MergeCertificateOptions {
    /**
     * The name of the certificate.
     */
    private final String name;

    /**
     * The certificate or the certificate chain to merge.
     */
    private final List<byte[]> x509Certificates;

    /**
     * Determines whether the certificate is enabled.
     */
    private boolean enabled;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    private Map<String, String> tags;

    /**
     * Creates a instance of {@link MergeCertificateOptions}.
     *
     * @param name The name of the certificate.
     * @param x509Certificates The certificate or the certificate chain to merge.
     */
    public MergeCertificateOptions(String name, List<byte[]> x509Certificates) {
        this.name = name;
        this.x509Certificates = x509Certificates;
    }

    /**
     * Get the name of the certificate.
     *
     * @return The name of the certificate.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the certificate or certificate chain to merge.
     *
     * @return The certificate or certificate chain to merge.
     */
    public List<byte[]> getX509Certificates() {
        return this.x509Certificates;
    }

    /**
     * Get a value indicating whether the certificate is enabled.
     *
     * @return The enabled status.
     */
    public Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set the enabled status.
     *
     * @param enabled The enabled status to set.
     * @return The updated {@link MergeCertificateOptions} object.
     */
    public MergeCertificateOptions setEnabled(Boolean enabled) {
        this.enabled = enabled;

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
     * Set the tags to be associated with the secret.
     *
     * @param tags The tags to set
     * @return The updated {@link MergeCertificateOptions} object.
     */
    public MergeCertificateOptions setTags(Map<String, String> tags) {
        this.tags = tags;

        return this;
    }
}
