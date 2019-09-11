// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.Map;

public class Key extends KeyBase {

    @JsonProperty(value = "key")
    private JsonWebKey keyMaterial;

    /**
     * Get the key value.
     *
     * @return the key value
     */
    public JsonWebKey getKeyMaterial() {
        return this.keyMaterial;
    }

    /**
     * Set the {@link OffsetDateTime notBefore} UTC time.
     *
     * @param notBefore The notBefore UTC time to set
     * @return the Key object itself.
     */
    @Override
    public Key setNotBefore(OffsetDateTime notBefore) {
        super.setNotBefore(notBefore);
        return this;
    }

    /**
     * Set the {@link OffsetDateTime expires} UTC time.
     *
     * @param expires The expiry time to set for the key.
     * @return the Key object itself.
     */
    @Override
    public Key setExpires(OffsetDateTime expires) {
        super.setExpires(expires);
        return this;
    }

    /**
     * Set the tags to be associated with the key.
     *
     * @param tags The tags to set
     * @return the Key object itself.
     */
    public Key setTags(Map<String, String> tags) {
        super.setTags(tags);
        return this;
    }

    /**
     * Unpacks the key material json response and updates the variables in the Key Base object.
     * @param key The key value mapping of the key material
     */
    @JsonProperty("key")
    private void unpackKeyMaterial(Map<String, Object> key) {
        keyMaterial = createKeyMaterialFromJson(key);
    }
}
