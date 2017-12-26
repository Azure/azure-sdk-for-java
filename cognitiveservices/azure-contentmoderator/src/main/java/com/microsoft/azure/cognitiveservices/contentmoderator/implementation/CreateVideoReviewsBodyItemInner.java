/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.contentmoderator.CreateVideoReviewsBodyItemVideoFramesItem;
import com.microsoft.azure.cognitiveservices.contentmoderator.CreateVideoReviewsBodyItemMetadataItem;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Schema items of the body.
 */
public class CreateVideoReviewsBodyItemInner {
    /**
     * Optional metadata details.
     */
    @JsonProperty(value = "VideoFrames")
    private List<CreateVideoReviewsBodyItemVideoFramesItem> videoFrames;

    /**
     * Optional metadata details.
     */
    @JsonProperty(value = "Metadata")
    private List<CreateVideoReviewsBodyItemMetadataItem> metadata;

    /**
     * Type of the content.
     */
    @JsonProperty(value = "Type", required = true)
    private String type;

    /**
     * Video content url to review.
     */
    @JsonProperty(value = "Content", required = true)
    private String content;

    /**
     * Content Identifier.
     */
    @JsonProperty(value = "ContentId", required = true)
    private String contentId;

    /**
     * Status of the video(Complete,Unpublished,Pending). Possible values
     * include: 'Complete', 'Unpublished', 'Pending'.
     */
    @JsonProperty(value = "Status", required = true)
    private String status;

    /**
     * Timescale of the video.
     */
    @JsonProperty(value = "Timescale")
    private Integer timescale;

    /**
     * Optional CallbackEndpoint.
     */
    @JsonProperty(value = "CallbackEndpoint")
    private String callbackEndpoint;

    /**
     * Creates an instance of CreateVideoReviewsBodyItemInner class.
     */
    public CreateVideoReviewsBodyItemInner() {
        type = "Video";
    }

    /**
     * Get the videoFrames value.
     *
     * @return the videoFrames value
     */
    public List<CreateVideoReviewsBodyItemVideoFramesItem> videoFrames() {
        return this.videoFrames;
    }

    /**
     * Set the videoFrames value.
     *
     * @param videoFrames the videoFrames value to set
     * @return the CreateVideoReviewsBodyItemInner object itself.
     */
    public CreateVideoReviewsBodyItemInner withVideoFrames(List<CreateVideoReviewsBodyItemVideoFramesItem> videoFrames) {
        this.videoFrames = videoFrames;
        return this;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public List<CreateVideoReviewsBodyItemMetadataItem> metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     * @return the CreateVideoReviewsBodyItemInner object itself.
     */
    public CreateVideoReviewsBodyItemInner withMetadata(List<CreateVideoReviewsBodyItemMetadataItem> metadata) {
        this.metadata = metadata;
        return this;
    }

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
     * @return the CreateVideoReviewsBodyItemInner object itself.
     */
    public CreateVideoReviewsBodyItemInner withType(String type) {
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
     * @return the CreateVideoReviewsBodyItemInner object itself.
     */
    public CreateVideoReviewsBodyItemInner withContent(String content) {
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
     * @return the CreateVideoReviewsBodyItemInner object itself.
     */
    public CreateVideoReviewsBodyItemInner withContentId(String contentId) {
        this.contentId = contentId;
        return this;
    }

    /**
     * Get the status value.
     *
     * @return the status value
     */
    public String status() {
        return this.status;
    }

    /**
     * Set the status value.
     *
     * @param status the status value to set
     * @return the CreateVideoReviewsBodyItemInner object itself.
     */
    public CreateVideoReviewsBodyItemInner withStatus(String status) {
        this.status = status;
        return this;
    }

    /**
     * Get the timescale value.
     *
     * @return the timescale value
     */
    public Integer timescale() {
        return this.timescale;
    }

    /**
     * Set the timescale value.
     *
     * @param timescale the timescale value to set
     * @return the CreateVideoReviewsBodyItemInner object itself.
     */
    public CreateVideoReviewsBodyItemInner withTimescale(Integer timescale) {
        this.timescale = timescale;
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
     * @return the CreateVideoReviewsBodyItemInner object itself.
     */
    public CreateVideoReviewsBodyItemInner withCallbackEndpoint(String callbackEndpoint) {
        this.callbackEndpoint = callbackEndpoint;
        return this;
    }

}
