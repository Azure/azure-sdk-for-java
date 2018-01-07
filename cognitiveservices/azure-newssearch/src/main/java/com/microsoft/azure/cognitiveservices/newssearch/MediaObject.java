/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.newssearch;

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
    @JsonSubTypes.Type(name = "ImageObject", value = ImageObject.class),
    @JsonSubTypes.Type(name = "VideoObject", value = VideoObject.class)
})
public class MediaObject extends CreativeWork {
    /**
     * Original URL to retrieve the source (file) for the media object (e.g the
     * source URL for the image).
     */
    @JsonProperty(value = "contentUrl", access = JsonProperty.Access.WRITE_ONLY)
    private String contentUrl;

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
