// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;

/**
 *
 */
@Immutable
public final class ExtractKeySentencesResult extends TextAnalyticsResult {
    private KeySentenceCollection sentences;

    /**
     * Creates a {@link AnalyzeSentimentResult} model that describes analyzed sentiment result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     */
    public ExtractKeySentencesResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error) {
        super(id, textDocumentStatistics, error);
    }

    /**
     * Get the key sentences.
     *
     * @return The key sentences.
     *
     * @throws TextAnalyticsException if result has {@code isError} equals to true and when a non-error property
     * was accessed.
     */
    public KeySentenceCollection getSentences() {
        throwExceptionIfError();
        return sentences;
    }
}
