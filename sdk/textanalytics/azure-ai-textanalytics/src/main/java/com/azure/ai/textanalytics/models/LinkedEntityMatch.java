// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

/**
 * The {@link LinkedEntityMatch} model.
 */
public interface LinkedEntityMatch {
    /**
     * Get the score property: If a well-known item is recognized, a decimal
     * number denoting the confidence level between 0 and 1 will be returned.
     *
     * @return The score value.
     */
    double getConfidenceScore();

    /**
     * Get the text property: Entity text as appears in the request.
     *
     * @return The text value.
     */
    String getText();

    /**
     * Get the offset property: Grapheme start position for the entity match text.
     *
     * @return The offset value.
     */
    int getGraphemeOffset();

    /**
     * Get the length property: Grapheme Length for the entity match text.
     *
     * @return The length value.
     */
    int getGraphemeLength();
}
