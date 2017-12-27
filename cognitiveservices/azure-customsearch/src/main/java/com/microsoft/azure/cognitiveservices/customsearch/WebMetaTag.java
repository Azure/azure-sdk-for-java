/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.customsearch;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Defines a webpage's metadata.
 */
public class WebMetaTag {
    /**
     * The metadata.
     */
    @JsonProperty(value = "name", access = JsonProperty.Access.WRITE_ONLY)
    private String name;

    /**
     * The name of the metadata.
     */
    @JsonProperty(value = "content", access = JsonProperty.Access.WRITE_ONLY)
    private String content;

    /**
     * Get the name value.
     *
     * @return the name value
     */
    public String name() {
        return this.name;
    }

    /**
     * Get the content value.
     *
     * @return the content value
     */
    public String content() {
        return this.content;
    }

}
