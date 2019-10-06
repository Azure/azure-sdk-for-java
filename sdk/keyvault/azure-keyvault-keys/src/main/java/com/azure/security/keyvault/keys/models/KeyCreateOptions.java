// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.security.keyvault.keys.models.webkey.KeyOperation;
import com.azure.security.keyvault.keys.models.webkey.KeyType;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class KeyCreateOptions {

    /**
     * Determines whether the object is setEnabled.
     */
    private Boolean enabled;

    /**
     * Not before date in UTC.
     */
    private OffsetDateTime notBefore;

    /**
     * Expiry date in UTC.
     */
    private OffsetDateTime expires;

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

    KeyCreateOptions() {

    }

    /**
     * Creates instance of KeyCreateOptions with {@code name} as key name and {@code keyType} as type of the key.
     * @param name The name of the key to create.
     * @param keyType The type of the key to create.
     */
    public KeyCreateOptions(String name, KeyType keyType) {
        this.name = name;
        this.keyType = keyType;
    }

    /**
     * Get the key operations.
     *
     * @return the key operations.
     */
    public List<KeyOperation> keyOperations() {
        return this.keyOperations;
    }

    /**
     * Set the key operations value.
     *
     * @param keyOperations The key operations value to set
     * @return the KeyCreateOptions object itself.
     */
    public KeyCreateOptions setKeyOperations(KeyOperation... keyOperations) {
        this.keyOperations = Arrays.asList(keyOperations);
        return this;
    }

    /**
     * Get the key type.
     *
     * @return the key type.
     */
    public KeyType keyType() {
        return this.keyType;
    }

    /**
     * Set the {@link OffsetDateTime getNotBefore} UTC time.
     *
     * @param notBefore The getNotBefore UTC time to set
     * @return the KeyCreateOptions object itself.
     */
    public KeyCreateOptions setNotBefore(OffsetDateTime notBefore) {
        this.notBefore = notBefore;
        return this;
    }

    /**
     * Get the getNotBefore UTC time.
     *
     * @return the getNotBefore UTC time.
     */
    public OffsetDateTime notBefore() {
        return notBefore;
    }

    /**
     * Set the {@link OffsetDateTime getExpires} UTC time.
     *
     * @param expires The expiry time to set for the key.
     * @return the KeyCreateOptions object itself.
     */
    public KeyCreateOptions setExpires(OffsetDateTime expires) {
        this.expires = expires;
        return this;
    }

    /**
     * Get the Key Expiry time in UTC.
     *
     * @return the getExpires UTC time.
     */
    public OffsetDateTime getExpires() {
        return this.expires;
    }

    /**
     * Set the getTags to be associated with the key.
     *
     * @param tags The getTags to set
     * @return the KeyCreateOptions object itself.
     */
    public KeyCreateOptions setTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the getTags associated with the key.
     *
     * @return the value of the getTags.
     */
    public Map<String, String> getTags() {
        return this.tags;
    }

    /**
     * Set the setEnabled value.
     *
     * @param enabled The setEnabled value to set
     * @return the KeyCreateOptions object itself.
     */
    public KeyCreateOptions setEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Get the setEnabled value.
     *
     * @return the setEnabled value
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
