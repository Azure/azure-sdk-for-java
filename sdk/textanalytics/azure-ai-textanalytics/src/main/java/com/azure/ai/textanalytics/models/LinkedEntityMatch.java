// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.ai.textanalytics.implementation.LinkedEntityMatchPropertiesHelper;
import com.azure.core.annotation.Immutable;

/**
 * The {@link LinkedEntityMatch} model.
 */
@Immutable
public final class LinkedEntityMatch {
    private final String text;
    private final double confidenceScore;
    private int offset;
    private int length;

    /**
     * Creates a {@link LinkedEntityMatch} model that describes linked entity match.
     *
     * @param text The linked entity match text as appears in the request.
     * @param confidenceScore If a well-known item is recognized, a decimal number denoting the
     * confidence level between 0 and 1 will be returned.
     */
    public LinkedEntityMatch(String text, double confidenceScore) {
        this.text = text;
        this.confidenceScore = confidenceScore;
        this.offset = 0;
    }

    static {
        LinkedEntityMatchPropertiesHelper.setAccessor(
            new LinkedEntityMatchPropertiesHelper.LinkedEntityMatchAccessor() {
                @Override
                public void setLength(LinkedEntityMatch entity, int length) {
                    entity.setLength(length);
                }

                @Override
                public void setOffset(LinkedEntityMatch entity, int offset) {
                    entity.setOffset(offset);
                }
            });
    }

    /**
     * Gets the linked entity match text property: linked entity text as appears in the request.
     *
     * @return The text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Gets the score property: If a well-known item is recognized, a decimal
     * number denoting the confidence level between 0 and 1 will be returned.
     *
     * @return The score value.
     */
    public double getConfidenceScore() {
        return this.confidenceScore;
    }

    /**
     * Gets the offset of linked entity match text. The start position for the linked entity match text in a document.
     *
     * @return The offset of linked entity match text.
     */
    public int getOffset() {
        return offset;
    }

    private void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Gets the length of entity text.
     *
     * @return The length of entity text.
     */
    public int getLength() {
        return length;
    }

    private void setLength(int length) {
        this.length = length;
    }
}
