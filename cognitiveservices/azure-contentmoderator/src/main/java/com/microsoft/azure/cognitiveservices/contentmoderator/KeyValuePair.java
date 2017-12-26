/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The key value pair object properties.
 */
public class KeyValuePair {
    /**
     * The key parameter.
     */
    @JsonProperty(value = "Key")
    private String key;

    /**
     * The value parameter.
     */
    @JsonProperty(value = "Value")
    private String value;

    /**
     * Get the key value.
     *
     * @return the key value
     */
    public String key() {
        return this.key;
    }

    /**
     * Set the key value.
     *
     * @param key the key value to set
     * @return the KeyValuePair object itself.
     */
    public KeyValuePair withKey(String key) {
        this.key = key;
        return this;
    }

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
     * @return the KeyValuePair object itself.
     */
    public KeyValuePair withValue(String value) {
        this.value = value;
        return this;
    }

}
