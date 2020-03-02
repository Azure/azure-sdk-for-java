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
    private final double score;

    /*
     * Entity text as appears in the request.
     */
    private final String text;

    /*
     * Grapheme start position for the entity match text.
     */
    private final int offset;

    /*
     * Grapheme length for the entity match text.
     */
    private final int length;

    /**
     * Creates a {@link LinkedEntityMatch} model that describes linked entity match.
     *
     * @param text The entity text as appears in the request.
     * @param score If a well-known item is recognized, a decimal number denoting the.
     * confidence level between 0 and 1 will be returned.
     * @param offset Grapheme start position for the entity match text.
     * @param length Grapheme length for the entity match text.
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
     * @return The score value.
     */
    public double getScore() {
        return this.score;
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
        return this.offset;
    }

    /**
     * Get the length property: Grapheme Length for the entity match text.
     *
     * @return The length value.
     */
    public int getGraphemeLength() {
        return this.length;
    }
}
