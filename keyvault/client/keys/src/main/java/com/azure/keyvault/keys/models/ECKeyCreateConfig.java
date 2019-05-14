package com.azure.keyvault.keys.models;

import com.azure.keyvault.keys.models.webkey.JsonWebKey;
import com.azure.keyvault.keys.models.webkey.JsonWebKeyCurveName;
import com.azure.keyvault.keys.models.webkey.JsonWebKeyOperation;
import com.azure.keyvault.keys.models.webkey.JsonWebKeyType;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ECKeyCreateConfig extends  KeyBase{
    private List<JsonWebKeyOperation> keyOperations;
    private JsonWebKeyCurveName curve;
    private JsonWebKeyType keyType;

    @JsonProperty(value = "key")
    private JsonWebKey key;

    public ECKeyCreateConfig(String name, JsonWebKeyType keyType) {
        super.name = name;
        this.keyType = keyType;
    }

    /**
     * Get the curve.
     *
     * @return the curve.
     */
    public JsonWebKeyCurveName curve() {
        return this.curve;
    }

    /**
     * Set the curve.
     *
     * @param curve The curve to set
     * @return the ECKeyCreateConfig object itself.
     */
    public ECKeyCreateConfig curve(JsonWebKeyCurveName curve) {
        this.curve = curve;
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
    public ECKeyCreateConfig keyOperations(List<JsonWebKeyOperation> keyOperations) {
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
