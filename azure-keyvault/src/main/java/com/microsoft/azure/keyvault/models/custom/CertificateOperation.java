package com.microsoft.azure.keyvault.models.custom;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.keyvault.CertificateOperationIdentifier;

import java.io.IOException;

public class CertificateOperation {
    /**
     * The certificate id.
     */
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
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
     * The certificate operation identifier.
     * @return the identifier value
     */
    public CertificateOperationIdentifier certificateOperationIdentifier() {
        if (id() == null || id().isEmpty()) {
            return null;
        }
        return new CertificateOperationIdentifier(id());
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
