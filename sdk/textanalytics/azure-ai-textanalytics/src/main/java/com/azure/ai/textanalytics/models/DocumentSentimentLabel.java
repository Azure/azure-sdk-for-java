// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;
import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Defines values for {@link DocumentSentimentLabel}.
 */
@Immutable
public final class DocumentSentimentLabel extends ExpandableStringEnum<DocumentSentimentLabel> {
    /**
     * Static value Positive for {@link DocumentSentimentLabel}.
     */
    public static final DocumentSentimentLabel POSITIVE = fromString("positive");

    /**
     * Static value Neutral for {@link DocumentSentimentLabel}.
     */
    public static final DocumentSentimentLabel NEUTRAL = fromString("neutral");

    /**
     * Static value Negative for {@link DocumentSentimentLabel}.
     */
    public static final DocumentSentimentLabel NEGATIVE = fromString("negative");

    /**
     * Static value Mixed for {@link DocumentSentimentLabel}.
     */
    public static final DocumentSentimentLabel MIXED = fromString("mixed");

    /**
     * Creates or finds a {@link DocumentSentimentLabel} from its string representation.
     *
     * @param name a name to look for.
     * @return the corresponding {@link DocumentSentimentLabel}.
     */
    @JsonCreator
    public static DocumentSentimentLabel fromString(String name) {
        return fromString(name, DocumentSentimentLabel.class);
    }
}
