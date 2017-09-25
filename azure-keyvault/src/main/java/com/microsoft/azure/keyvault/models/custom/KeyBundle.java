package com.microsoft.azure.keyvault.models.custom;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.keyvault.KeyIdentifier;
import com.microsoft.azure.keyvault.webkey.JsonWebKey;

import java.io.IOException;

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
