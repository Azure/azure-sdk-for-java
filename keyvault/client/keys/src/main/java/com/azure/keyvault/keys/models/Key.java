package com.azure.keyvault.keys.models;

import com.azure.keyvault.keys.models.webkey.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.codec.binary.Base64;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Key extends KeyBase {

    @JsonProperty(value = "key")
    private JsonWebKey keyMaterial;

    private List<JsonWebKeyOperation> keyOperations;

    /**
     * Get the key value.
     *
     * @return the key value
     */
    public JsonWebKey keyMaterial() {
        return this.keyMaterial;
    }

    /**
     * Get the key operations.
     *
     * @return the key operations
     */
    public List<JsonWebKeyOperation> keyOperations() {
        return this.keyOperations;
    }

    /**
     * Set the keyOps value.
     *
     * @param keyOperations The key operations to set.
     * @return the KeyRequestParameters object itself.
     */
    public Key keyOperations(List<JsonWebKeyOperation> keyOperations) {
        this.keyOperations = keyOperations;
        return this;
    }

    /**
     * Unpacks the attributes json response and updates the variables in the Key Attributes object.
     * Uses Lazy Update to set values for variables id, tags, contentType, managed and keyId as these variables are
     * part of main json body and not attributes json body when the secret response comes from list Secrets operations.
     * @param key The key value mapping of the key attributes
     */
    @JsonProperty("key")
    private void unpackKeyMaterial(Map<String, Object> key) {
        final Base64 BASE64 = new Base64(-1, null, true);
        keyMaterial = new JsonWebKey()
            .withY(BASE64.decode((String)key.get("y")))
            .withX(BASE64.decode((String)key.get("x")))
            .withCrv(new JsonWebKeyCurveName((String)key.get("crv")))
            .keyOps(getKeyOperations((List<String>)key.get("key_ops")))
            .withT(BASE64.decode((String)key.get("key_hsm")))
            .withK(BASE64.decode((String)key.get("k")))
            .withQ(BASE64.decode((String)key.get("q")))
            .withP(BASE64.decode((String)key.get("p")))
            .withQi(BASE64.decode((String)key.get("qi")))
            .withDq(BASE64.decode((String)key.get("dq")))
            .withDp(BASE64.decode((String)key.get("dp")))
            .withD(BASE64.decode((String)key.get("d")))
            .rsaExponent(BASE64.decode((String)key.get("rsaExponent")))
            .rsaModulus(BASE64.decode((String)key.get("rsaModulus")))
            .withKty(new JsonWebKeyType((String)key.get("kty")))
            .withKid((String)key.get("kid"));
        keyOperations(getKeyOperations((List<String>)key.get("key_ops")));
        unpackId((String)key.get("kid"));
    }



    private List<JsonWebKeyOperation> getKeyOperations(List<String> jsonWebKeyOps){
        List<JsonWebKeyOperation> output = new ArrayList<>();
        for(String keyOp : jsonWebKeyOps){
            output.add(new JsonWebKeyOperation(keyOp));
        }
        return output;
    }

}
