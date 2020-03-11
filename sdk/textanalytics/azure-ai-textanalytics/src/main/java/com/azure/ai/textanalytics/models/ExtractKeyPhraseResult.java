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
public final class ExtractKeyPhraseResult extends DocumentResult {
    private final IterableStream<String> keyPhrases;

    /**
     * Create a {@link ExtractKeyPhraseResult} model that describes extracted key phrases result.
     *
     * @param id Unique, non-empty document identifier.
     * @param textDocumentStatistics The text document statistics.
     * @param error The document error.
     * @param keyPhrases A list of key phrases string.
     */
    public ExtractKeyPhraseResult(String id, TextDocumentStatistics textDocumentStatistics,
        TextAnalyticsError error, IterableStream<String> keyPhrases) {
        super(id, textDocumentStatistics, error);
        this.keyPhrases = keyPhrases == null ? new IterableStream<>(new ArrayList<>()) : keyPhrases;
    }

    /**
     * Get a list of key phrase string.
     *
     * @return A list of key phrase string.
     */
    public IterableStream<String> getKeyPhrases() {
        throwExceptionIfError();
        return keyPhrases;
    }
}
