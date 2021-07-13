// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 *
 */
@Immutable
public final class KeySentenceCollection extends IterableStream<KeySentence> {
    private final IterableStream<TextAnalyticsWarning> warnings;

    /**
     * Creates a {@link KeySentenceCollection} model that describes a key sentence collection including warnings.
     *
     * @param sentences An {@link IterableStream} of {@link KeySentence}.
     * @param warnings An {@link IterableStream} of {@link TextAnalyticsWarning warnings}.
     */
    public KeySentenceCollection(IterableStream<KeySentence> sentences, IterableStream<TextAnalyticsWarning> warnings) {
        super(sentences);
        this.warnings = warnings;
    }

    /**
     * Get the {@link IterableStream} of {@link TextAnalyticsWarning Text Analytics warnings}.
     *
     * @return {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public IterableStream<TextAnalyticsWarning> getWarnings() {
        return this.warnings;
    }

}
