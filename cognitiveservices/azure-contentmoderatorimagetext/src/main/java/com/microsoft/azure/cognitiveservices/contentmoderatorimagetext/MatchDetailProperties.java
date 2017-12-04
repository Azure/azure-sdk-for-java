/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderatorimagetext;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The match detail.
 */
public class MatchDetailProperties {
    /**
     * Confidence score of the image match.
     */
    @JsonProperty(value = "score")
    private Double score;

    /**
     * The match id.
     */
    @JsonProperty(value = "matchId")
    private String matchId;

    /**
     * The source.
     */
    @JsonProperty(value = "source")
    private String source;

    /**
     * The tags for match details.
     */
    @JsonProperty(value = "tags")
    private List<MatchDetailTag> tags;

    /**
     * The label.
     */
    @JsonProperty(value = "label")
    private String label;

    /**
     * Get the score value.
     *
     * @return the score value
     */
    public Double score() {
        return this.score;
    }

    /**
     * Set the score value.
     *
     * @param score the score value to set
     * @return the MatchDetailProperties object itself.
     */
    public MatchDetailProperties withScore(Double score) {
        this.score = score;
        return this;
    }

    /**
     * Get the matchId value.
     *
     * @return the matchId value
     */
    public String matchId() {
        return this.matchId;
    }

    /**
     * Set the matchId value.
     *
     * @param matchId the matchId value to set
     * @return the MatchDetailProperties object itself.
     */
    public MatchDetailProperties withMatchId(String matchId) {
        this.matchId = matchId;
        return this;
    }

    /**
     * Get the source value.
     *
     * @return the source value
     */
    public String source() {
        return this.source;
    }

    /**
     * Set the source value.
     *
     * @param source the source value to set
     * @return the MatchDetailProperties object itself.
     */
    public MatchDetailProperties withSource(String source) {
        this.source = source;
        return this;
    }

    /**
     * Get the tags value.
     *
     * @return the tags value
     */
    public List<MatchDetailTag> tags() {
        return this.tags;
    }

    /**
     * Set the tags value.
     *
     * @param tags the tags value to set
     * @return the MatchDetailProperties object itself.
     */
    public MatchDetailProperties withTags(List<MatchDetailTag> tags) {
        this.tags = tags;
        return this;
    }

    /**
     * Get the label value.
     *
     * @return the label value
     */
    public String label() {
        return this.label;
    }

    /**
     * Set the label value.
     *
     * @param label the label value to set
     * @return the MatchDetailProperties object itself.
     */
    public MatchDetailProperties withLabel(String label) {
        this.label = label;
        return this;
    }

}
