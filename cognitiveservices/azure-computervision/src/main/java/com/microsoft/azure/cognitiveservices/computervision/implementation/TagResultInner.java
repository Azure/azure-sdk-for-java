/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.computervision.ImageTag;
import com.microsoft.azure.cognitiveservices.computervision.ImageMetadata;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The results of a image tag operation, including any tags and image metadata.
 */
public class TagResultInner {
    /**
     * A list of tags with confidence level.
     */
    @JsonProperty(value = "tags")
    private List<ImageTag> tags;

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
    public List<ImageTag> tags() {
        return this.tags;
    }

    /**
     * Set the tags value.
     *
     * @param tags the tags value to set
     * @return the TagResultInner object itself.
     */
    public TagResultInner withTags(List<ImageTag> tags) {
        this.tags = tags;
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
     * @return the TagResultInner object itself.
     */
    public TagResultInner withRequestId(String requestId) {
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
     * @return the TagResultInner object itself.
     */
    public TagResultInner withMetadata(ImageMetadata metadata) {
        this.metadata = metadata;
        return this;
    }

}
