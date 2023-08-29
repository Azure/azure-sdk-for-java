// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.util.Base64Url;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.certificates.implementation.CertificatePropertiesHelper;
import com.azure.security.keyvault.certificates.implementation.models.CertificateItem;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * Represents base properties of a certificate.
 */
public class CertificateProperties {
    private static final ClientLogger LOGGER = new ClientLogger(CertificateProperties.class);

    static {
        CertificatePropertiesHelper.setAccessor(CertificateProperties::new);
    }

    /**
     * URL for the Azure KeyVault service.
     */
    String vaultUrl;

    /**
     * The certificate version.
     */
    String version;

    /**
     * The Certificate name.
     */
    String name;

    /**
     * Determines whether the object is enabled.
     */
    Boolean enabled;

    /**
     * Not before date in UTC.
     */
    OffsetDateTime notBefore;

    /**
     * Expiry date in UTC.
     */
    OffsetDateTime expiresOn;

    /**
     * Creation time in UTC.
     */
    OffsetDateTime createdOn;

    /**
     * Last updated time in UTC.
     */
    OffsetDateTime updatedOn;

    /**
     * Reflects the deletion recovery level currently in effect for certificates in
     * the current vault. If it contains 'Purgeable', the certificate can be
     * permanently deleted by a privileged user; otherwise, only the system can
     * purge the certificate, at the end of the retention interval. Possible values
     * include: 'Purgeable', 'Recoverable+Purgeable', 'Recoverable',
     * 'Recoverable+ProtectedSubscription'.
     */
    String recoveryLevel;

    /**
     * The certificate id.
     */
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    String id;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    @JsonProperty(value = "tags")
    Map<String, String> tags;

    /**
     * Thumbprint of the certificate. Read Only
     */
    @JsonProperty(value = "x5t", access = JsonProperty.Access.WRITE_ONLY)
    Base64Url x509Thumbprint;

    /**
     * The number of days a certificate is retained before being deleted for a soft delete-enabled Key Vault.
     */
    @JsonProperty(value = "recoverableDays", access = JsonProperty.Access.WRITE_ONLY)
    Integer recoverableDays;

    CertificateProperties(String name) {
        this.name = name;
    }

    CertificateProperties() { }

    CertificateProperties(CertificateItem item) {
        unpackId(item.getId(), this);
        this.enabled = item.getAttributes().isEnabled();
        this.notBefore = item.getAttributes().getNotBefore();
        this.expiresOn = item.getAttributes().getExpires();
        this.createdOn = item.getAttributes().getCreated();
        this.updatedOn = item.getAttributes().getUpdated();
        this.recoveryLevel = Objects.toString(item.getAttributes().getRecoveryLevel(), null);
        this.tags = item.getTags();
        byte[] wireThumbprint = item.getX509Thumbprint();
        this.x509Thumbprint = (wireThumbprint == null || wireThumbprint.length == 0)
            ? null : new Base64Url(item.getX509Thumbprint());
        this.recoverableDays = item.getAttributes().getRecoverableDays();
    }

    /**
     * Get the certificate identifier.
     *
     * @return the certificate identifier
     */
    public String getId() {
        return this.id;
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
     * Gets the number of days a key is retained before being deleted for a soft delete-enabled Key Vault.
     * @return the recoverable days.
     */
    public Integer getRecoverableDays() {
        return recoverableDays;
    }

    /**
     * Get the Certificate Expiry time in UTC.
     *
     * @return the expires UTC time.
     */
    public OffsetDateTime getExpiresOn() {
        return this.expiresOn;
    }

    /**
     * Get the the UTC time at which certificate was created.
     *
     * @return the created UTC time.
     */
    public OffsetDateTime getCreatedOn() {
        return createdOn;
    }

    /**
     * Get the UTC time at which certificate was last updated.
     *
     * @return the last updated UTC time.
     */
    public OffsetDateTime getUpdatedOn() {
        return updatedOn;
    }


    /**
     * Get the tags associated with the certificate.
     *
     * @return the value of the tags.
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Get the URL for the Azure KeyVault service.
     *
     * @return the value of the URL for the Azure KeyVault service.
     */
    public String getVaultUrl() {
        return this.vaultUrl;
    }

    /**
     * Set the tags to be associated with the certificate.
     *
     * @param tags The tags to set
     * @return the CertificateProperties object itself.
     */
    public CertificateProperties setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the version of the certificate.
     *
     * @return the version of the certificate.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Get the certificate name.
     *
     * @return the name of the certificate.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get the recovery level of the certificate.

     * @return the recoveryLevel of the certificate.
     */
    public String getRecoveryLevel() {
        return recoveryLevel;
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
     * Set the enabled status.
     * @param enabled The enabled status to set.
     * @return the CertificateProperties object itself.
     */
    public CertificateProperties setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     *  Get the X509 Thumbprint of the certificate.
     * @return the x509Thumbprint.
     */
    public byte[] getX509Thumbprint() {
        if (x509Thumbprint != null) {
            return this.x509Thumbprint.decodedBytes();
        }
        return null;
    }

    static void unpackId(String id, CertificateProperties properties) {
        if (id != null && id.length() > 0) {
            properties.id = id;
            try {
                URL url = new URL(id);
                String[] tokens = url.getPath().split("/");
                properties.vaultUrl = (tokens.length >= 2 ? tokens[1] : null);
                properties.name = (tokens.length >= 3 ? tokens[2] : null);
                properties.version = (tokens.length >= 4 ? tokens[3] : null);
            } catch (MalformedURLException e) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("The Azure Key Vault endpoint url is malformed.", e));
            }
        }
    }
}

