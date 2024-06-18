// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.ExpandableStringEnum;

import java.util.Collection;

/**
 * Defines values for {@code TextSentiment}.
 */
@Immutable
public final class TextSentiment extends ExpandableStringEnum<TextSentiment> {
    /**
     * Static value Positive for {@link TextSentiment}.
     */
    public static final TextSentiment POSITIVE = fromString("positive");

    /**
     * Static value Neutral for {@link TextSentiment}.
     */
    public static final TextSentiment NEUTRAL = fromString("neutral");

    /**
     * Static value Negative for {@link TextSentiment}.
     */
    public static final TextSentiment NEGATIVE = fromString("negative");

    /**
     * Static value Mixed for {@link TextSentiment}.
     */
    public static final TextSentiment MIXED = fromString("mixed");

    /**
     * Creates a new instance of {@code TextSentiment} value.
     *
     * @deprecated Use the {@link #fromString(String)} factory method.
     */
    @Deprecated
    public TextSentiment() {
    }

    /**
     * Creates or finds a {@code TextSentiment} from its string representation.
     *
     * @param name A name to look for.
     * @return The corresponding {@code TextSentiment}.
     */
    public static TextSentiment fromString(String name) {
        return fromString(name, TextSentiment.class);
    }

    /**
     * All known TextSentiment values.
     *
     * @return known TextSentiment values.
     */
    public static Collection<TextSentiment> values() {
        return values(TextSentiment.class);
    }
}
