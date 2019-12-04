// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates.models;

import com.azure.core.util.Base64Url;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * Represents base properties of a certificate.
 */
public class CertificateProperties {

    /**
     * Determines whether the object is enabled.
     */
    private Boolean enabled;

    /**
     * Not before date in UTC.
     */
    private OffsetDateTime notBefore;

    /**
     * The certificate version.
     */
    String version;

    /**
     * Expiry date in UTC.
     */
    private OffsetDateTime expiresOn;

    /**
     * Creation time in UTC.
     */
    private OffsetDateTime createdOn;

    /**
     * Last updated time in UTC.
     */
    private OffsetDateTime updatedOn;

    /**
     * Reflects the deletion recovery level currently in effect for certificates in
     * the current vault. If it contains 'Purgeable', the certificate can be
     * permanently deleted by a privileged user; otherwise, only the system can
     * purge the certificate, at the end of the retention interval. Possible values
     * include: 'Purgeable', 'Recoverable+Purgeable', 'Recoverable',
     * 'Recoverable+ProtectedSubscription'.
     */
    private String recoveryLevel;

    /**
     * The Certificate name.
     */
    String name;

    /**
     * The certificate id.
     */
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    private String id;

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

    CertificateProperties(String name) {
        this.name = name;
    }

    CertificateProperties() { }

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

    @JsonProperty("attributes")
    @SuppressWarnings("unchecked")
    void unpackBaseAttributes(Map<String, Object> attributes) {
        this.enabled = (Boolean) attributes.get("enabled");
        this.notBefore =  epochToOffsetDateTime(attributes.get("nbf"));
        this.expiresOn =  epochToOffsetDateTime(attributes.get("exp"));
        this.createdOn = epochToOffsetDateTime(attributes.get("created"));
        this.updatedOn = epochToOffsetDateTime(attributes.get("updated"));
        this.recoveryLevel = (String) attributes.get("recoveryLevel");
        this.tags = (Map<String, String>) lazyValueSelection(attributes.get("tags"), this.tags);
        unpackId((String) attributes.get("id"));
    }

    private OffsetDateTime epochToOffsetDateTime(Object epochValue) {
        if (epochValue != null) {
            Instant instant = Instant.ofEpochMilli(((Number) epochValue).longValue() * 1000L);
            return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
        }
        return null;
    }

    @JsonProperty(value = "id")
    void unpackId(String id) {
        if (id != null && id.length() > 0) {
            this.id = id;
            try {
                URL url = new URL(id);
                String[] tokens = url.getPath().split("/");
                this.name = (tokens.length >= 3 ? tokens[2] : null);
                this.version = (tokens.length >= 4 ? tokens[3] : null);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }

    private Object lazyValueSelection(Object input1, Object input2) {
        if (input1 == null) {
            return input2;
        }
        return input1;
    }
}

