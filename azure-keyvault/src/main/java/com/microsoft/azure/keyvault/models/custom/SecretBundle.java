package com.microsoft.azure.keyvault.models.custom;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.keyvault.SecretIdentifier;

import java.io.IOException;

public class SecretBundle {


    /**
     * The secret id.
     */
    @JsonProperty(value = "id")
    private String id;


    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * the secret identifier.
     * @return The Identifier value
     */
    public SecretIdentifier secretIdentifier() {
        if (id() == null || id().length() == 0) {
            return null;
        }
        return new SecretIdentifier(id());
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
