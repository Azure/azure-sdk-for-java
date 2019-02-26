/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.azconfig.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Parameters passed to create or update KeyValue; passed in the body of the request.
 */
public class KeyValueCreateUpdateParameters {
    @JsonProperty(value = "value")
    private String value;

    @JsonProperty(value = "content_type")
    private String contentType;

    @JsonProperty(value = "tags")
    private Map<String, String> tags;

    /**
     * @return value
     */
    public String value() {
        return value;
    }

    /**
     * Sets the value.
     * @param value value
     * @return KeyValueCreateUpdateParameters object itself
     */
    public KeyValueCreateUpdateParameters withValue(String value) {
        this.value = value;
        return this;
    }

    /**
     * @return content type
     */
    public String contentType() {
        return contentType;
    }

    /**
     * Sets the content type.
     * @param contentType content type
     * @return KeyValueCreateUpdateParameters object itself
     */
    public KeyValueCreateUpdateParameters withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * @return tags
     */
    public Map<String, String> tags() {
        return tags;
    }

    /**
     * Sets the tags.
     * @param tags tags
     * @return KeyValueCreateUpdateParameters object itself
     */
    public KeyValueCreateUpdateParameters withTags(Map<String, String> tags) {
        this.tags = tags;
        return this;
    }
}
