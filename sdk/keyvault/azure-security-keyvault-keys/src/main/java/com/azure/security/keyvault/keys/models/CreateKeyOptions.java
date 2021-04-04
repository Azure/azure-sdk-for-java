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

    /**
     * Indicates if the private key can be exported.
     */
    private Boolean exportable;

    /**
     * The policy rules under which the key can be exported.
     */
    private KeyReleasePolicy releasePolicy;

    /**
     * Creates instance of KeyCreateOptions with {@code name} as key name and {@code keyType} as type of the key.
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
     * @return The KeyCreateOptions object itself.
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
     * Get the notBefore UTC time.
     *
     * @return The notBefore UTC time.
     */
    public OffsetDateTime getNotBefore() {
        return notBefore;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The notBefore UTC time to set.
     *
     * @return The KeyCreateOptions object itself.
     */
    public CreateKeyOptions setNotBefore(OffsetDateTime notBefore) {
        this.notBefore = notBefore;
        return this;
    }

    /**
     * Get the Key Expiry time in UTC.
     *
     * @return The expires UTC time.
     */
    public OffsetDateTime getExpiresOn() {
        return this.expiresOn;
    }

    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expiresOn The expiry time to set for the key.
     *
     * @return The KeyCreateOptions object itself.
     */
    public CreateKeyOptions setExpiresOn(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;
        return this;
    }

    /**
     * Get the tags associated with the key.
     *
     * @return The value of the tags.
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Set the tags to be associated with the key.
     *
     * @param tags The tags to set.
     *
     * @return The KeyCreateOptions object itself.
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
     * @return The KeyCreateOptions object itself.
     */
    public CreateKeyOptions setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Indicates if the private key can be exported.
     *
     * @return The exportable value.
     */
    public Boolean isExportable() {
        return this.exportable;
    }

    /**
     * Set a value that indicates if the private key can be exported.
     *
     * @param exportable The exportable value to set.
     *
     * @return The updated {@link CreateKeyOptions} object.
     */
    public CreateKeyOptions setExportable(Boolean exportable) {
        this.exportable = exportable;
        return this;
    }

    /**
     * Get the policy rules under which the key can be exported.
     *
     * @return The release policy.
     */
    public KeyReleasePolicy getReleasePolicy() {
        return releasePolicy;
    }

    /**
     * Set the policy rules under which the key can be exported.
     *
     * @param releasePolicy The release policy to set.
     *
     * @return The updated {@link CreateKeyOptions} object.
     */
    public CreateKeyOptions setReleasePolicy(KeyReleasePolicy releasePolicy) {
        this.releasePolicy = releasePolicy;
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

}
