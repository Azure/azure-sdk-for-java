// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.security.keyvault.certificates.models.ImportCertificateOptions;
import com.azure.security.keyvault.certificates.models.CertificateProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * The object attributes managed by the Secret service.
 */
class CertificateRequestAttributes {

    /**
     * Creates an instance of SecretRequestAttributes. Reads certificateProperties.notBefore, certificateProperties.expires and certificateProperties.enabled fields
     * from {@code certificateProperties}
     * @param certificateProperties the {@link CertificateProperties} object with populated attributes
     */
    CertificateRequestAttributes(CertificateProperties certificateProperties) {
        if (certificateProperties.getNotBefore() != null) {
            this.notBefore = certificateProperties.getNotBefore().toEpochSecond();
        }
        if (certificateProperties.getExpiresOn() != null) {
            this.expires = certificateProperties.getExpiresOn().toEpochSecond();
        }
        this.enabled = certificateProperties.isEnabled();
    }

    CertificateRequestAttributes(ImportCertificateOptions importCertificateOptions) {
        this.enabled = importCertificateOptions.isEnabled();
    }

    CertificateRequestAttributes() {

    }

    /**
     * The secret value.
     */
    @JsonProperty(value = "value")
    private String value;

    /**
     * The secret id.
     */
    @JsonProperty(value = "id")
    private String id;

    /**
     * Determines whether the object is enabled.
     */
    @JsonProperty(value = "enabled")
    private Boolean enabled;

    /**
     * Not before date in UTC.
     */
    @JsonProperty(value = "nbf")
    private Long notBefore;

    /**
     * Expiry date in UTC.
     */
    @JsonProperty(value = "exp")
    private Long expires;

    /**
     * Creation time in UTC.
     */
    @JsonProperty(value = "created", access = JsonProperty.Access.WRITE_ONLY)
    private Long created;

    /**
     * Last updated time in UTC.
     */
    @JsonProperty(value = "updated", access = JsonProperty.Access.WRITE_ONLY)
    private Long updated;

    /**
     * Get the enabled value.
     *
     * @return the enabled value
     */
    public Boolean enabled() {
        return this.enabled;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled the enabled value to set
     * @return the Attributes object itself.
     */
    CertificateRequestAttributes enabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the notBefore value.
     *
     * @return the notBefore value
     */
    OffsetDateTime notBefore() {
        if (this.notBefore == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.notBefore * 1000L), ZoneOffset.UTC);
    }

    /**
     * Set the notBefore value.
     *
     * @param notBefore the notBefore value to set
     * @return the Attributes object itself.
     */
    CertificateRequestAttributes notBefore(OffsetDateTime notBefore) {
        if (notBefore == null) {
            this.notBefore = null;
        } else {
            this.notBefore = OffsetDateTime.ofInstant(notBefore.toInstant(), ZoneOffset.UTC).toEpochSecond();
        }
        return this;
    }

    /**
     * Get the expires value.
     *
     * @return the expires value
     */
    OffsetDateTime expires() {
        if (this.expires == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.expires * 1000L), ZoneOffset.UTC);
    }

    /**
     * Set the expires value.
     *
     * @param expires the expires value to set
     * @return the Attributes object itself.
     */
    CertificateRequestAttributes expires(OffsetDateTime expires) {
        if (expires == null) {
            this.expires = null;
        } else {
            this.expires = OffsetDateTime.ofInstant(expires.toInstant(), ZoneOffset.UTC).toEpochSecond();
        }
        return this;
    }

    /**
     * Get the created value.
     *
     * @return the created value
     */
    OffsetDateTime created() {
        if (this.created == null) {
            return null;
        }
        return  OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.created * 1000L), ZoneOffset.UTC);
    }

    /**
     * Get the updated value.
     *
     * @return the updated value
     */
    OffsetDateTime updated() {
        if (this.updated == null) {
            return null;
        }
        return  OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.updated * 1000L), ZoneOffset.UTC);
    }

}
