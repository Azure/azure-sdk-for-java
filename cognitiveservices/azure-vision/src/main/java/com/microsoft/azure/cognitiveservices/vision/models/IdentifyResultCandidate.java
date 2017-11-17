/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.vision.models;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * All possible faces that may qualify.
 */
public class IdentifyResultCandidate {
    /**
     * Id of candidate.
     */
    @JsonProperty(value = "personId", required = true)
    private String personId;

    /**
     * Confidence level in the candidate person: a float number between 0.0 and
     * 1.0.
     */
    @JsonProperty(value = "confidence", required = true)
    private double confidence;

    /**
     * Get the personId value.
     *
     * @return the personId value
     */
    public String personId() {
        return this.personId;
    }

    /**
     * Set the personId value.
     *
     * @param personId the personId value to set
     * @return the IdentifyResultCandidate object itself.
     */
    public IdentifyResultCandidate withPersonId(String personId) {
        this.personId = personId;
        return this;
    }

    /**
     * Get the confidence value.
     *
     * @return the confidence value
     */
    public double confidence() {
        return this.confidence;
    }

    /**
     * Set the confidence value.
     *
     * @param confidence the confidence value to set
     * @return the IdentifyResultCandidate object itself.
     */
    public IdentifyResultCandidate withConfidence(double confidence) {
        this.confidence = confidence;
        return this;
    }

}
