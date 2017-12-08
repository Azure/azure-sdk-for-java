/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.computervision.Category;
import com.microsoft.azure.cognitiveservices.computervision.AdultInfo;
import com.microsoft.azure.cognitiveservices.computervision.ColorInfo;
import com.microsoft.azure.cognitiveservices.computervision.ImageType;
import com.microsoft.azure.cognitiveservices.computervision.ImageTag;
import com.microsoft.azure.cognitiveservices.computervision.ImageDescriptionDetails;
import com.microsoft.azure.cognitiveservices.computervision.FaceDescription;
import com.microsoft.azure.cognitiveservices.computervision.ImageMetadata;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of AnalyzeImage operation.
 */
public class ImageAnalysisInner {
    /**
     * An array indicating identified categories.
     */
    @JsonProperty(value = "categories")
    private List<Category> categories;

    /**
     * A property scoring on whether the image is adult-oriented and/or racy.
     */
    @JsonProperty(value = "adult")
    private AdultInfo adult;

    /**
     * A property scoring on color spectrums.
     */
    @JsonProperty(value = "color")
    private ColorInfo color;

    /**
     * A property indicating type of image (whether it's clipart or line
     * drawing).
     */
    @JsonProperty(value = "imageType")
    private ImageType imageType;

    /**
     * A list of tags with confidence level.
     */
    @JsonProperty(value = "tags")
    private List<ImageTag> tags;

    /**
     * Description of the image.
     */
    @JsonProperty(value = "description")
    private ImageDescriptionDetails description;

    /**
     * An array of possible faces within the image.
     */
    @JsonProperty(value = "faces")
    private List<FaceDescription> faces;

    /**
     * Id of the request for tracking purposes.
     */
    @JsonProperty(value = "requestId")
    private String requestId;

    /**
     * Image metadata.
     */
    @JsonProperty(value = "metadata")
    private ImageMetadata metadata;

    /**
     * Get the categories value.
     *
     * @return the categories value
     */
    public List<Category> categories() {
        return this.categories;
    }

    /**
     * Set the categories value.
     *
     * @param categories the categories value to set
     * @return the ImageAnalysisInner object itself.
     */
    public ImageAnalysisInner withCategories(List<Category> categories) {
        this.categories = categories;
        return this;
    }

    /**
     * Get the adult value.
     *
     * @return the adult value
     */
    public AdultInfo adult() {
        return this.adult;
    }

    /**
     * Set the adult value.
     *
     * @param adult the adult value to set
     * @return the ImageAnalysisInner object itself.
     */
    public ImageAnalysisInner withAdult(AdultInfo adult) {
        this.adult = adult;
        return this;
    }

    /**
     * Get the color value.
     *
     * @return the color value
     */
    public ColorInfo color() {
        return this.color;
    }

    /**
     * Set the color value.
     *
     * @param color the color value to set
     * @return the ImageAnalysisInner object itself.
     */
    public ImageAnalysisInner withColor(ColorInfo color) {
        this.color = color;
        return this;
    }

    /**
     * Get the imageType value.
     *
     * @return the imageType value
     */
    public ImageType imageType() {
        return this.imageType;
    }

    /**
     * Set the imageType value.
     *
     * @param imageType the imageType value to set
     * @return the ImageAnalysisInner object itself.
     */
    public ImageAnalysisInner withImageType(ImageType imageType) {
        this.imageType = imageType;
        return this;
    }

    /**
     * Get the tags value.
     *
     * @return the tags value
     */
    public List<ImageTag> tags() {
        return this.tags;
    }

    /**
     * Set the tags value.
     *
     * @param tags the tags value to set
     * @return the ImageAnalysisInner object itself.
     */
    public ImageAnalysisInner withTags(List<ImageTag> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the description value.
     *
     * @return the description value
     */
    public ImageDescriptionDetails description() {
        return this.description;
    }

    /**
     * Set the description value.
     *
     * @param description the description value to set
     * @return the ImageAnalysisInner object itself.
     */
    public ImageAnalysisInner withDescription(ImageDescriptionDetails description) {
        this.description = description;
        return this;
    }

    /**
     * Get the faces value.
     *
     * @return the faces value
     */
    public List<FaceDescription> faces() {
        return this.faces;
    }

    /**
     * Set the faces value.
     *
     * @param faces the faces value to set
     * @return the ImageAnalysisInner object itself.
     */
    public ImageAnalysisInner withFaces(List<FaceDescription> faces) {
        this.faces = faces;
        return this;
    }

    /**
     * Get the requestId value.
     *
     * @return the requestId value
     */
    public String requestId() {
        return this.requestId;
    }

    /**
     * Set the requestId value.
     *
     * @param requestId the requestId value to set
     * @return the ImageAnalysisInner object itself.
     */
    public ImageAnalysisInner withRequestId(String requestId) {
        this.requestId = requestId;
        return this;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public ImageMetadata metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     * @return the ImageAnalysisInner object itself.
     */
    public ImageAnalysisInner withMetadata(ImageMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

}
