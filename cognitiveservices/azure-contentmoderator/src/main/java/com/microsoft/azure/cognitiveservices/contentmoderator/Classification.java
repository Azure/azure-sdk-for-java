/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.contentmoderator;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The classification details of the text.
 */
public class Classification {
    /**
     * The adult score.
     */
    @JsonProperty(value = "AdultScore")
    private Double adultScore;

    /**
     * The racy score.
     */
    @JsonProperty(value = "RacyScore")
    private Double racyScore;

    /**
     * The offensive score.
     */
    @JsonProperty(value = "OffensiveScore")
    private Double offensiveScore;

    /**
     * The review recommended flag.
     */
    @JsonProperty(value = "ReviewRecommended")
    private Boolean reviewRecommended;

    /**
     * Get the adultScore value.
     *
     * @return the adultScore value
     */
    public Double adultScore() {
        return this.adultScore;
    }

    /**
     * Set the adultScore value.
     *
     * @param adultScore the adultScore value to set
     * @return the Classification object itself.
     */
    public Classification withAdultScore(Double adultScore) {
        this.adultScore = adultScore;
        return this;
    }

    /**
     * Get the racyScore value.
     *
     * @return the racyScore value
     */
    public Double racyScore() {
        return this.racyScore;
    }

    /**
     * Set the racyScore value.
     *
     * @param racyScore the racyScore value to set
     * @return the Classification object itself.
     */
    public Classification withRacyScore(Double racyScore) {
        this.racyScore = racyScore;
        return this;
    }

    /**
     * Get the offensiveScore value.
     *
     * @return the offensiveScore value
     */
    public Double offensiveScore() {
        return this.offensiveScore;
    }

    /**
     * Set the offensiveScore value.
     *
     * @param offensiveScore the offensiveScore value to set
     * @return the Classification object itself.
     */
    public Classification withOffensiveScore(Double offensiveScore) {
        this.offensiveScore = offensiveScore;
        return this;
    }

    /**
     * Get the reviewRecommended value.
     *
     * @return the reviewRecommended value
     */
    public Boolean reviewRecommended() {
        return this.reviewRecommended;
    }

    /**
     * Set the reviewRecommended value.
     *
     * @param reviewRecommended the reviewRecommended value to set
     * @return the Classification object itself.
     */
    public Classification withReviewRecommended(Boolean reviewRecommended) {
        this.reviewRecommended = reviewRecommended;
        return this;
    }

}
