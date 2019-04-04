package models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KeyVaultConfiguration {
    @JsonProperty("endpointUri")
    private String endpointUri;

    @JsonProperty("secret")
    private String secret;

    public String endpointUri() {
        return endpointUri;
    }

    public KeyVaultConfiguration endpointUri(String endpointUri) {
        this.endpointUri = endpointUri;
        return this;
    }

    public String secret() {
        return secret;
    }

    public KeyVaultConfiguration secret(String secret) {
        this.secret = secret;
        return this;
    }

    @Override
    public String toString() {
        return "Endpoint: " + endpointUri() + ", Secret: " + secret();
    }
}
