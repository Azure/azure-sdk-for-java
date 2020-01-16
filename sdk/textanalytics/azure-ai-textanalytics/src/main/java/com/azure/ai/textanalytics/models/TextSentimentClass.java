// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Defines values for TextSentimentClass.
 */
public final class TextSentimentClass extends ExpandableStringEnum<TextSentimentClass> {
    /**
     * Static value Positive for TextSentimentClass.
     */
    public static final TextSentimentClass POSITIVE = fromString("positive");

    /**
     * Static value Neutral for TextSentimentClass.
     */
    public static final TextSentimentClass NEUTRAL = fromString("neutral");

    /**
     * Static value Negative for TextSentimentClass.
     */
    public static final TextSentimentClass NEGATIVE = fromString("negative");

    /**
     * Static value Mixed for TextSentimentClass.
     */
    public static final TextSentimentClass MIXED = fromString("mixed");

    /**
     * Creates or finds a TextSentimentClass from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding TextSentimentClass.
     */
    @JsonCreator
    public static TextSentimentClass fromString(String name) {
        return fromString(name, TextSentimentClass.class);
    }
}
