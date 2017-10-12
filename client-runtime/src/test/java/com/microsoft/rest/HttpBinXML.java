package com.microsoft.rest;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * Represents the XML returned by httpbin.org.
 */
@JacksonXmlRootElement(localName = "slideshow")
public class HttpBinXML {
    @JacksonXmlProperty()
    public Object[] slides;
}
