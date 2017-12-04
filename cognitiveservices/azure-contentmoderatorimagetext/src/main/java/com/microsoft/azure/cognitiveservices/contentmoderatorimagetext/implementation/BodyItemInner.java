/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.contentmoderatorimagetext.BodyItemMetadataItem;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Schema items of the body.
 */
public class BodyItemInner {
    /**
     * Type of the content. Possible values include: 'Image', 'Text'.
     */
    @JsonProperty(value = "Type", required = true)
    private String type;

    /**
     * Content to review.
     */
    @JsonProperty(value = "Content", required = true)
    private String content;

    /**
     * Content Identifier.
     */
    @JsonProperty(value = "ContentId", required = true)
    private String contentId;

    /**
     * Optional CallbackEndpoint.
     */
    @JsonProperty(value = "CallbackEndpoint")
    private String callbackEndpoint;

    /**
     * Optional metadata details.
     */
    @JsonProperty(value = "Metadata")
    private List<BodyItemMetadataItem> metadata;

    /**
     * Get the type value.
     *
     * @return the type value
     */
    public String type() {
        return this.type;
    }

    /**
     * Set the type value.
     *
     * @param type the type value to set
     * @return the BodyItemInner object itself.
     */
    public BodyItemInner withType(String type) {
        this.type = type;
        return this;
    }

    /**
     * Get the content value.
     *
     * @return the content value
     */
    public String content() {
        return this.content;
    }

    /**
     * Set the content value.
     *
     * @param content the content value to set
     * @return the BodyItemInner object itself.
     */
    public BodyItemInner withContent(String content) {
        this.content = content;
        return this;
    }

    /**
     * Get the contentId value.
     *
     * @return the contentId value
     */
    public String contentId() {
        return this.contentId;
    }

    /**
     * Set the contentId value.
     *
     * @param contentId the contentId value to set
     * @return the BodyItemInner object itself.
     */
    public BodyItemInner withContentId(String contentId) {
        this.contentId = contentId;
        return this;
    }

    /**
     * Get the callbackEndpoint value.
     *
     * @return the callbackEndpoint value
     */
    public String callbackEndpoint() {
        return this.callbackEndpoint;
    }

    /**
     * Set the callbackEndpoint value.
     *
     * @param callbackEndpoint the callbackEndpoint value to set
     * @return the BodyItemInner object itself.
     */
    public BodyItemInner withCallbackEndpoint(String callbackEndpoint) {
        this.callbackEndpoint = callbackEndpoint;
        return this;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public List<BodyItemMetadataItem> metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     * @return the BodyItemInner object itself.
     */
    public BodyItemInner withMetadata(List<BodyItemMetadataItem> metadata) {
        this.metadata = metadata;
        return this;
    }

}
