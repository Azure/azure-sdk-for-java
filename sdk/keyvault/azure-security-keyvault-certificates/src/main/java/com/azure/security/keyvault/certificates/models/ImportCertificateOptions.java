// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.util.CoreUtils;

import java.util.Map;
import java.util.Objects;

/**
 * Represents the configuration used to import a certificate in the key vault.
 */
public final class ImportCertificateOptions {
    /**
     * The name of the certificate.
     */
    private final String name;

    /**
     * The file location of the certificate.
     */
    private final byte[] certificate;

    /**
     * If the private key in base64EncodedCertificate is encrypted, the password used for encryption.
     */
    private String password;

    /**
     * Determines whether the certificate is enabled.
     */
    private Boolean enabled;

    /**
     * The policy which governs the lifecycle of the imported certificate and it's properties when it is rotated.
     */
    private CertificatePolicy policy;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    private Map<String, String> tags;

    /**
     * Determines whether the order of the certificates in the certificate chain is to be preserved.
     */
    private Boolean certificateOrderPreserved;

    /**
     * Creates an instance of {@link ImportCertificateOptions}.
     *
     * @param name The name of the key.
     * @param certificate The PFX or PEM formatted value of the certificate containing both the x509 certificates and
     * the private key.
     */
    public ImportCertificateOptions(String name, byte[] certificate) {
        Objects.requireNonNull(certificate, "'certificate' cannot be null.");

        this.name = name;
        this.certificate = CoreUtils.clone(certificate);
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
     * Get the value of the certificate.
     *
     * @return The value of the certificate.
     */
    public byte[] getCertificate() {
        return CoreUtils.clone(this.certificate);
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
     * Set a value indicating whether the certificate is enabled.
     *
     * @param enabled The enabled status to set.
     * @return The updated {@link ImportCertificateOptions} object.
     */
    public ImportCertificateOptions setEnabled(Boolean enabled) {
        this.enabled = enabled;

        return this;
    }

    /**
     * Get the management policy for the certificate.
     *
     * @return The management policy.
     */
    public CertificatePolicy getPolicy() {
        return this.policy;
    }

    /**
     * Set the management policy for the certificate.
     *
     * @param policy the management policy for the certificate
     * @return The updated {@link ImportCertificateOptions} object.
     */
    public ImportCertificateOptions setPolicy(CertificatePolicy policy) {
        this.policy = policy;

        return this;
    }

    /**
     * Get the tags associated with the certificate.
     *
     * @return The tags associated with the certificate.
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Set the tags associated with the certificate.
     *
     * @param tags The tags to associate with the certificate.
     * @return The updated {@link ImportCertificateOptions} object.
     */
    public ImportCertificateOptions setTags(Map<String, String> tags) {
        this.tags = tags;

        return this;
    }

    /**
     * Get the password for decrypting the certificate, if it's encrypted.
     * @return The password.
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Set the password for decrypting the certificate, if it's encrypted.
     *
     * @param password The password used to encrypt the certificate.
     * @return The updated {@link ImportCertificateOptions} object.
     */
    public ImportCertificateOptions setPassword(String password) {
        this.password = password;

        return this;
    }

    /**
     * Get a value indicating the certificate order in the vault is to be preserved. If true, the certificate chain
     * will be preserved in its original order. If false (default), the leaf certificate will be placed at index 0.
     *
     * @return The certificate order preserved status.
     */
    public Boolean isCertificateOrderPreserved() {
        return this.certificateOrderPreserved;
    }

    /**
     * Set a value indicating whether the order of the certificates in the certificate chain is preserved.
     *
     * @param certificateOrderPreserved The certificate order preserved status to set.
     * @return The updated {@link ImportCertificateOptions} object.
     */
    public ImportCertificateOptions setCertificateOrderPreserved(Boolean certificateOrderPreserved) {
        this.certificateOrderPreserved = certificateOrderPreserved;

        return this;
    }
}
