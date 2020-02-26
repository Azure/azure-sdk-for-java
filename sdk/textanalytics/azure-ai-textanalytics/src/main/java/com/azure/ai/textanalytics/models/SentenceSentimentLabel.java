// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Defines values for {@link SentenceSentimentLabel}.
 */
@Immutable
public final class SentenceSentimentLabel extends ExpandableStringEnum<SentenceSentimentLabel> {
    /**
     * Static value Positive for {@link SentenceSentimentLabel}.
     */
    public static final SentenceSentimentLabel POSITIVE = fromString("positive");

    /**
     * Static value Neutral for {@link SentenceSentimentLabel}.
     */
    public static final SentenceSentimentLabel NEUTRAL = fromString("neutral");

    /**
     * Static value Negative for {@link SentenceSentimentLabel}.
     */
    public static final SentenceSentimentLabel NEGATIVE = fromString("negative");

    /**
     * Creates or finds a {@link SentenceSentimentLabel} from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding {@link SentenceSentimentLabel}.
     */
    @JsonCreator
    public static SentenceSentimentLabel fromString(String name) {
        return fromString(name, SentenceSentimentLabel.class);
    }
}
