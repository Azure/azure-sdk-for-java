// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.keys.models;

import com.azure.keyvault.webkey.JsonWebKey;
import com.azure.keyvault.webkey.JsonWebKeyCurveName;
import com.azure.keyvault.webkey.JsonWebKeyOperation;
import com.azure.keyvault.webkey.JsonWebKeyType;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.codec.binary.Base64;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Key extends KeyBase {

    @JsonProperty(value = "key")
    private JsonWebKey keyMaterial;

    /**
     * Get the key value.
     *
     * @return the key value
     */
    public JsonWebKey keyMaterial() {
        return this.keyMaterial;
    }


    private List<JsonWebKeyOperation> getKeyOperations(List<String> jsonWebKeyOps){
        List<JsonWebKeyOperation> output = new ArrayList<>();
        for(String keyOp : jsonWebKeyOps){
            output.add(new JsonWebKeyOperation(keyOp));
        }
        return output;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The notBefore UTC time to set
     * @return the Key object itself.
     */
    @Override
    public Key notBefore(OffsetDateTime notBefore) {
        super.notBefore(notBefore);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expires The expiry time to set for the key.
     * @return the Key object itself.
     */
    @Override
    public Key expires(OffsetDateTime expires) {
        super.expires(expires);
        return this;
    }

    /**
     * Set the contentType.
     *
     * @param contentType The contentType to set
     * @return the Key object itself.
     */
    public Key contentType(String contentType) {
        super.contentType(contentType);
        return this;
    }

    /**
     * Set the tags to be associated with the key.
     *
     * @param tags The tags to set
     * @return the Key object itself.
     */
    public Key tags(Map<String, String> tags) {
        super.tags(tags);
        return this;
    }

    /**
     * Set the keyOps value.
     *
     * @param keyOperations The key operations to set.
     * @return the Key object itself.
     */
    @Override
    public Key keyOperations(List<JsonWebKeyOperation> keyOperations) {
        super.keyOperations(keyOperations);
        return this;
    }

    /**
     * Unpacks the key material json response and updates the variables in the Key Base object.
     * @param key The key value mapping of the key material
     */
    @JsonProperty("key")
    private void unpackKeyMaterial(Map<String, Object> key) {
        final Base64 BASE64 = new Base64(-1, null, true);
        keyMaterial = new JsonWebKey()
                .ecPublicKeyYComponent(BASE64.decode((String)key.get("y")))
                .ecPublicKeyXComponent(BASE64.decode((String)key.get("x")))
                .curve(new JsonWebKeyCurveName((String)key.get("crv")))
                .keyOps(getKeyOperations((List<String>)key.get("key_ops")))
                .keyHsm(BASE64.decode((String)key.get("key_hsm")))
                .symmetricKey(BASE64.decode((String)key.get("k")))
                .rsaSecretPrimeBounded(BASE64.decode((String)key.get("q")))
                .rsaSecretPrime(BASE64.decode((String)key.get("p")))
                .rsaPrivateKeyParameterQi(BASE64.decode((String)key.get("qi")))
                .rsaPrivateKeyParameterDq(BASE64.decode((String)key.get("dq")))
                .rsaPrivateKeyParameterDp(BASE64.decode((String)key.get("dp")))
                .rsaPrivateExponent(BASE64.decode((String)key.get("d")))
                .rsaExponent(BASE64.decode((String)key.get("e")))
                .rsaModulus(BASE64.decode((String)key.get("n")))
                .keyType(new JsonWebKeyType((String)key.get("kty")))
                .keyId((String)key.get("kid"));
        keyOperations(getKeyOperations((List<String>)key.get("key_ops")));
        unpackId((String)key.get("kid"));
    }
}
