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
    private OffsetDateTime expiresOn;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    private Map<String, String> tags;

    /**
     * The type of the key.
     */
    KeyType keyType;

    /**
     * The key operations.
     */
    List<KeyOperation> keyOperations;

    /**
     * The key name.
     */
    String name;

    CreateKeyOptions() { }

    /**
     * Creates instance of KeyCreateOptions with {@code name} as key name and {@code keyType} as type of the key.
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
     * @return the key operations.
     */
    public List<KeyOperation> getKeyOperations() {
        return this.keyOperations;
    }

    /**
     * Set the key operations value.
     *
     * @param keyOperations The key operations value to set
     * @return the KeyCreateOptions object itself.
     */
    public CreateKeyOptions setKeyOperations(KeyOperation... keyOperations) {
        this.keyOperations = Arrays.asList(keyOperations);
        return this;
    }

    /**
     * Get the key type.
     *
     * @return the key type.
     */
    public KeyType getKeyType() {
        return this.keyType;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The notBefore UTC time to set
     * @return the KeyCreateOptions object itself.
     */
    public CreateKeyOptions setNotBefore(OffsetDateTime notBefore) {
        this.notBefore = notBefore;
        return this;
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
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expiresOn The expiry time to set for the key.
     * @return the KeyCreateOptions object itself.
     */
    public CreateKeyOptions setExpiresOn(OffsetDateTime expiresOn) {
        this.expiresOn = expiresOn;
        return this;
    }

    /**
     * Get the Key Expiry time in UTC.
     *
     * @return the expires UTC time.
     */
    public OffsetDateTime getExpiresOn() {
        return this.expiresOn;
    }

    /**
     * Set the tags to be associated with the key.
     *
     * @param tags The tags to set
     * @return the KeyCreateOptions object itself.
     */
    public CreateKeyOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the tags associated with the key.
     *
     * @return the value of the tags.
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled The enabled value to set
     * @return the KeyCreateOptions object itself.
     */
    public CreateKeyOptions setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the enabled value.
     *
     * @return the enabled value
     */
    public Boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Get the key name.
     *
     * @return the name of the key.
     */
    public String getName() {
        return this.name;
    }

}
