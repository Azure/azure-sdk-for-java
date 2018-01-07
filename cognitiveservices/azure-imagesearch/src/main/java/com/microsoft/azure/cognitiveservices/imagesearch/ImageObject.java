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
 * Defines an image.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "_type",
        defaultImpl = ImageObject.class)
@JsonTypeName("ImageObject")
public class ImageObject extends MediaObject {
    /**
     * The URL to a thumbnail of the image.
     */
    @JsonProperty(value = "thumbnail", access = JsonProperty.Access.WRITE_ONLY)
    private ImageObject thumbnail;

    /**
     * The token that you use in a subsequent call to the Image Search API to
     * get additional information about the image. For information about using
     * this token, see the insightsToken query parameter.
     */
    @JsonProperty(value = "imageInsightsToken", access = JsonProperty.Access.WRITE_ONLY)
    private String imageInsightsToken;

    /**
     * A count of the number of websites where you can shop or perform other
     * actions related to the image. For example, if the image is of an apple
     * pie, this object includes a count of the number of websites where you
     * can buy an apple pie. To indicate the number of offers in your UX,
     * include badging such as a shopping cart icon that contains the count.
     * When the user clicks on the icon, use imageInisghtsToken to get the list
     * of websites.
     */
    @JsonProperty(value = "insightsMetadata", access = JsonProperty.Access.WRITE_ONLY)
    private ImagesImageMetadata insightsMetadata;

    /**
     * Unique Id for the image.
     */
    @JsonProperty(value = "imageId", access = JsonProperty.Access.WRITE_ONLY)
    private String imageId;

    /**
     * A three-byte hexadecimal number that represents the color that dominates
     * the image. Use the color as the temporary background in your client
     * until the image is loaded.
     */
    @JsonProperty(value = "accentColor", access = JsonProperty.Access.WRITE_ONLY)
    private String accentColor;

    /**
     * Visual representation of the image. Used for getting more sizes.
     */
    @JsonProperty(value = "visualWords", access = JsonProperty.Access.WRITE_ONLY)
    private String visualWords;

    /**
     * Get the thumbnail value.
     *
     * @return the thumbnail value
     */
    public ImageObject thumbnail() {
        return this.thumbnail;
    }

    /**
     * Get the imageInsightsToken value.
     *
     * @return the imageInsightsToken value
     */
    public String imageInsightsToken() {
        return this.imageInsightsToken;
    }

    /**
     * Get the insightsMetadata value.
     *
     * @return the insightsMetadata value
     */
    public ImagesImageMetadata insightsMetadata() {
        return this.insightsMetadata;
    }

    /**
     * Get the imageId value.
     *
     * @return the imageId value
     */
    public String imageId() {
        return this.imageId;
    }

    /**
     * Get the accentColor value.
     *
     * @return the accentColor value
     */
    public String accentColor() {
        return this.accentColor;
    }

    /**
     * Get the visualWords value.
     *
     * @return the visualWords value
     */
    public String visualWords() {
        return this.visualWords;
    }

}
