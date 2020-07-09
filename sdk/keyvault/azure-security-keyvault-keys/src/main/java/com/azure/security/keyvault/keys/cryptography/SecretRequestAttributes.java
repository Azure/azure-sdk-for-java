// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/*
 * The object attributes managed by the Cryptography service.
 */
class SecretRequestAttributes {

    /*
     * Creates an instance of SecretRequestAttributes. Reads secretProperties.notBefore, secretProperties.expires and
     * secretProperties.enabled fields from {@code secretProperties}
     * @param secretProperties the {@link SecretProperties} object with populated attributes
     */
    SecretRequestAttributes(SecretProperties secretProperties) {
        if (secretProperties.getNotBefore() != null) {
            this.notBefore = secretProperties.getNotBefore().toEpochSecond();
        }
        if (secretProperties.getExpiresOn() != null) {
            this.expires = secretProperties.getExpiresOn().toEpochSecond();
        }
        this.enabled = secretProperties.isEnabled();
    }

    /*
     * The secret value.
     */
    @JsonProperty(value = "value")
    private String value;

    /*
     * The secret id.
     */
    @JsonProperty(value = "id")
    private String id;

    /*
     * Determines whether the object is enabled.
     */
    @JsonProperty(value = "enabled")
    private Boolean enabled;

    /*
     * Not before date in UTC.
     */
    @JsonProperty(value = "nbf")
    private Long notBefore;

    /*
     * Expiry date in UTC.
     */
    @JsonProperty(value = "exp")
    private Long expires;

    /*
     * Creation time in UTC.
     */
    @JsonProperty(value = "created", access = JsonProperty.Access.WRITE_ONLY)
    private Long created;

    /*
     * Last updated time in UTC.
     */
    @JsonProperty(value = "updated", access = JsonProperty.Access.WRITE_ONLY)
    private Long updated;

    /*
     * Get the enabled value.
     *
     * @return the enabled value
     */
    public Boolean isEnabled() {
        return this.enabled;
    }

    /*
     * Set the enabled value.
     *
     * @param enabled the enabled value to set
     * @return the Attributes object itself.
     */
    public SecretRequestAttributes getEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /*
     * Get the notBefore value.
     *
     * @return the notBefore value
     */
    public OffsetDateTime getNotBefore() {
        if (this.notBefore == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.notBefore * 1000L), ZoneOffset.UTC);
    }

    /*
     * Set the notBefore value.
     *
     * @param notBefore the notBefore value to set
     * @return the Attributes object itself.
     */
    public SecretRequestAttributes setNotBefore(OffsetDateTime notBefore) {
        if (notBefore == null) {
            this.notBefore = null;
        } else {
            this.notBefore = OffsetDateTime.ofInstant(notBefore.toInstant(), ZoneOffset.UTC).toEpochSecond();
        }
        return this;
    }

    /*
     * Get the expires value.
     *
     * @return the expires value
     */
    public OffsetDateTime getExpires() {
        if (this.expires == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.expires * 1000L), ZoneOffset.UTC);
    }

    /*
     * Set the expires value.
     *
     * @param expires the expires value to set
     * @return the Attributes object itself.
     */
    public SecretRequestAttributes setExpires(OffsetDateTime expires) {
        if (expires == null) {
            this.expires = null;
        } else {
            this.expires = OffsetDateTime.ofInstant(expires.toInstant(), ZoneOffset.UTC).toEpochSecond();
        }
        return this;
    }

    /*
     * Get the created value.
     *
     * @return the created value
     */
    public OffsetDateTime getCreated() {
        if (this.created == null) {
            return null;
        }
        return  OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.created * 1000L), ZoneOffset.UTC);
    }

    /*
     * Get the updated value.
     *
     * @return the updated value
     */
    public OffsetDateTime getUpdated() {
        if (this.updated == null) {
            return null;
        }
        return  OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.updated * 1000L), ZoneOffset.UTC);
    }
}
