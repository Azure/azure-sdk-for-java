// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.textanalytics.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.IterableStream;

import java.util.ArrayList;

/**
 * The {@link ExtractKeyPhraseResult} model.
 */
@Immutable
public final class ExtractKeyPhraseResult extends TextAnalyticsResult {
    private final KeyPhrasesCollection keyPhrases;

    /**
     * Create a {@link ExtractKeyPhraseResult} model that describes extracted key phrases result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param keyPhrases A list of key phrases string.
     * @param warnings A {@link IterableStream} of {@link TextAnalyticsWarning}.
     */
    public ExtractKeyPhraseResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, IterableStream<String> keyPhrases, IterableStream<TextAnalyticsWarning> warnings) {
        super(id, textDocumentStatistics, error);
        this.keyPhrases = new KeyPhrasesCollection(
            keyPhrases == null ? new IterableStream<>(new ArrayList<>()) : keyPhrases, warnings);
    }

    /**
     * Get a list of key phrase string.
     *
     * @return A list of key phrase string.
     */
    public KeyPhrasesCollection getKeyPhrases() {
        throwExceptionIfError();
        return keyPhrases;
    }
}
