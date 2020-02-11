// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 * The LinkedEntityMatch model.
 */
@Immutable
public final class LinkedEntityMatch {
    /*
     * If a well-known item is recognized, a decimal number denoting the
     * confidence level between 0 and 1 will be returned.
     */
    private final double score;

    /*
     * Entity text as appears in the request.
     */
    private final String text;

    /*
     * Start position (in Unicode characters) for the entity match text.
     */
    private final int offset;

    /*
     * Length (in Unicode characters) for the entity match text.
     */
    private final int length;

    /**
     * Creates a {@code LinkedEntityMatch} model that describes linked entity match.
     *
     * @param text entity text as appears in the request
     * @param score if a well-known item is recognized, a decimal number denoting the
     * confidence level between 0 and 1 will be returned
     * @param offset start position (in Unicode characters) for the entity match text
     * @param length length (in Unicode characters) for the entity match text
     */
    public LinkedEntityMatch(String text, double score, int offset, int length) {
        this.text = text;
        this.score = score;
        this.offset = offset;
        this.length = length;
    }

    /**
     * Get the score property: If a well-known item is recognized, a decimal
     * number denoting the confidence level between 0 and 1 will be returned.
     *
     * @return the score value.
     */
    public double getScore() {
        return this.score;
    }

    /**
     * Get the text property: Entity text as appears in the request.
     *
     * @return the text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Get the offset property: Start position (in Unicode characters) for the entity match text.
     *
     * @return the offset value.
     */
    public int getOffset() {
        return this.offset;
    }

    /**
     * Get the length property: Length (in Unicode characters) for the entity match text.
     *
     * @return the length value.
     */
    public int getLength() {
        return this.length;
    }
}
