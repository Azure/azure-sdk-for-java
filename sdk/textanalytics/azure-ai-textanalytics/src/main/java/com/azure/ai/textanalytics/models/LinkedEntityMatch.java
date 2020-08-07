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
     * Start position for the entity text.
     */
    private final int offset;

    /*
     * Length for the entity text.
     */
    private final int length;

    /**
     * Creates a {@link LinkedEntityMatch} model that describes linked entity match.
     *
     * @param text The entity text as appears in the request.
     * @param confidenceScore If a well-known item is recognized, a decimal number denoting the
     * confidence level between 0 and 1 will be returned.
     */
    public LinkedEntityMatch(String text, double confidenceScore) {
        this.text = text;
        this.confidenceScore = confidenceScore;
        this.offset = 0;
        this.length = 0;
    }

    /**
     * Creates a {@link LinkedEntityMatch} model that describes linked entity match.
     *
     * @param text The entity text as appears in the request.
     * @param offset The start position for the entity text.
     * @param length The length for the entity text.
     * @param confidenceScore If a well-known item is recognized, a decimal number denoting the
     * confidence level between 0 and 1 will be returned.
     */
    public LinkedEntityMatch(String text, int offset, int length, double confidenceScore) {
        this.text = text;
        this.offset = offset;
        this.length = length;
        this.confidenceScore = confidenceScore;
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
     * Get the offset of entity text.
     *
     * @return The offset of entity text.
     */
    public int getOffset() {
        return offset;
    }

    /**
     * Get the length of entity text.
     *
     * @return The length of entity text.
     */
    public int getLength() {
        return length;
    }
}
