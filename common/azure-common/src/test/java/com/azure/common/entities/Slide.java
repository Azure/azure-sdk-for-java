package com.azure.common.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Slide {
    @JacksonXmlProperty(localName = "type", isAttribute = true)
    public String type;

    @JsonProperty("title")
    public String title;

    @JsonProperty("item")
    public String[] items;
}
