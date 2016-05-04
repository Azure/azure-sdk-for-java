/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation.api;

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
    public String key() {
        return this.key;
    }

    /**
     * Get the value value.
     *
     * @return the value value
     */
    public String value() {
        return this.value;
    }

}
