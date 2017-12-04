/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.computervision;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * An object describing whether the image contains adult-oriented content
 * and/or is racy.
 */
public class AdultInfo {
    /**
     * A value indicating if the image contains adult-oriented content.
     */
    @JsonProperty(value = "isAdultContent")
    private boolean isAdultContent;

    /**
     * A value indicating if the image is race.
     */
    @JsonProperty(value = "isRacyContent")
    private boolean isRacyContent;

    /**
     * Score from 0 to 1 that indicates how much of adult content is within the
     * image.
     */
    @JsonProperty(value = "adultScore")
    private double adultScore;

    /**
     * Score from 0 to 1 that indicates how suggestive is the image.
     */
    @JsonProperty(value = "racyScore")
    private double racyScore;

    /**
     * Get the isAdultContent value.
     *
     * @return the isAdultContent value
     */
    public boolean isAdultContent() {
        return this.isAdultContent;
    }

    /**
     * Set the isAdultContent value.
     *
     * @param isAdultContent the isAdultContent value to set
     * @return the AdultInfo object itself.
     */
    public AdultInfo withIsAdultContent(boolean isAdultContent) {
        this.isAdultContent = isAdultContent;
        return this;
    }

    /**
     * Get the isRacyContent value.
     *
     * @return the isRacyContent value
     */
    public boolean isRacyContent() {
        return this.isRacyContent;
    }

    /**
     * Set the isRacyContent value.
     *
     * @param isRacyContent the isRacyContent value to set
     * @return the AdultInfo object itself.
     */
    public AdultInfo withIsRacyContent(boolean isRacyContent) {
        this.isRacyContent = isRacyContent;
        return this;
    }

    /**
     * Get the adultScore value.
     *
     * @return the adultScore value
     */
    public double adultScore() {
        return this.adultScore;
    }

    /**
     * Set the adultScore value.
     *
     * @param adultScore the adultScore value to set
     * @return the AdultInfo object itself.
     */
    public AdultInfo withAdultScore(double adultScore) {
        this.adultScore = adultScore;
        return this;
    }

    /**
     * Get the racyScore value.
     *
     * @return the racyScore value
     */
    public double racyScore() {
        return this.racyScore;
    }

    /**
     * Set the racyScore value.
     *
     * @param racyScore the racyScore value to set
     * @return the AdultInfo object itself.
     */
    public AdultInfo withRacyScore(double racyScore) {
        this.racyScore = racyScore;
        return this;
    }

}
