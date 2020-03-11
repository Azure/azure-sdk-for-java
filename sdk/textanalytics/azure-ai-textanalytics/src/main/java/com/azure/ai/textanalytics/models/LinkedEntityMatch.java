// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The {@link LinkedEntityMatch} model.
 */
@Immutable
public final class LinkedEntityMatch {
    /*
     * If a well-known item is recognized, a decimal number denoting the
     * confidence level between 0 and 1 will be returned.
     */
    private final double confidenceScore;

    /*
     * Entity text as appears in the request.
     */
    private final String text;

    /*
     * Grapheme start position for the entity match text.
     */
    private final int graphemeOffset;

    /*
     * Grapheme length for the entity match text.
     */
    private final int graphemeLength;

    /**
     * Creates a {@link LinkedEntityMatch} model that describes linked entity match.
     *
     * @param text The entity text as appears in the request.
     * @param confidenceScore If a well-known item is recognized, a decimal number denoting the
     * confidence level between 0 and 1 will be returned.
     * @param graphemeOffset Grapheme start position for the entity match text.
     * @param graphemeLength Grapheme length for the entity match text.
     */
    public LinkedEntityMatch(String text, double confidenceScore, int graphemeOffset, int graphemeLength) {
        this.text = text;
        this.confidenceScore = confidenceScore;
        this.graphemeOffset = graphemeOffset;
        this.graphemeLength = graphemeLength;
    }

    /**
     * Get the score property: If a well-known item is recognized, a decimal
     * number denoting the confidence level between 0 and 1 will be returned.
     *
     * @return The score value.
     */
    public double getConfidenceScore() {
        return this.confidenceScore;
    }

    /**
     * Get the text property: Entity text as appears in the request.
     *
     * @return The text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get the offset property: Grapheme start position for the entity match text.
     *
     * @return The offset value.
     */
    public int getGraphemeOffset() {
        return this.graphemeOffset;
    }

    /**
     * Get the length property: Grapheme Length for the entity match text.
     *
     * @return The length value.
     */
    public int getGraphemeLength() {
        return this.graphemeLength;
    }
}
