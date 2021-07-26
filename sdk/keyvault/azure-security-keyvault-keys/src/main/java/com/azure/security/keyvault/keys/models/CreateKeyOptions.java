// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.annotation.Fluent;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Represents the configurable options to create a key.
 */
@Fluent
public class CreateKeyOptions {
    /**
     * The key name.
     */
    private final String name;

    /**
     * The type of the key.
     */
    private KeyType keyType;

    /**
     * The key operations.
     */
    private List<KeyOperation> keyOperations;

    /**
     * Not before date in UTC.
     */
    private OffsetDateTime notBefore;

    /**
     * Expiry date in UTC.
     */
    private OffsetDateTime expiresOn;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    private Map<String, String> tags;

    /**
     * Determines whether the object is enabled.
     */
    private Boolean enabled;

    /*
     * Indicates if the private key can be exported.
     */
    private Boolean exportable;

    /**
     * The number of days a key is retained before being deleted for a soft delete-enabled Key Vault.
     */
    private Integer recoverableDays;

    /**
     * Reflects the deletion recovery level currently in effect for keys in the current vault. If it contains
     * 'Purgeable', the key can be permanently deleted by a privileged user; otherwise, only the system can purge the
     * key, at the end of the retention interval. Possible values include: 'Purgeable', 'Recoverable+Purgeable',
     * 'Recoverable', 'Recoverable+ProtectedSubscription'.
     */
    private String recoveryLevel;

    /*
     * The policy rules under which the key can be exported.
     */
    private KeyReleasePolicy releasePolicy;

    /**
     * Creates instance of {@link CreateKeyOptions} with {@code name} as key name and {@code keyType} as type of the
     * key.
     *
     * @param name The name of the key to create.
     * @param keyType The type of the key to create.
     */
    public CreateKeyOptions(String name, KeyType keyType) {
        this.name = name;
        this.keyType = keyType;
    }

    /**
     * Get the key operations.
     *
     * @return The key operations.
     */
    public List<KeyOperation> getKeyOperations() {
        return this.keyOperations;
    }

    /**
     * Set the key operations.
     *
     * @param keyOperations The key operations to set.
     *
     * @return The updated {@link CreateKeyOptions} object.
     */
    public CreateKeyOptions setKeyOperations(KeyOperation... keyOperations) {
        this.keyOperations = Arrays.asList(keyOperations);

        return this;
    }

    /**
     * Get the key type.
     *
     * @return The key type.
     */
    public KeyType getKeyType() {
        return this.keyType;
    }

    void setKeyType(KeyType keyType) {
        this.keyType = keyType;
    }

    /**
     * Get the {@link OffsetDateTime key's notBefore time} in UTC.
     *
     * @return The {@link OffsetDateTime key's notBefore time} in UTC.
     */
    public OffsetDateTime getNotBefore() {
        return notBefore;
    }

    /**
     * Set the {@link OffsetDateTime key's notBefore time} in UTC.
     *
     * @param notBefore The {@link OffsetDateTime key's notBefore time} in UTC.
     *
     * @return The updated {@link CreateKeyOptions} object.
     */
    public CreateKeyOptions setNotBefore(OffsetDateTime notBefore) {
        this.notBefore = notBefore;

        return this;
    }

    /**
     * Get the {@link OffsetDateTime key expiration time} in UTC.
     *
     * @return The {@link OffsetDateTime key expiration time} in UTC.
     */
    public OffsetDateTime getExpiresOn() {
        return this.expiresOn;
    }

    /**
     * Set the {@link OffsetDateTime key expiration time} in UTC.
     *
     * @param expiresOn The {@link OffsetDateTime key expiration time} in UTC.
     *
     * @return The updated {@link CreateKeyOptions} object.
     */
    public CreateKeyOptions setExpiresOn(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;

        return this;
    }

    /**
     * Get the tags associated with the key.
     *
     * @return The tag names and values.
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Set the tags to be associated with the key.
     *
     * @param tags The tags to set.
     *
     * @return The updated {@link CreateKeyOptions} object.
     */
    public CreateKeyOptions setTags(Map<String, String> tags) {
        this.tags = tags;

        return this;
    }

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
     * @return The updated {@link CreateKeyOptions} object.
     */
    public CreateKeyOptions setEnabled(Boolean enabled) {
        this.enabled = enabled;

        return this;
    }

    /**
     * Get the key name.
     *
     * @return The name of the key.
     */
    public String getName() {
        return this.name;
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
     * @return The updated {@link CreateKeyOptions} object.
     */
    public CreateKeyOptions setExportable(Boolean exportable) {
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
     * @return The updated {@link CreateKeyOptions} object.
     */
    public CreateKeyOptions setRecoverableDays(Integer recoverableDays) {
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
     * @return The updated {@link CreateKeyOptions} object.
     */
    public CreateKeyOptions setRecoveryLevel(String recoveryLevel) {
        this.recoveryLevel = recoveryLevel;

        return this;
    }

    /**
     * Get the policy rules under which the key can be exported.
     *
     * @return The policy rules under which the key can be exported.
     */
    public KeyReleasePolicy getReleasePolicy() {
        return this.releasePolicy;
    }

    /**
     * Set the policy rules under which the key can be exported.
     *
     * @param releasePolicy The policy rules to set.
     *
     * @return The updated {@link CreateKeyOptions} object.
     */
    public CreateKeyOptions setReleasePolicy(KeyReleasePolicy releasePolicy) {
        this.releasePolicy = releasePolicy;

        return this;
    }
}
