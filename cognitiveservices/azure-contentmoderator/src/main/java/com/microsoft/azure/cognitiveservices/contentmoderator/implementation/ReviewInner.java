/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.contentmoderator.KeyValuePair;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Review object.
 */
public class ReviewInner {
    /**
     * Id of the review.
     */
    @JsonProperty(value = "ReviewId")
    private String reviewId;

    /**
     * Name of the subteam.
     */
    @JsonProperty(value = "SubTeam")
    private String subTeam;

    /**
     * The status string (&lt;Pending, Complete&gt;).
     */
    @JsonProperty(value = "Status")
    private String status;

    /**
     * Array of KeyValue with Reviewer set Tags.
     */
    @JsonProperty(value = "ReviewerResultTags")
    private List<KeyValuePair> reviewerResultTags;

    /**
     * The reviewer name.
     */
    @JsonProperty(value = "CreatedBy")
    private String createdBy;

    /**
     * Array of KeyValue.
     */
    @JsonProperty(value = "Metadata")
    private List<KeyValuePair> metadata;

    /**
     * The type of content.
     */
    @JsonProperty(value = "Type")
    private String type;

    /**
     * The content value.
     */
    @JsonProperty(value = "Content")
    private String content;

    /**
     * Id of the content.
     */
    @JsonProperty(value = "ContentId")
    private String contentId;

    /**
     * The callback endpoint.
     */
    @JsonProperty(value = "CallbackEndpoint")
    private String callbackEndpoint;

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
     * @return the ReviewInner object itself.
     */
    public ReviewInner withReviewId(String reviewId) {
        this.reviewId = reviewId;
        return this;
    }

    /**
     * Get the subTeam value.
     *
     * @return the subTeam value
     */
    public String subTeam() {
        return this.subTeam;
    }

    /**
     * Set the subTeam value.
     *
     * @param subTeam the subTeam value to set
     * @return the ReviewInner object itself.
     */
    public ReviewInner withSubTeam(String subTeam) {
        this.subTeam = subTeam;
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
     * @return the ReviewInner object itself.
     */
    public ReviewInner withStatus(String status) {
        this.status = status;
        return this;
    }

    /**
     * Get the reviewerResultTags value.
     *
     * @return the reviewerResultTags value
     */
    public List<KeyValuePair> reviewerResultTags() {
        return this.reviewerResultTags;
    }

    /**
     * Set the reviewerResultTags value.
     *
     * @param reviewerResultTags the reviewerResultTags value to set
     * @return the ReviewInner object itself.
     */
    public ReviewInner withReviewerResultTags(List<KeyValuePair> reviewerResultTags) {
        this.reviewerResultTags = reviewerResultTags;
        return this;
    }

    /**
     * Get the createdBy value.
     *
     * @return the createdBy value
     */
    public String createdBy() {
        return this.createdBy;
    }

    /**
     * Set the createdBy value.
     *
     * @param createdBy the createdBy value to set
     * @return the ReviewInner object itself.
     */
    public ReviewInner withCreatedBy(String createdBy) {
        this.createdBy = createdBy;
        return this;
    }

    /**
     * Get the metadata value.
     *
     * @return the metadata value
     */
    public List<KeyValuePair> metadata() {
        return this.metadata;
    }

    /**
     * Set the metadata value.
     *
     * @param metadata the metadata value to set
     * @return the ReviewInner object itself.
     */
    public ReviewInner withMetadata(List<KeyValuePair> metadata) {
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
     * @return the ReviewInner object itself.
     */
    public ReviewInner withType(String type) {
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
     * @return the ReviewInner object itself.
     */
    public ReviewInner withContent(String content) {
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
     * @return the ReviewInner object itself.
     */
    public ReviewInner withContentId(String contentId) {
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
     * @return the ReviewInner object itself.
     */
    public ReviewInner withCallbackEndpoint(String callbackEndpoint) {
        this.callbackEndpoint = callbackEndpoint;
        return this;
    }

}
