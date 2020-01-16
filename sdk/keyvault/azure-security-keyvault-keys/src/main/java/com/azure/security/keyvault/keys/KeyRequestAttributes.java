// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

class KeyRequestAttributes {

    /**
     * Creates an instance of KeyRequestAttributes. Reads keyProperties.getNotBefore, keyProperties.getExpires and keyProperties.setEnabled fields
     * from {@code keyProperties}
     * @param keyProperties the {@link KeyProperties} object with populated attributes
     */
    KeyRequestAttributes(KeyProperties keyProperties) {
        if (keyProperties.getNotBefore() != null) {
            this.notBefore = keyProperties.getNotBefore().toEpochSecond();
        }
        if (keyProperties.getExpiresOn() != null) {
            this.expires = keyProperties.getExpiresOn().toEpochSecond();
        }
        this.enabled = keyProperties.isEnabled();
    }

    /**
     * Creates an instance of KeyRequestAttributes. Reads KeyCreateOptions.getNotBefore, KeyCreateOptions.getExpires and
     * KeyCreateOptions.isEnabled fields
     * from {@code keyOptions}
     * @param keyOptions the {@link CreateKeyOptions} object with populated attributes
     */
    KeyRequestAttributes(CreateKeyOptions keyOptions) {
        if (keyOptions.getNotBefore() != null) {
            this.notBefore = keyOptions.getNotBefore().toEpochSecond();
        }
        if (keyOptions.getExpiresOn() != null) {
            this.expires = keyOptions.getExpiresOn().toEpochSecond();
        }
        this.enabled = keyOptions.isEnabled();
    }

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
    public Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled the enabled value to set
     * @return the Attributes object itself.
     */
    public KeyRequestAttributes setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
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

    /**
     * Set the notBefore value.
     *
     * @param notBefore the notBefore value to set
     * @return the Attributes object itself.
     */
    public KeyRequestAttributes setNotBefore(OffsetDateTime notBefore) {
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
    public OffsetDateTime getExpires() {
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
    public KeyRequestAttributes setExpires(OffsetDateTime expires) {
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
    public OffsetDateTime getCreated() {
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
    public OffsetDateTime getUpdated() {
        if (this.updated == null) {
            return null;
        }
        return  OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.updated * 1000L), ZoneOffset.UTC);
    }
}
