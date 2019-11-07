// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Arrays;

public class Slide {
    @JacksonXmlProperty(localName = "type", isAttribute = true)
    private String type;

    @JsonProperty("title")
    private String title;

    @JsonProperty("item")
    private String[] items;

    /**
     * Gets the type of slide.
     *
     * @return The type of slide.
     */
    public String type() {
        return type;
    }

    /**
     * Gets the slide title.
     *
     * @return The title of the slide.
     */
    public String title() {
        return title;
    }

    /**
     * Gets the content strings of the slide.
     *
     * @return The content strings of the slide.
     */
    public String[] items() {
        if (items == null) {
            return new String[0];
        }
        return Arrays.copyOf(items, items.length);
    }
}
