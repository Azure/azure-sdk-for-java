// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.common.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Slideshow {
    @JacksonXmlProperty(localName = "title", isAttribute = true)
    private String title;

    @JacksonXmlProperty(localName = "date", isAttribute = true)
    private String date;

    @JacksonXmlProperty(localName = "author", isAttribute = true)
    private String author;

    @JsonProperty("slide")
    private Slide[] slides;

    public String title() {
        return title;
    }

    public void title(String title) {
        this.title = title;
    }

    public String date() {
        return date;
    }

    public void date(String date) {
        this.date = date;
    }

    public String author() {
        return author;
    }

    public void author(String author) {
        this.author = author;
    }

    public Slide[] slides() {
        return slides;
    }

    public void slides(Slide[] slides) {
        this.slides = slides;
    }
}
