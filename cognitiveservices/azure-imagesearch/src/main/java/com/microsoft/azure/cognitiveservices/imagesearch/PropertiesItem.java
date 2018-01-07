/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonSubTypes;

/**
 * Defines an item.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = PropertiesItem.class)
@JsonTypeName("Properties/Item")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "Rating", value = Rating.class)
})
public class PropertiesItem {
    /**
     * Text representation of an item.
     */
    @JsonProperty(value = "text", access = JsonProperty.Access.WRITE_ONLY)
    private String text;

    /**
     * Get the text value.
     *
     * @return the text value
     */
    public String text() {
        return this.text;
    }

}
