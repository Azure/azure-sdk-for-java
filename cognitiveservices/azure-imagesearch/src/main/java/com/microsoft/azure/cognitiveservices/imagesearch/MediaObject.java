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
 * Defines a media object.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = MediaObject.class)
@JsonTypeName("MediaObject")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "ImageObject", value = ImageObject.class)
})
public class MediaObject extends CreativeWork {
    /**
     * Original URL to retrieve the source (file) for the media object (e.g the
     * source URL for the image).
     */
    @JsonProperty(value = "contentUrl", access = JsonProperty.Access.WRITE_ONLY)
    private String contentUrl;

    /**
     * URL of the page that hosts the media object.
     */
    @JsonProperty(value = "hostPageUrl", access = JsonProperty.Access.WRITE_ONLY)
    private String hostPageUrl;

    /**
     * Size of the media object content (use format "value unit" e.g "1024 B").
     */
    @JsonProperty(value = "contentSize", access = JsonProperty.Access.WRITE_ONLY)
    private String contentSize;

    /**
     * Encoding format (e.g mp3, mp4, jpeg, etc).
     */
    @JsonProperty(value = "encodingFormat", access = JsonProperty.Access.WRITE_ONLY)
    private String encodingFormat;

    /**
     * Display URL of the page that hosts the media object.
     */
    @JsonProperty(value = "hostPageDisplayUrl", access = JsonProperty.Access.WRITE_ONLY)
    private String hostPageDisplayUrl;

    /**
     * The width of the source media object, in pixels.
     */
    @JsonProperty(value = "width", access = JsonProperty.Access.WRITE_ONLY)
    private Integer width;

    /**
     * The height of the source media object, in pixels.
     */
    @JsonProperty(value = "height", access = JsonProperty.Access.WRITE_ONLY)
    private Integer height;

    /**
     * Get the contentUrl value.
     *
     * @return the contentUrl value
     */
    public String contentUrl() {
        return this.contentUrl;
    }

    /**
     * Get the hostPageUrl value.
     *
     * @return the hostPageUrl value
     */
    public String hostPageUrl() {
        return this.hostPageUrl;
    }

    /**
     * Get the contentSize value.
     *
     * @return the contentSize value
     */
    public String contentSize() {
        return this.contentSize;
    }

    /**
     * Get the encodingFormat value.
     *
     * @return the encodingFormat value
     */
    public String encodingFormat() {
        return this.encodingFormat;
    }

    /**
     * Get the hostPageDisplayUrl value.
     *
     * @return the hostPageDisplayUrl value
     */
    public String hostPageDisplayUrl() {
        return this.hostPageDisplayUrl;
    }

    /**
     * Get the width value.
     *
     * @return the width value
     */
    public Integer width() {
        return this.width;
    }

    /**
     * Get the height value.
     *
     * @return the height value
     */
    public Integer height() {
        return this.height;
    }

}
