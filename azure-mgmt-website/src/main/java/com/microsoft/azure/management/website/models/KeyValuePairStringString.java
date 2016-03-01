/**
 * Object]
 */

package com.microsoft.azure.management.website.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The KeyValuePairStringString model.
 */
public class KeyValuePairStringString {
    /**
     * The key property.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String key;

    /**
     * The value property.
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String value;

    /**
     * Get the key value.
     *
     * @return the key value
     */
    public String getKey() {
        return this.key;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public String getValue() {
        return this.value;
    }

}
