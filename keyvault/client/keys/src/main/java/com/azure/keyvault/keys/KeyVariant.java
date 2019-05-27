package com.azure.keyvault.keys;

import com.azure.keyvault.keys.models.KeyBase;
import com.azure.keyvault.webkey.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.codec.binary.Base64;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KeyVariant extends KeyBase {

    @JsonProperty(value = "key")
    private JsonWebKeyVariant keyMaterial;

    /**
     * Get the key value.
     *
     * @return the key value
     */
    public JsonWebKeyVariant keyMaterial() {
        return this.keyMaterial;
    }

    private List<JsonWebKeyOperation> getKeyOperations(List<String> jsonWebKeyOps){
        List<JsonWebKeyOperation> output = new ArrayList<>();
        for(String keyOp : jsonWebKeyOps){
            output.add(new JsonWebKeyOperation(keyOp));
        }
        return output;
    }

}