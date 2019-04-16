// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Slide {
    @JacksonXmlProperty(localName = "type", isAttribute = true)
    private String type;

    @JsonProperty("title")
    private String title;

    @JsonProperty("item")
    private String[] items;

    public String type() {
        return type;
    }

    public void type(String type) {
        this.type = type;
    }

    public String title() {
        return title;
    }

    public void title(String title) {
        this.title = title;
    }

    public String[] items() {
        return items;
    }

    public void items(String[] items) {
        this.items = items;
    }
}
