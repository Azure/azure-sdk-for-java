// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Defines values for SentimentLabel.
 */
public final class SentimentLabel extends ExpandableStringEnum<SentimentLabel> {
    /**
     * Static value Positive for SentimentLabel.
     */
    public static final SentimentLabel POSITIVE = fromString("positive");

    /**
     * Static value Neutral for SentimentLabel.
     */
    public static final SentimentLabel NEUTRAL = fromString("neutral");

    /**
     * Static value Negative for SentimentLabel.
     */
    public static final SentimentLabel NEGATIVE = fromString("negative");

    /**
     * Static value Mixed for SentimentLabel.
     */
    public static final SentimentLabel MIXED = fromString("mixed");

    /**
     * Creates or finds a SentimentLabel from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding SentimentLabel.
     */
    @JsonCreator
    public static SentimentLabel fromString(String name) {
        return fromString(name, SentimentLabel.class);
    }
}
