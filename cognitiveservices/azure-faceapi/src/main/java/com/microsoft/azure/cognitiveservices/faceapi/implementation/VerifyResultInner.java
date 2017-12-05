/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.faceapi.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of the verify operation.
 */
public class VerifyResultInner {
    /**
     * True if the two faces belong to the same person or the face belongs to
     * the person, otherwise false.
     */
    @JsonProperty(value = "isIdentical", required = true)
    private boolean isIdentical;

    /**
     * The confidence property.
     */
    @JsonProperty(value = "confidence")
    private double confidence;

    /**
     * Get the isIdentical value.
     *
     * @return the isIdentical value
     */
    public boolean isIdentical() {
        return this.isIdentical;
    }

    /**
     * Set the isIdentical value.
     *
     * @param isIdentical the isIdentical value to set
     * @return the VerifyResultInner object itself.
     */
    public VerifyResultInner withIsIdentical(boolean isIdentical) {
        this.isIdentical = isIdentical;
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
     * @return the VerifyResultInner object itself.
     */
    public VerifyResultInner withConfidence(double confidence) {
        this.confidence = confidence;
        return this;
    }

}
