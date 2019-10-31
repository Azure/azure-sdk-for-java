// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.util.GeneralUtils;

import java.util.Map;
import java.util.Objects;

/**
 * Represents the configuration used to import a certificate in the key vault.
 */
public final class CertificateImportOptions {

    /**
     * The file location of the certificate.
     */
    private final byte[] value;

    /**
     * The name of the certificate.
     */
    private final String name;

    /**
     * If the private key in base64EncodedCertificate is encrypted, the
     * password used for encryption.
     */
    private String password;

    /**
     * Determines whether the object is enabled.
     */
    private Boolean enabled;

    /**
     * The policy which governs the lifecycle of the imported certificate and it's properties when it is rotated.
     */
    private CertificatePolicy certificatePolicy;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    private Map<String, String> tags;

    /**
     * Creates instance of CertificateImportOptions.
     * @param name The name of the key.
     * @param value The PFX or PEM formatted value of the certificate containing both the x509 certificates and the private key.
     */
    public CertificateImportOptions(String name, byte[] value) {
        Objects.requireNonNull(value, "The certificate value parameter cannot be null.");
        this.name = name;
        this.value = GeneralUtils.clone(value);
    }

    /**
     * Set the enabled status.
     * @param enabled The enabled status to set.
     * @return the CertificateImportOptions itself
     */
    public CertificateImportOptions setEnabled(Boolean enabled) {
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
     * Get the management policy for the certificate.
     * @return the management policy
     */
    public CertificatePolicy getCertificatePolicy() {
        return this.certificatePolicy;
    }

    /**
     * Set the management policy for the certificate.
     * @param certificatePolicy the management policy for the certificate
     * @return the updated CertificateImportOptions itself
     */
    public CertificateImportOptions setCertificatePolicy(CertificatePolicy certificatePolicy) {
        this.certificatePolicy = certificatePolicy;
        return this;
    }

    /**
     * Set the application specific maetadata.
     * @param tags The metadata to set.
     * @return the updated CertificateImportOptions itself
     */
    public CertificateImportOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the tags associated with the secret.
     *
     * @return the value of the tags.
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Set the password for encrypting the certificate, if its encrypted.
     * @param password The password used to encrypt the certificate.
     * @return the updated CertificateImportOptions itself
     */
    public CertificateImportOptions setPassword(String password) {
        this.password = password;
        return this;
    }

    /**
     * Get the password for encrypting the certificate, if its encrypted.
     * @return the password
     */
    public String getPassword() {
        return this.password;
    }

    /**
     * Get the name of the certificate.
     * @return the name of the certificate.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the value of the certificate.
     * @return the value of the certificate.
     */
    public byte[] getValue() {
        return GeneralUtils.clone(this.value);
    }
}
