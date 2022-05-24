// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.implementation;

import com.azure.core.annotation.Fluent;
import com.azure.security.keyvault.keys.models.CreateKeyOptions;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * Key request attributes.
 */
@Fluent
public final class KeyRequestAttributes {
    /**
     * Creates an instance of {@link KeyRequestAttributes}. Reads {@link KeyProperties#getNotBefore()},
     * {@link KeyProperties#getExpiresOn()} and {@link KeyProperties#isEnabled()} fields from {@link KeyProperties}.
     *
     * @param keyProperties The {@link KeyProperties} object with populated attributes.
     */
    public KeyRequestAttributes(KeyProperties keyProperties) {
        if (keyProperties.getNotBefore() != null) {
            this.notBefore = keyProperties.getNotBefore().toEpochSecond();
        }

        if (keyProperties.getExpiresOn() != null) {
            this.expires = keyProperties.getExpiresOn().toEpochSecond();
        }

        this.enabled = keyProperties.isEnabled();
        this.exportable = keyProperties.isExportable();
    }

    /**
     * Creates an instance of {@link KeyRequestAttributes}. Reads {@link CreateKeyOptions#getNotBefore()},
     * {@link CreateKeyOptions#getExpiresOn()} and {@link CreateKeyOptions#isEnabled()} fields
     * from {@link CreateKeyOptions}.
     *
     * @param keyOptions The {@link CreateKeyOptions} object with populated attributes.
     */
    public KeyRequestAttributes(CreateKeyOptions keyOptions) {
        if (keyOptions.getNotBefore() != null) {
            this.notBefore = keyOptions.getNotBefore().toEpochSecond();
        }

        if (keyOptions.getExpiresOn() != null) {
            this.expires = keyOptions.getExpiresOn().toEpochSecond();
        }

        this.enabled = keyOptions.isEnabled();
        this.exportable = keyOptions.isExportable();
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
     * The number of days a key is retained before being deleted for a soft delete-enabled Key Vault.
     */
    @JsonProperty(value = "recoverableDays", access = JsonProperty.Access.WRITE_ONLY)
    private Integer recoverableDays;

    /**
     * Reflects the deletion recovery level currently in effect for keys in the current vault. If it contains
     * 'Purgeable', the key can be permanently deleted by a privileged user; otherwise, only the system can purge the
     * key, at the end of the retention interval. Possible values include: 'Purgeable', 'Recoverable+Purgeable',
     * 'Recoverable', 'Recoverable+ProtectedSubscription'.
     */
    @JsonProperty(value = "recoveryLevel", access = JsonProperty.Access.WRITE_ONLY)
    private String recoveryLevel;

    /*
     * Indicates if the private key can be exported.
     */
    @JsonProperty(value = "exportable")
    private Boolean exportable;

    /**
     * Get the enabled value.
     *
     * @return The enabled value.
     */
    public Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Set a value that indicates if the key is enabled.
     *
     * @param enabled The enabled value to set.
     *
     * @return The updated {@link KeyRequestAttributes} object.
     */
    public KeyRequestAttributes setEnabled(Boolean enabled) {
        this.enabled = enabled;

        return this;
    }

    /**
     * Get the {@link OffsetDateTime key's notBefore time} in UTC.
     *
     * @return The {@link OffsetDateTime key's notBefore time} in UTC.
     */
    public OffsetDateTime getNotBefore() {
        if (this.notBefore == null) {
            return null;
        }

        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.notBefore * 1000L), ZoneOffset.UTC);
    }

    /**
     * Set the {@link OffsetDateTime key's notBefore time} in UTC.
     *
     * @param notBefore The {@link OffsetDateTime key's notBefore time} in UTC.
     *
     * @return The updated {@link KeyRequestAttributes} object.
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
     * Get the {@link OffsetDateTime key expiration time} in UTC.
     *
     * @return The {@link OffsetDateTime key expiration time} in UTC.
     */
    public OffsetDateTime getExpires() {
        if (this.expires == null) {
            return null;
        }

        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.expires * 1000L), ZoneOffset.UTC);
    }

    /**
     * Set the {@link OffsetDateTime key expiration time} in UTC.
     *
     * @param expires The {@link OffsetDateTime key expiration time} in UTC.
     *
     * @return The updated {@link CreateKeyOptions} object.
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
     * Get the {@link OffsetDateTime time at which key was created} in UTC.
     *
     * @return The {@link OffsetDateTime time at which key was created} in UTC.
     */
    public OffsetDateTime getCreated() {
        if (this.created == null) {
            return null;
        }

        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.created * 1000L), ZoneOffset.UTC);
    }

    /**
     * Get the {@link OffsetDateTime time at which key was last updated} in UTC.
     *
     * @return The {@link OffsetDateTime time at which key was last updated} in UTC.
     */
    public OffsetDateTime getUpdated() {
        if (this.updated == null) {
            return null;
        }

        return OffsetDateTime.ofInstant(Instant.ofEpochMilli(this.updated * 1000L), ZoneOffset.UTC);
    }

    /**
     * Get a flag that indicates if the private key can be exported.
     *
     * @return A flag that indicates if the private key can be exported.
     */
    public Boolean isExportable() {
        return this.exportable;
    }

    /**
     * Set a flag that indicates if the private key can be exported.
     *
     * @param exportable A flag that indicates if the private key can be exported.
     *
     * @return The updated {@link KeyRequestAttributes} object.
     */
    public KeyRequestAttributes setExportable(Boolean exportable) {
        this.exportable = exportable;

        return this;
    }

    /**
     * Get the number of days a key is retained before being deleted for a soft delete-enabled Key Vault.
     *
     * @return The recoverable days.
     */
    public Integer getRecoverableDays() {
        return recoverableDays;
    }

    /**
     * Sets the number of days a key is retained before being deleted for a soft delete-enabled Key Vault.
     *
     * @param recoverableDays The recoverable days.
     *
     * @return The updated {@link KeyRequestAttributes} object.
     */
    public KeyRequestAttributes setRecoverableDays(Integer recoverableDays) {
        this.recoverableDays = recoverableDays;

        return this;
    }

    /**
     * Get the key recovery level.
     *
     * @return The key recovery level.
     */
    public String getRecoveryLevel() {
        return this.recoveryLevel;
    }

    /**
     * Get the key recovery level.
     *
     * @param recoveryLevel The key recovery level.
     *
     * @return The updated {@link KeyRequestAttributes} object.
     */
    public KeyRequestAttributes setRecoveryLevel(String recoveryLevel) {
        this.recoveryLevel = recoveryLevel;

        return this;
    }
}
