// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Defines values for {@link TextSentimentLabel}.
 */
@Immutable
public final class TextSentimentLabel extends ExpandableStringEnum<TextSentimentLabel> {
    /**
     * Static value Positive for {@link TextSentimentLabel}.
     */
    public static final TextSentimentLabel POSITIVE = fromString("positive");

    /**
     * Static value Neutral for {@link TextSentimentLabel}.
     */
    public static final TextSentimentLabel NEUTRAL = fromString("neutral");

    /**
     * Static value Negative for {@link TextSentimentLabel}.
     */
    public static final TextSentimentLabel NEGATIVE = fromString("negative");

    /**
     * Static value Mixed for {@link TextSentimentLabel}.
     */
    public static final TextSentimentLabel MIXED = fromString("mixed");

    /**
     * Creates or finds a {@link TextSentimentLabel} from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding {@link TextSentimentLabel}.
     */
    @JsonCreator
    public static TextSentimentLabel fromString(String name) {
        return fromString(name, TextSentimentLabel.class);
    }
}
