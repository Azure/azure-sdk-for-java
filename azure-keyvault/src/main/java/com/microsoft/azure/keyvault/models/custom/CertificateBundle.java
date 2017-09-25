package com.microsoft.azure.keyvault.models.custom;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.keyvault.CertificateIdentifier;
import com.microsoft.azure.keyvault.KeyIdentifier;
import com.microsoft.azure.keyvault.SecretIdentifier;

import java.io.IOException;

public class CertificateBundle {

    /**
     * The certificate id.
     */
    @JsonProperty(value = "id", access = JsonProperty.Access.WRITE_ONLY)
    private String id;

    /**
     * The key id.
     */
    @JsonProperty(value = "kid", access = JsonProperty.Access.WRITE_ONLY)
    private String kid;

    /**
     * The secret id.
     */
    @JsonProperty(value = "sid", access = JsonProperty.Access.WRITE_ONLY)
    private String sid;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Get the kid value.
     *
     * @return the kid value
     */
    public String kid() {
        return this.kid;
    }

    /**
     * Get the sid value.
     *
     * @return the sid value
     */
    public String sid() {
        return this.sid;
    }


    /**
     * The certificate identifier.
     * @return certificate identifier
     */
    public CertificateIdentifier certificateIdentifier() {
        if (id() == null || id().isEmpty()) {
            return null;
        }
        return new CertificateIdentifier(id());
    }

    /**
     * The secret identifier.
     * @return secret identifier
     */
    public SecretIdentifier secretIdentifier() {
        if (sid() == null || sid().isEmpty()) {
            return null;
        }
        return new SecretIdentifier(sid());
    }

    /**
     * The key identifier.
     * @return key identifier
     */
    public KeyIdentifier keyIdentifier() {
        if (kid() == null || kid().isEmpty()) {
            return null;
        }
        return new KeyIdentifier(kid());
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
