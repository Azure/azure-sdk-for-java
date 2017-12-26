/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The CreateVideoReviewsBodyItemVideoFramesItem model.
 */
public class CreateVideoReviewsBodyItemVideoFramesItem {
    /**
     * Id of the frame.
     */
    @JsonProperty(value = "Id", required = true)
    private String id;

    /**
     * Timestamp of the frame.
     */
    @JsonProperty(value = "Timestamp", required = true)
    private int timestamp;

    /**
     * Frame image Url.
     */
    @JsonProperty(value = "FrameImage", required = true)
    private String frameImage;

    /**
     * The reviewerResultTags property.
     */
    @JsonProperty(value = "ReviewerResultTags")
    private List<CreateVideoReviewsBodyItemVideoFramesItemReviewerResultTagsItem> reviewerResultTags;

    /**
     * Optional metadata details.
     */
    @JsonProperty(value = "Metadata")
    private List<CreateVideoReviewsBodyItemVideoFramesItemMetadataItem> metadata;

    /**
     * Get the id value.
     *
     * @return the id value
     */
    public String id() {
        return this.id;
    }

    /**
     * Set the id value.
     *
     * @param id the id value to set
     * @return the CreateVideoReviewsBodyItemVideoFramesItem object itself.
     */
    public CreateVideoReviewsBodyItemVideoFramesItem withId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the timestamp value.
     *
     * @return the timestamp value
     */
    public int timestamp() {
        return this.timestamp;
    }

    /**
     * Set the timestamp value.
     *
     * @param timestamp the timestamp value to set
     * @return the CreateVideoReviewsBodyItemVideoFramesItem object itself.
     */
    public CreateVideoReviewsBodyItemVideoFramesItem withTimestamp(int timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Get the frameImage value.
     *
     * @return the frameImage value
     */
    public String frameImage() {
        return this.frameImage;
    }

    /**
     * Set the frameImage value.
     *
     * @param frameImage the frameImage value to set
     * @return the CreateVideoReviewsBodyItemVideoFramesItem object itself.
     */
    public CreateVideoReviewsBodyItemVideoFramesItem withFrameImage(String frameImage) {
        this.frameImage = frameImage;
        return this;
    }

    /**
     * Get the reviewerResultTags value.
     *
     * @return the reviewerResultTags value
     */
    public List<CreateVideoReviewsBodyItemVideoFramesItemReviewerResultTagsItem> reviewerResultTags() {
        return this.reviewerResultTags;
    }

    /**
     * Set the reviewerResultTags value.
     *
     * @param reviewerResultTags the reviewerResultTags value to set
     * @return the CreateVideoReviewsBodyItemVideoFramesItem object itself.
     */
    public CreateVideoReviewsBodyItemVideoFramesItem withReviewerResultTags(List<CreateVideoReviewsBodyItemVideoFramesItemReviewerResultTagsItem> reviewerResultTags) {
        this.reviewerResultTags = reviewerResultTags;
        return this;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public List<CreateVideoReviewsBodyItemVideoFramesItemMetadataItem> metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     * @return the CreateVideoReviewsBodyItemVideoFramesItem object itself.
     */
    public CreateVideoReviewsBodyItemVideoFramesItem withMetadata(List<CreateVideoReviewsBodyItemVideoFramesItemMetadataItem> metadata) {
        this.metadata = metadata;
        return this;
    }

}
