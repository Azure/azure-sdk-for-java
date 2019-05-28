// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.keys.models;

import com.azure.keyvault.webkey.JsonWebKeyOperation;
import com.azure.keyvault.webkey.JsonWebKeyType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class KeyCreateOptions extends KeyBase {
    JsonWebKeyType keyType;
    boolean hsm;

    KeyCreateOptions() {

    }

    /**
     * Creates instance of KeyCreateOptions with {@code name} as key name and {@code keyType} as type of the key.
     * @param name The name of the key to create.
     * @param keyType The type of the key to create.
     */
    public KeyCreateOptions(String name, JsonWebKeyType keyType) {
        super.name = name;
        this.keyType = JsonWebKeyType.EC;
    }

    /**
     * Get the key operations.
     *
     * @return the key operations.
     */
    public List<JsonWebKeyOperation> keyOperations() {
        return this.keyOperations;
    }

    /**
     * Set the key operations value.
     *
     * @param keyOperations The key operations value to set
     * @return the KeyCreateOptions object itself.
     */
    @Override
    public KeyCreateOptions keyOperations(List<JsonWebKeyOperation> keyOperations) {
        this.keyOperations = keyOperations;
        return this;
    }

    /**
     * Get the key type.
     *
     * @return the key type.
     */
    public JsonWebKeyType keyType() {
        return this.keyType;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The notBefore UTC time to set
     * @return the KeyCreateOptions object itself.
     */
    @Override
    public KeyCreateOptions notBefore(OffsetDateTime notBefore) {
        super.notBefore(notBefore);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expires The expiry time to set for the key.
     * @return the KeyCreateOptions object itself.
     */
    @Override
    public KeyCreateOptions expires(OffsetDateTime expires) {
        super.expires(expires);
        return this;
    }

    /**
     * Set the tags to be associated with the key.
     *
     * @param tags The tags to set
     * @return the KeyCreateOptions object itself.
     */
    @Override
    public KeyCreateOptions tags(Map<String, String> tags) {
        super.tags(tags);
        return this;
    }

    /**
     * Set the enabled value.
     *
     * @param enabled The enabled value to set
     * @return the KeyCreateOptions object itself.
     */
    public KeyCreateOptions enabled(Boolean enabled) {
        super.enabled(enabled);
        return this;
    }
}
