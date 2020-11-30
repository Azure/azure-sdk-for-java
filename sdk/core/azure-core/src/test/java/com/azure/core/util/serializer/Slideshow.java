// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.util.Arrays;

public class Slideshow {
    @JacksonXmlProperty(localName = "title", isAttribute = true)
    private String title;

    @JacksonXmlProperty(localName = "date", isAttribute = true)
    private String date;

    @JacksonXmlProperty(localName = "author", isAttribute = true)
    private String author;

    @JsonProperty("slide")
    private Slide[] slides;

    /**
     * Gets the title of the slideshow.
     *
     * @return The title of the slideshow.
     */
    public String title() {
        return title;
    }

    /**
     * Gets the date of publication.
     *
     * @return The date of publication.
     */
    public String date() {
        return date;
    }

    /**
     * Gets the slideshow author.
     *
     * @return Author of the slideshow.
     */
    public String author() {
        return author;
    }

    /**
     * Gets the slides in the slideshow.
     *
     * @return The slides in the slideshow.
     */
    public Slide[] slides() {
        if (slides == null) {
            return new Slide[0];
        }
        return Arrays.copyOf(slides, slides.length);
    }
}
