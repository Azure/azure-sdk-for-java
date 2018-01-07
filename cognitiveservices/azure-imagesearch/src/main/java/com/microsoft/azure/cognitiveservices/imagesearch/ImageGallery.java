/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.imagesearch;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * Defines a link to a webpage that contains a collection of related images.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = ImageGallery.class)
@JsonTypeName("ImageGallery")
public class ImageGallery extends CollectionPage {
    /**
     * The publisher or social network where the images were found. You must
     * attribute the publisher as the source where the collection was found.
     */
    @JsonProperty(value = "source", access = JsonProperty.Access.WRITE_ONLY)
    private String source;

    /**
     * The number of related images found in the collection.
     */
    @JsonProperty(value = "imagesCount", access = JsonProperty.Access.WRITE_ONLY)
    private Long imagesCount;

    /**
     * The number of users on the social network that follow the creator.
     */
    @JsonProperty(value = "followersCount", access = JsonProperty.Access.WRITE_ONLY)
    private Long followersCount;

    /**
     * Get the source value.
     *
     * @return the source value
     */
    public String source() {
        return this.source;
    }

    /**
     * Get the imagesCount value.
     *
     * @return the imagesCount value
     */
    public Long imagesCount() {
        return this.imagesCount;
    }

    /**
     * Get the followersCount value.
     *
     * @return the followersCount value
     */
    public Long followersCount() {
        return this.followersCount;
    }

}
