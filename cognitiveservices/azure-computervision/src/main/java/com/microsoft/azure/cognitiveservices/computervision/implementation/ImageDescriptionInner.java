/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.computervision.ImageCaption;
import com.microsoft.azure.cognitiveservices.computervision.ImageMetadata;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.microsoft.rest.serializer.JsonFlatten;

/**
 * A collection of content tags, along with a list of captions sorted by
 * confidence level, and image metadata.
 */
@JsonFlatten
public class ImageDescriptionInner {
    /**
     * A collection of image tags.
     */
    @JsonProperty(value = "description.tags")
    private List<String> tags;

    /**
     * A list of captions, sorted by confidence level.
     */
    @JsonProperty(value = "description.captions")
    private List<ImageCaption> captions;

    /**
     * Id of the REST API request.
     */
    @JsonProperty(value = "description.requestId")
    private String requestId;

    /**
     * Image metadata.
     */
    @JsonProperty(value = "description.metadata")
    private ImageMetadata metadata;

    /**
     * Get the tags value.
     *
     * @return the tags value
     */
    public List<String> tags() {
        return this.tags;
    }

    /**
     * Set the tags value.
     *
     * @param tags the tags value to set
     * @return the ImageDescriptionInner object itself.
     */
    public ImageDescriptionInner withTags(List<String> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the captions value.
     *
     * @return the captions value
     */
    public List<ImageCaption> captions() {
        return this.captions;
    }

    /**
     * Set the captions value.
     *
     * @param captions the captions value to set
     * @return the ImageDescriptionInner object itself.
     */
    public ImageDescriptionInner withCaptions(List<ImageCaption> captions) {
        this.captions = captions;
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
     * @return the ImageDescriptionInner object itself.
     */
    public ImageDescriptionInner withRequestId(String requestId) {
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
     * @return the ImageDescriptionInner object itself.
     */
    public ImageDescriptionInner withMetadata(ImageMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

}
