// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;


/**
 * Represents a set of request options used in REST requests intitiated by Secret service.
 */
class SecretRequestParameters {
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
     * @return the SecretRequestParameters object itself.
     */
    public SecretRequestParameters value(String value) {
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
     * @return the SecretRequestParameters object itself.
     */
    public SecretRequestParameters tags(Map<String, String> tags) {
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
     * @return the SecretRequestParameters object itself.
     */
    public SecretRequestParameters contentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Get the secretRequestAttributes value.
     *
     * @return the SecretRequestAttributes value
     */
    public SecretRequestAttributes secretAttributes() {
        return this.secretRequestAttributes;
    }

    /**
     * Set the secretRequestAttributes value.
     *
     * @param secretRequestAttributes the secretRequestAttributes to set
     * @return the SecretRequestParameters object itself.
     */
    public SecretRequestParameters secretAttributes(SecretRequestAttributes secretRequestAttributes) {
        this.secretRequestAttributes = secretRequestAttributes;
        return this;
    }

}
