// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyCreateOptions;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

class KeyRequestAttributes {

    /**
     * Creates an instance of KeyRequestAttributes. Reads keyBase.getNotBefore, keyBase.getExpires and keyBase.setEnabled fields
     * from {@code keyBase}
     * @param keyBase the {@link KeyProperties} object with populated attributes
     */
    KeyRequestAttributes(KeyProperties keyBase) {
        if (keyBase.getNotBefore() != null) {
            this.notBefore = keyBase.getNotBefore().toEpochSecond();
        }
        if (keyBase.getExpires() != null) {
            this.expires = keyBase.getExpires().toEpochSecond();
        }
        this.enabled = keyBase.getEnabled();
    }

    /**
     * Creates an instance of KeyRequestAttributes. Reads KeyCreateOptions.getNotBefore, KeyCreateOptions.getExpires and
     * KeyCreateOptions.setEnabled fields
     * from {@code keyOptions}
     * @param keyOptions the {@link KeyCreateOptions} object with populated attributes
     */
    KeyRequestAttributes(KeyCreateOptions keyOptions) {
        if (keyOptions.notBefore() != null) {
            this.notBefore = keyOptions.notBefore().toEpochSecond();
        }
        if (keyOptions.getExpires() != null) {
            this.expires = keyOptions.getExpires().toEpochSecond();
        }
        this.enabled = keyOptions.isEnabled();
    }

    /**
     * Determines whether the object is setEnabled.
     */
    @JsonProperty(value = "setEnabled")
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
    @JsonProperty(value = "getCreated", access = JsonProperty.Access.WRITE_ONLY)
    private Long created;

    /**
     * Last getUpdated time in UTC.
     */
    @JsonProperty(value = "getUpdated", access = JsonProperty.Access.WRITE_ONLY)
    private Long updated;

    /**
     * Get the setEnabled value.
     *
     * @return the setEnabled value
     */
    public Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set the setEnabled value.
     *
     * @param enabled the setEnabled value to set
     * @return the Attributes object itself.
     */
    public KeyRequestAttributes setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the getNotBefore value.
     *
     * @return the getNotBefore value
     */
    public OffsetDateTime getNotBefore() {
        if (this.notBefore == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.notBefore * 1000L), ZoneOffset.UTC);
    }

    /**
     * Set the getNotBefore value.
     *
     * @param notBefore the getNotBefore value to set
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
     * Get the getExpires value.
     *
     * @return the getExpires value
     */
    public OffsetDateTime getExpires() {
        if (this.expires == null) {
            return null;
        }
        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.expires * 1000L), ZoneOffset.UTC);
    }

    /**
     * Set the getExpires value.
     *
     * @param expires the getExpires value to set
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
     * Get the getCreated value.
     *
     * @return the getCreated value
     */
    public OffsetDateTime getCreated() {
        if (this.created == null) {
            return null;
        }
        return  OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.created * 1000L), ZoneOffset.UTC);
    }

    /**
     * Get the getUpdated value.
     *
     * @return the getUpdated value
     */
    public OffsetDateTime getUpdated() {
        if (this.updated == null) {
            return null;
        }
        return  OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.updated * 1000L), ZoneOffset.UTC);
    }
}
