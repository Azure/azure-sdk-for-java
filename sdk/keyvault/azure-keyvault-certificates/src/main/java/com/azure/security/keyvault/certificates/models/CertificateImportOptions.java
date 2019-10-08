// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Represents the configuration used to import a certificate in the key vault.
 */
public final class CertificateImportOptions {

    /**
     * The file location of the certificate.
     */
    private final String filePath;

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
     * Not before date in UTC.
     */
    private OffsetDateTime notBefore;

    /**
     * Expiry date in UTC.
     */
    private OffsetDateTime expires;

    /**
     * The management policy for the certificate.
     */
    private CertificatePolicy certificatePolicy;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    private Map<String, String> tags;

    /**
     * Creates instance of CertificateImportOptions.
     * @param name The name of the key.
     * @param filePath The file location of the certificate.
     */
    public CertificateImportOptions(String name, String filePath) {
        this.name = name;
        this.filePath = filePath;
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
    public Boolean getEnabled() {
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
     * @return the CertificateImportOptions itself
     */
    public CertificateImportOptions setCertificatePolicy(CertificatePolicy certificatePolicy) {
        this.certificatePolicy = certificatePolicy;
        return this;
    }

    /**
     * Set the application specific maetadata.
     * @param tags The metadata to set.
     * @return the CertificateImportOptions itself
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
     * @return the CertificateImportOptions itself
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
     * Get the file path of the certificate.
     * @return the file path of the certificate.
     */
    public String getFilePath() {
        return this.filePath;
    }

    /**
     * Get the notBefore UTC time.
     *
     * @return the notBefore UTC time.
     */
    public OffsetDateTime getNotBefore() {
        return notBefore;
    }

    /**
     * Get the Certificate Expiry time in UTC.
     *
     * @return the expires UTC time.
     */
    public OffsetDateTime getExpires() {
        return this.expires;
    }

    /**
     * Set the notBefore UTC time.
     *
     * @param notBefore THe notBefore UTC time.
     * @return the notBefore UTC time.
     */
    public CertificateImportOptions setNotBefore(OffsetDateTime notBefore) {
        this.notBefore = notBefore;
        return this;
    }

    /**
     * Set the Certificate Expiry time in UTC.
     *
     * @param expires The expires UTC time.
     * @return the expires UTC time.
     */
    public CertificateImportOptions setExpires(OffsetDateTime expires) {
        this.expires = expires;
        return this;
    }
}
