/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A collection of content tags, along with a list of captions sorted by
 * confidence level, and image metadata.
 */
public class ImageDescriptionDetails {
    /**
     * A collection of image tags.
     */
    @JsonProperty(value = "tags")
    private List<String> tags;

    /**
     * A list of captions, sorted by confidence level.
     */
    @JsonProperty(value = "captions")
    private List<ImageCaption> captions;

    /**
     * Id of the REST API request.
     */
    @JsonProperty(value = "requestId")
    private String requestId;

    /**
     * Image metadata.
     */
    @JsonProperty(value = "metadata")
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
     * @return the ImageDescriptionDetails object itself.
     */
    public ImageDescriptionDetails withTags(List<String> tags) {
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
     * @return the ImageDescriptionDetails object itself.
     */
    public ImageDescriptionDetails withCaptions(List<ImageCaption> captions) {
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
     * @return the ImageDescriptionDetails object itself.
     */
    public ImageDescriptionDetails withRequestId(String requestId) {
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
     * @return the ImageDescriptionDetails object itself.
     */
    public ImageDescriptionDetails withMetadata(ImageMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

}
