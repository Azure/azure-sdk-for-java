package com.azure.common.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Slideshow {
    @JacksonXmlProperty(localName = "title", isAttribute = true)
    public String title;

    @JacksonXmlProperty(localName = "date", isAttribute = true)
    public String date;

    @JacksonXmlProperty(localName = "author", isAttribute = true)
    public String author;

    @JsonProperty("slide")
    public Slide[] slides;
}
