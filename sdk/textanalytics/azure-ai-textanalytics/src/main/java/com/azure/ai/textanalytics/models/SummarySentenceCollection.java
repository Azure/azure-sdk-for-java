// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

/**
 * The {@link SummarySentenceCollection} model.
 */
@Immutable
public final class SummarySentenceCollection extends IterableStream<SummarySentence> {
    private final IterableStream<TextAnalyticsWarning> warnings;

    /**
     * Creates a {@link SummarySentenceCollection} model that describes a extractive summarization sentence collection
     * including warnings.
     *
     * @param sentences An {@link IterableStream} of {@link SummarySentence}.
     * @param warnings An {@link IterableStream} of {@link TextAnalyticsWarning warnings}.
     */
    public SummarySentenceCollection(IterableStream<SummarySentence> sentences,
        IterableStream<TextAnalyticsWarning> warnings) {
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
