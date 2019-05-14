package com.azure.keyvault.keys.models;

import com.azure.keyvault.keys.models.webkey.JsonWebKey;
import com.azure.keyvault.keys.models.webkey.JsonWebKeyCurveName;
import com.azure.keyvault.keys.models.webkey.JsonWebKeyOperation;
import com.azure.keyvault.keys.models.webkey.JsonWebKeyType;
import com.fasterxml.jackson.annotation.JsonProperty;


import java.util.List;

public class RSAKeyCreateConfig extends KeyBase {
    private Integer keySize;
    private List<JsonWebKeyOperation> keyOperations;
    private JsonWebKeyType keyType;

    @JsonProperty(value = "key")
    private JsonWebKey key;

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
}
