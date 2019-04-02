package com.azure.keyvault.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * The secret request options object used in REST requests intitiated by Secret service.
 */
public class SecretRequestOptions {
    /**
     * The value of the secret.
     */
    @JsonProperty(value = "value", required = true)
    private String value;

    /**
     * Application specific metadata in the form of key-value pairs.
     */
    @JsonProperty(value = "tags")
    private Map<String, String> tags;

    /**
     * Type of the secret value such as a password.
     */
    @JsonProperty(value = "contentType")
    private String contentType;

    /**
     * The secret management attributes.
     */
    @JsonProperty(value = "attributes")
    private SecretRequestAttributes secretRequestAttributes;

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public String value() {
        return this.value;
    }

    /**
     * Set the value value.
     *
     * @param value the value value to set
     * @return the SecretRequestOptions object itself.
     */
    public SecretRequestOptions withValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * Get the tags value.
     *
     * @return the tags value
     */
    public Map<String, String> tags() {
        return this.tags;
    }

    /**
     * Set the tags value.
     *
     * @param tags the tags value to set
     * @return the SecretRequestOptions object itself.
     */
    public SecretRequestOptions withTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the contentType value.
     *
     * @return the contentType value
     */
    public String contentType() {
        return this.contentType;
    }

    /**
     * Set the contentType value.
     *
     * @param contentType the contentType value to set
     * @return the SecretRequestOptions object itself.
     */
    public SecretRequestOptions withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Get the secretRequestAttributes value.
     *
     * @return the secretRequestAttributes value
     */
    public SecretRequestAttributes secretAttributes() {
        return this.secretRequestAttributes;
    }

    /**
     * Set the secretRequestAttributes value.
     *
     * @param secretRequestAttributes the secretRequestAttributes value to set
     * @return the SecretRequestOptions object itself.
     */
    public SecretRequestOptions withSecretAttributes(SecretRequestAttributes secretRequestAttributes) {
        this.secretRequestAttributes = secretRequestAttributes;
        return this;
    }

}
