// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.models.custom;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.keyvault.KeyIdentifier;
import com.microsoft.azure.keyvault.webkey.JsonWebKey;

import java.io.IOException;

/**
 * A KeyBundle consisting of a WebKey plus its attributes.
 */
public class KeyBundle {

    /**
     * The Json web key.
     */
    @JsonProperty(value = "key")
    private JsonWebKey key;


    /**
     * Get the key value.
     *
     * @return the key value
     */
    public JsonWebKey key() {
        return this.key;
    }

    /**
     * The key identifier.
     * @return identifier for the key
     */
    public KeyIdentifier keyIdentifier() {
        if (key() == null || key().kid() == null || key().kid().length() == 0) {
            return null;
        }
        return new KeyIdentifier(key().kid());
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonGenerationException e) {
            throw new IllegalStateException(e);
        } catch (JsonMappingException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
