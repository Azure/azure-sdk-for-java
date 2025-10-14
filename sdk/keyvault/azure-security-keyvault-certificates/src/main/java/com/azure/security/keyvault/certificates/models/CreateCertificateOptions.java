// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.annotation.Fluent;

import java.util.Map;
import java.util.Objects;

/**
 * Represents the configuration used to create a certificate in the key vault.
 */
@Fluent
public class CreateCertificateOptions {
    /**
     * The name of the certificate.
     */
    private final String name;

    /**
     * The policy which governs the lifecycle of the certificate and its properties when it is rotated.
     */
    private final CertificatePolicy policy;

    /**
     * Determines whether the certificate is enabled.
     */
    private Boolean enabled;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    private Map<String, String> tags;

    /**
     * Determines whether the order of the certificates in the certificate chain is to be preserved.
     */
    private Boolean preserveCertificateOrder;

    /**
     * Creates an instance of {@link CreateCertificateOptions}.
     *
     * @param name The name of the certificate to be created.
     * @param policy The policy to be used for creating the certificate.
     * @throws NullPointerException if {@code name} or {@code policy} is null.
     */
    public CreateCertificateOptions(String name, CertificatePolicy policy) {
        Objects.requireNonNull(name, "'name' cannot be null.");
        Objects.requireNonNull(policy, "'policy' cannot be null.");

        this.name = name;
        this.policy = policy;
        this.enabled = true;
    }

    /**
     * Get the name of the certificate.
     *
     * @return The name of the certificate.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the management policy for the certificate.
     *
     * @return The management policy for the certificate.
     */
    public CertificatePolicy getPolicy() {
        return policy;
    }

    /**
     * Get the enabled status of the certificate.
     *
     * @return The enabled status.
     */
    public Boolean isEnabled() {
        return enabled;
    }

    /**
     * Set the enabled status of the certificate.
     *
     * @param enabled The enabled status.
     * @return The updated {@link CreateCertificateOptions} object.
     */
    public CreateCertificateOptions setEnabled(Boolean enabled) {
        this.enabled = enabled;

        return this;
    }

    /**
     * Get the tags associated with the certificate.
     *
     * @return The tags associated with the certificate.
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * Set the tags to be associated with the certificate.
     *
     * @param tags The tags to be associated with the certificate.
     * @return The updated {@link CreateCertificateOptions} object.
     */
    public CreateCertificateOptions setTags(Map<String, String> tags) {
        this.tags = tags;

        return this;
    }

    /**
     * Get a value indicating the certificate order in the vault is to be preserved. If true, the certificate chain
     * will be preserved in its original order. If false (default), the leaf certificate will be placed at index 0.
     *
     * @return A value indicating the certificate order in the vault is to be preserved.
     */
    public Boolean isCertificateOrderPreserved() {
        return preserveCertificateOrder;
    }

    /**
     * Set whether to preserve the certificate order in the vault. If true, the certificate chain
     * will be preserved in its original order. If false (default), the leaf certificate will be placed at index 0.
     *
     * @param preserveCertificateOrder Whether to preserve the certificate order in the vault.
     * @return The updated {@link CreateCertificateOptions} object.
     */
    public CreateCertificateOptions setCertificateOrderPreserved(Boolean preserveCertificateOrder) {
        this.preserveCertificateOrder = preserveCertificateOrder;

        return this;
    }
}
