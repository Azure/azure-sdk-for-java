// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.keys.models;

import com.azure.keyvault.webkey.JsonWebKeyOperation;
import com.azure.keyvault.webkey.JsonWebKeyType;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public class RSAKeyCreateConfig extends KeyBase {
    private Integer keySize;
    private JsonWebKeyType keyType;

    public RSAKeyCreateConfig(String name, JsonWebKeyType keyType) {
        super.name = name;
        this.keyType = keyType;
    }

    /**
     * Get the keySize value.
     *
     * @return the keySize value
     */
    public Integer keySize() {
        return this.keySize;
    }

    /**
     * Set the keySize value.
     *
     * @param keySize The keySize value to set
     * @return the KeyRequestParameters object itself.
     */
    public RSAKeyCreateConfig keySize(Integer keySize) {
        this.keySize = keySize;
        return this;
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
     * @return the RSAKeyCreateConfig object itself.
     */
    @Override
    public RSAKeyCreateConfig keyOperations(List<JsonWebKeyOperation> keyOperations) {
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
     * @return the RSAKeyCreateConfig object itself.
     */
    @Override
    public RSAKeyCreateConfig notBefore(OffsetDateTime notBefore) {
        super.notBefore(notBefore);
        return this;
    }


    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expires The expiry time to set for the key.
     * @return the RSAKeyCreateConfig object itself.
     */
    @Override
    public RSAKeyCreateConfig expires(OffsetDateTime expires) {
        super.expires(expires);
        return this;
    }

    /**
     * Set the tags to be associated with the key.
     *
     * @param tags The tags to set
     * @return the RSAKeyCreateConfig object itself.
     */
    @Override
    public RSAKeyCreateConfig tags(Map<String, String> tags) {
        super.tags(tags);
        return this;
    }
}
