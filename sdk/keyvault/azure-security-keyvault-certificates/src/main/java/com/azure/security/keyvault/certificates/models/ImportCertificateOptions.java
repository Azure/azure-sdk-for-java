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
     * The file location of the certificate.
     */
    private final byte[] certificate;

    /**
     * The name of the certificate.
     */
    private final String name;

    /**
     * If the private key in base64EncodedCertificate is encrypted, the password used for encryption.
     */
    private String password;

    /**
     * Determines whether the object is enabled.
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
     * Creates instance of {@link  ImportCertificateOptions}.
     *
     * @param name The name of the key.
     * @param certificate The PFX or PEM formatted value of the certificate containing both the x509 certificates and
     * the private key.
     */
    public ImportCertificateOptions(String name, byte[] certificate) {
        Objects.requireNonNull(certificate, "The certificate parameter cannot be null.");
        this.name = name;
        this.certificate = CoreUtils.clone(certificate);
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
     * Get a value indicating whether the certificate is enabled.
     *
     * @return The enabled status.
     */
    public Boolean isEnabled() {
        return this.enabled;
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
     * Set the application specific metadata.
     *
     * @param tags The metadata to set.
     * @return The updated {@link ImportCertificateOptions} object.
     */
    public ImportCertificateOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the tags associated with the secret.
     *
     * @return The value of the tags.
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Set the password for encrypting the certificate, if its encrypted.
     *
     * @param password The password used to encrypt the certificate.
     * @return The updated {@link ImportCertificateOptions} object.
     */
    public ImportCertificateOptions setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Get the password for encrypting the certificate, if its encrypted.
     * @return The password.
     */
    public String getPassword() {
        return this.password;
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
     * Set a value indicating whether the order of the certificates in the certificate chain is preserved.
     *
     * @param certificateOrderPreserved The certificate order preserved status to set.
     * @return The updated {@link ImportCertificateOptions} object.
     */
    public ImportCertificateOptions setCertificateOrderPreserved(Boolean certificateOrderPreserved) {
        this.certificateOrderPreserved = certificateOrderPreserved;

        return this;
    }

    /**
     * Get a value indicating whether the order of the certificates in the certificate chain is preserved.
     *
     * @return The certificate order preserved status.
     */
    public Boolean isCertificateOrderPreserved() {
        return this.certificateOrderPreserved;
    }
}
