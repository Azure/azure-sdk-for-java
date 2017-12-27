/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator.implementation;

import java.util.List;
import com.microsoft.azure.cognitiveservices.contentmoderator.Match;
import com.microsoft.azure.cognitiveservices.contentmoderator.Status;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The response for a Match request.
 */
public class MatchResponseInner {
    /**
     * The tracking id.
     */
    @JsonProperty(value = "TrackingId")
    private String trackingId;

    /**
     * The cache id.
     */
    @JsonProperty(value = "CacheID")
    private String cacheID;

    /**
     * Indicates if there is a match.
     */
    @JsonProperty(value = "IsMatch")
    private Boolean isMatch;

    /**
     * The match details.
     */
    @JsonProperty(value = "Matches")
    private List<Match> matches;

    /**
     * The evaluate status.
     */
    @JsonProperty(value = "Status")
    private Status status;

    /**
     * Get the trackingId value.
     *
     * @return the trackingId value
     */
    public String trackingId() {
        return this.trackingId;
    }

    /**
     * Set the trackingId value.
     *
     * @param trackingId the trackingId value to set
     * @return the MatchResponseInner object itself.
     */
    public MatchResponseInner withTrackingId(String trackingId) {
        this.trackingId = trackingId;
        return this;
    }

    /**
     * Get the cacheID value.
     *
     * @return the cacheID value
     */
    public String cacheID() {
        return this.cacheID;
    }

    /**
     * Set the cacheID value.
     *
     * @param cacheID the cacheID value to set
     * @return the MatchResponseInner object itself.
     */
    public MatchResponseInner withCacheID(String cacheID) {
        this.cacheID = cacheID;
        return this;
    }

    /**
     * Get the isMatch value.
     *
     * @return the isMatch value
     */
    public Boolean isMatch() {
        return this.isMatch;
    }

    /**
     * Set the isMatch value.
     *
     * @param isMatch the isMatch value to set
     * @return the MatchResponseInner object itself.
     */
    public MatchResponseInner withIsMatch(Boolean isMatch) {
        this.isMatch = isMatch;
        return this;
    }

    /**
     * Get the matches value.
     *
     * @return the matches value
     */
    public List<Match> matches() {
        return this.matches;
    }

    /**
     * Set the matches value.
     *
     * @param matches the matches value to set
     * @return the MatchResponseInner object itself.
     */
    public MatchResponseInner withMatches(List<Match> matches) {
        this.matches = matches;
        return this;
    }

    /**
     * Get the status value.
     *
     * @return the status value
     */
    public Status status() {
        return this.status;
    }

    /**
     * Set the status value.
     *
     * @param status the status value to set
     * @return the MatchResponseInner object itself.
     */
    public MatchResponseInner withStatus(Status status) {
        this.status = status;
        return this;
    }

}
