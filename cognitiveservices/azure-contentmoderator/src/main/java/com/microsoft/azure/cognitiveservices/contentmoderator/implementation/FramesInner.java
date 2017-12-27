/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.contentmoderator.Frame;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The response for a Get Frames request.
 */
public class FramesInner {
    /**
     * Id of the review.
     */
    @JsonProperty(value = "ReviewId")
    private String reviewId;

    /**
     * The videoFrames property.
     */
    @JsonProperty(value = "VideoFrames")
    private List<Frame> videoFrames;

    /**
     * Get the reviewId value.
     *
     * @return the reviewId value
     */
    public String reviewId() {
        return this.reviewId;
    }

    /**
     * Set the reviewId value.
     *
     * @param reviewId the reviewId value to set
     * @return the FramesInner object itself.
     */
    public FramesInner withReviewId(String reviewId) {
        this.reviewId = reviewId;
        return this;
    }

    /**
     * Get the videoFrames value.
     *
     * @return the videoFrames value
     */
    public List<Frame> videoFrames() {
        return this.videoFrames;
    }

    /**
     * Set the videoFrames value.
     *
     * @param videoFrames the videoFrames value to set
     * @return the FramesInner object itself.
     */
    public FramesInner withVideoFrames(List<Frame> videoFrames) {
        this.videoFrames = videoFrames;
        return this;
    }

}
